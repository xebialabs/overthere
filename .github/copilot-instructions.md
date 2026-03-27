# Overthere Project Guidelines

## Architecture

Overthere is a Java library that abstracts remote file manipulation and process execution behind a protocol-agnostic API (think `java.io.File` and `java.lang.Process` as interfaces). Key layers:

- **Public API** (`src/main/java/com/xebialabs/overthere/`): `Overthere` (factory), `OverthereConnection`, `OverthereFile`, `OverthereProcess`, `ConnectionOptions`, `CmdLine`/`CmdLineArgument`
- **SPI** (`spi/`): `OverthereConnectionBuilder`, `BaseOverthereConnection`, `BaseOverthereFile`, `Protocol` annotation — extend these when adding a new protocol
- **Protocol implementations**: `local/`, `ssh/`, `cifs/`, `smb/`, `winrm/`, `winrs/`, `gcp/`, `telnet/`, `proxy/`
- **Utilities** (`util/`): `OverthereUtils`, `OverthereFileTransmitter`, `OverthereFileTranscoder`

**SPI plugin mechanism**: Connection builders are annotated with `@Protocol(name = "...")` and discovered at runtime via classpath scanning (scannit library). `OverthereConnector` instantiates the builder reflectively. See [SshConnectionBuilder.java](../src/main/java/com/xebialabs/overthere/ssh/SshConnectionBuilder.java) and [CifsConnectionBuilder.java](../src/main/java/com/xebialabs/overthere/cifs/CifsConnectionBuilder.java) as reference implementations.

## Build and Test

```bash
# Build (Windows)
gradlew clean build

# Build (Unix)
./gradlew clean build

# Run only unit tests (no cloud infra needed)
gradlew test

# Publish to Maven Central (requires PGP key + Central Publisher Portal tokens)
gradle clean build signArchives uploadArchives closeAndPromoteRepository
```

- **Java 21** required (`sourceCompatibility = JavaVersion.VERSION_21`)
- **Integration tests** (`itest/`) require live cloud hosts configured via [Overcast](https://github.com/xebialabs/overcast) (`src/test/resources/overcast.conf`)
- `LocalConnectionTest` is the only itest that runs without cloud infra

## Code Style

- **Testing framework**: TestNG (not JUnit) — use `org.testng.annotations.@Test`, `@BeforeMethod`, `@AfterMethod`
- **Conditional tests**: Use `@Assumption` from AssumEng (e.g., `@Assumption(methods = "onWindows")`) — see [ItestsBase6Windows.java](../src/test/java/com/xebialabs/overthere/itest/ItestsBase6Windows.java)
- **Assertions**: Hamcrest (`assertThat(..., is(...))` / `equalTo(...)`) preferred over TestNG `assert*`
- **Error handling**: Throw `RuntimeIOException` (not checked exceptions) for I/O failures in `OverthereFile`/`OverthereConnection` implementations
- All source files must carry the GPLv2 copyright header — use the template in [HEADER](../HEADER)

## Project Conventions

- **ConnectionOptions**: All protocol-specific options are declared as `public static final String` constants in the corresponding `*ConnectionBuilder`. Boolean/numeric defaults follow the `*_DEFAULT` naming convention (e.g., `CIFS_PORT_DEFAULT`).
- **Integration test hierarchy**: New connection itests extend the chain `ItestsBase1Utils` → `ItestsBase2Basics` → `ItestsBase3Copy` → `ItestsBase4Size` → `ItestsBase5Unix` → `ItestsBase6Windows`; override `getProtocol()`, `getOptions()`, and `getExpectedConnectionClassName()`
- **Examples**: Standalone Maven project under `examples/` demonstrating library usage; kept intentionally simple

## Integration Points

- **SSH**: [sshj](https://github.com/hierynomus/sshj) library (`com.hierynomus:sshj`)
- **CIFS/SMB**: jcifs (`jcifs:jcifs`) and smbj for SMB 2.x
- **GCP**: Google Cloud SDK — two flavours: OsLogin ([GcpOsLoginSshConnection.java](../examples/src/main/java/com/xebialabs/overthere/GcpOsLoginSshConnection.java)) and Metadata ([GcpMetaSshConnection.java](../examples/src/main/java/com/xebialabs/overthere/GcpMetaSshConnection.java))
- **WinRM**: Internal HTTP implementation (`winrm/`) and native `winrs` (`winrs/`)

## Security

- GCP credentials are configured via `GCP_CREDENTIALS_TYPE` option; supported types in `GcpCredentialsType` enum (ServiceAccountJsonFile, ServiceAccountToken, OsLogin, etc.) — test key files are in `src/test/resources/gcp/` and must **not** contain real credentials
- Kerberos config for WinRM domain auth lives in `src/test/resources/winrm/conf/krb5.conf` — treat as sensitive
