---
name: dependency-scan
description: "Use this skill to scan overthere's dependencies for available updates. Runs 3 phases: xl-platform BOM scan (optional), repo hardcoded dep scan, and alignment check."
argument-hint: "dependency-scan [--phase all|xl-platform-scan|scan|alignment] [--xl-platform-dir ../xl-platform]"
---

# dependency-scan Skill — overthere

Orchestrates a full dependency scan across all three phases and produces upgrade reports.

## Phases

### Phase 1 — xl-platform scan (delegated to `xl-platform-scan` skill)

Scans the xl-platform BOM for upstream dependency updates. See [`xl-platform-scan` skill](../xl-platform-scan/SKILL.md) for full details.

> **Note**: `overthere` does not consume the xl-platform BOM directly. Run Phase 1 only if you are also maintaining `xl-platform` and want to propagate upstream updates.

Parses:
- `xl-reference/xl-reference.conf` (~100 unique vars)
- `xl-jakartaee-bom/xl-jakartaee-bom.conf` (~30 vars)
- xl-platform's `gradle.properties` (~5 build plugin vars)
- **~135 total unique version variables**

Resolves via `XL_PLATFORM_COORDINATES` (114 entries) + `SKIP_VARIABLES` (24 entries); dynamic inference (46 patterns) handles unmapped vars.

### Phase 2 — repo scan (overthere)

Scans `overthere`'s own dependency files for hardcoded versions not managed by any BOM.

| Source file | What it contains |
|---|---|
| `build.gradle` (buildscript block) | Gradle build plugin versions: nexus-staging, nebula-release, gradle-pom, grgit, license |
| `build.gradle` (dependencies block) | All library versions: sshj, smbj, jcifs, bouncycastle, httpcomponents, dom4j, jaxen, grpc, GCP, testng, mockito, guava, logback, etc. |
| `gradle.properties` | JVM args only — no library versions defined here |

Uses:
- `REPO_COORDINATES` (9 entries) — this repo's Gradle plugin → Maven coordinate mappings
- `CODE_ANALYSIS_COORDINATES` (0 entries) — no code-analysis tool config in overthere

### Phase 3 — alignment check

Verifies that any version overrides in `build.gradle` are consistent with each other (e.g. Bouncy Castle `bcprov` and `bcpkix` should match, `httpclient` and `httpcore` should be compatible). Flags mismatches.

## Interactive workflow

1. Ask user: "Scan xl-platform too? (yes/no)" (defaults to **no** for standalone repos)
2. Run Phase 2 (always) ± Phase 1 (if yes)
3. Run Phase 3 (alignment check)
4. Output reports
5. Offer handoff: "Run dependency-upgrade to apply selected upgrades?"

## Run command

```bash
# All phases (including xl-platform scan)
python3 .github/skills/dependency-scan/scripts/scan-dependencies.py \
  --phase all \
  --xl-platform-dir ../xl-platform

# Repo scan + alignment only (no xl-platform)
python3 .github/skills/dependency-scan/scripts/scan-dependencies.py \
  --phase scan

# Alignment check only
python3 .github/skills/dependency-scan/scripts/scan-dependencies.py \
  --phase alignment
```

## Output

Both files are written (or overwritten) in the repo root:

| File | Purpose |
|---|---|
| `dependency-upgrade-report.md` | Human-readable grouped upgrade table |
| `dependency-upgrade-report.json` | Machine-readable; consumed by `execute-upgrade.py` |

## Configuration & credentials

- **Maven Central**: no credentials required
- **Nexus**: optional; set `NEXUS_USER` + `NEXUS_PASSWORD` env vars, or `nexusUserName`/`nexusPassword` in `~/.gradle/gradle.properties`
- Nexus base URL: `https://nexus.xebialabs.com/nexus/content` (searches `releases`, `central`, `public`)
