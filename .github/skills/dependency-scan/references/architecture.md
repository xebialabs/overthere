# Dependency Scan — Architecture

## How `overthere` manages dependencies

`overthere` is a **standalone Java library** that does **not** consume the xl-platform BOM. All dependency versions are hardcoded directly in `build.gradle`.

```
build.gradle
├── buildscript { dependencies { ... } }   ← Gradle plugin versions (5 entries)
└── dependencies { ... }                   ← Library versions (~20 runtime + ~7 test entries)
```

## Dependency source files

| File | Format | Contains |
|---|---|---|
| `build.gradle` (buildscript block) | Groovy string literals `'group:artifact:version'` | Gradle plugin versions |
| `build.gradle` (dependencies block) | Groovy string literals | All runtime and test library versions |
| `gradle.properties` | `key=value` | Only JVM/SSL args — **no version variables** |

## Coordinate maps

### `REPO_COORDINATES` (9 entries)

Maps the artifact identifier (as it appears in `build.gradle`) to its Maven coordinate for version lookup:

| Artifact string | Maven coordinate |
|---|---|
| `io.codearte.gradle.nexus:gradle-nexus-staging-plugin` | `io.codearte.gradle.nexus:gradle-nexus-staging-plugin` |
| `com.netflix.nebula:nebula-release-plugin` | `com.netflix.nebula:nebula-release-plugin` |
| `ru.vyarus:gradle-pom-plugin` | `ru.vyarus:gradle-pom-plugin` |
| `org.ajoberstar.grgit:grgit-gradle` | `org.ajoberstar.grgit:grgit-gradle` |
| `com.github.hierynomus.license` (plugin id) | `gradle.plugin.com.hierynomus.license:licensegradle` |
| `com.hierynomus:sshj` | `com.hierynomus:sshj` |
| `com.hierynomus:smbj` | `com.hierynomus:smbj` |
| `com.google.auth:google-auth-library-oauth2-http` | `com.google.auth:google-auth-library-oauth2-http` |
| `com.google.cloud:google-cloud-os-login` | `com.google.cloud:google-cloud-os-login` |

### `CODE_ANALYSIS_COORDINATES` (0 entries)

`overthere` has no `codeAnalysis.gradle` or equivalent — no code-analysis tool versions to track here.

### `XL_PLATFORM_COORDINATES` (114 entries)

Shared with all xl-platform consumers. See `scan-dependencies.py` for full table.

## Two-tier resolution

```
Variable/artifact
       │
       ├─► XL_PLATFORM_COORDINATES (114 entries)  ─► Maven Central → Nexus
       ├─► REPO_COORDINATES (9 entries)            ─► Maven Central → Nexus
       ├─► CODE_ANALYSIS_COORDINATES (0 entries)
       ├─► SKIP_VARIABLES (24 entries)             ─► skip
       └─► DYNAMIC_PATTERNS (46 patterns)          ─► infer groupId:artifactId
                                                       → Maven Central search API
                                                       → Nexus search fallback
```

## Dependency categories (with risk ratings)

| Category | Risk | Examples in overthere |
|---|---|---|
| Security | 🔴 High | `org.bouncycastle:bcprov-jdk18on`, `org.bouncycastle:bcpkix-jdk18on` |
| Core Framework | 🔴 High | `nl.javadude.scannit:scannit`, `org.slf4j:slf4j-api` |
| Logging | 🔴 High | `org.slf4j:jcl-over-slf4j`, `ch.qos.logback:logback-classic` |
| SSH / Network | 🟠 Medium | `com.hierynomus:sshj`, `com.hierynomus:smbj`, `jcifs:jcifs` |
| HTTP | 🟠 Medium | `org.apache.httpcomponents:httpclient`, `org.apache.httpcomponents:httpcore` |
| Serialization | 🟠 Medium | `org.dom4j:dom4j`, `jaxen:jaxen`, `commons-codec:commons-codec` |
| GCP | 🟠 Medium | `com.google.apis:google-api-services-compute`, `com.google.cloud:google-cloud-os-login` |
| Testing | 🟡 Low | `org.testng:testng`, `org.mockito:mockito-core`, `org.hamcrest:*`, `com.google.guava:guava` |
| Build | 🟢 Info | Gradle buildscript plugins (nexus-staging, nebula-release, grgit, etc.) |

## Alignment checks (Phase 3)

| Check | Rule |
|---|---|
| Bouncy Castle | `bcprov-jdk18on` version must equal `bcpkix-jdk18on` version |
| Apache HttpComponents | `httpclient` 4.x is compatible with `httpcore` 4.x; verify minor alignment |
| SLF4J | `slf4j-api` and `jcl-over-slf4j` must share the same version |
| Guava (test) | Track separately from runtime Guava to avoid accidental runtime promotion |

## Pre-release filtering rules

Versions matching any of the following patterns are excluded from "latest stable":

- `SNAPSHOT` (case-insensitive)
- `alpha`, `beta`, `rc`, `cr` (case-insensitive)
- `M1`–`M9` milestone suffixes
- `milestone`, `dev`, `incubating` (case-insensitive)
- `-pr`, `-preview` suffixes

## Internal group prefixes (skip Maven Central lookup)

- `com.xebialabs.*`
- `ai.digital.*`

These are internal artifacts not published to Maven Central.
