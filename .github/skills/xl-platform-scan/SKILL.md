---
name: xl-platform-scan
description: "Use this skill to scan the xl-platform BOM for available dependency updates. Standalone — reusable for any repo that consumes the xl-platform BOM."
argument-hint: "xl-platform-scan [--xl-platform-dir ../xl-platform]"
---

# xl-platform-scan Skill

Standalone scan of the xl-platform BOM for available dependency updates. **Reusable across any repo** that consumes the xl-platform BOM — no dependency on any specific repo's configuration.

## What it scans

| Source file | Variables | Notes |
|---|---|---|
| `xl-reference/xl-reference.conf` | ~100 unique version vars | Core platform dependency versions |
| `xl-jakartaee-bom/xl-jakartaee-bom.conf` | ~30 vars | Jakarta EE stack |
| `gradle.properties` (xl-platform root) | ~5 build plugin vars | Gradle plugin versions |
| **Total** | **~135 unique version variables** | |

## Resolution strategy

1. **`XL_PLATFORM_COORDINATES`** — 114 explicit `variableName → group:artifact` mappings (78 from xl-reference.conf + 30 Jakarta BOM + 6 build plugins)
2. **`SKIP_VARIABLES`** — 24 variables excluded from lookup (meta vars, Python plugin deps, internal forks, Gradle settings); see below
3. **Dynamic inference** — 46 regex patterns strip `Version` suffix and infer Maven coordinates via Maven Central search API → Nexus search fallback

Coverage: 114 mapped + 24 skipped = full coverage of all ~135 source variables.

## Skipped variables (24 total, `SKIP_VARIABLES`)

| Category | Variables |
|---|---|
| Meta / non-version | `scalaVersion`, `scalaFullVersion`, `pekkoMajorVersion` |
| Python plugin deps | `pyTestVersion`, `pyMockVersion`, `pyConanVersion`, `pyJinja2Version`, `pyParamikoVersion`, `pyRequestsVersion`, `pySixVersion`, `pyYamlVersion`, `pyLxmlVersion`, `pySetuptoolsVersion`, `pyWheelVersion`, `pyTwineVersion`, `pyVirtualenvVersion` |
| Internal artifacts | `crashVersion`, `scannitVersion`, `docBaseStyleVersion`, `overcastVersion`, `jythonStandaloneVersion` |
| Derived versions | `jacksonAnnotationsVersion` (follows `jacksonVersion`) |
| Gradle settings | `languageLevel`, `release.stage` |

## Risk categories

| Category | Risk | Examples |
|---|---|---|
| Security | 🔴 High | Bouncy Castle, TLS libs |
| Core Framework | 🔴 High | Spring Boot, Hibernate |
| Logging | 🔴 High | SLF4J, Logback |
| Data | 🟠 Medium | JDBC drivers, connection pools |
| Serialization | 🟠 Medium | Jackson, Kryo |
| HTTP | 🟠 Medium | Apache HttpClient, Netty |
| Testing | 🟡 Low | TestNG, Mockito, Hamcrest |
| Build | 🟢 Info | Gradle plugins |

## Spring compatibility matrix

| Dependency | Constraint |
|---|---|
| Hibernate | Must be compatible with Spring Boot version |
| Spring Security | Must match Spring Boot major version |
| Jackson | Managed by Spring Boot BOM — do not independently upgrade |

## Run command

```bash
python3 .github/skills/dependency-scan/scripts/scan-dependencies.py \
  --phase xl-platform-scan \
  --xl-platform-dir ../xl-platform
```

> **Note**: This skill has no `scripts/` or `references/` of its own.  
> Script lives at: `dependency-scan/scripts/scan-dependencies.py`  
> Architecture details at: `dependency-scan/references/architecture.md`

## Output

- `.github/skills/dependency-scan/dependency-upgrade-report.md` — human-readable report
- `.github/skills/dependency-scan/dependency-upgrade-report.json` — machine-readable JSON for upgrade skill
