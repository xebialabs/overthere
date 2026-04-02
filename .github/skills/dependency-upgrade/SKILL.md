---
name: dependency-upgrade
description: "Use this skill to interactively select and apply dependency upgrades to overthere. Always reads or re-runs the scan report first, then lets you pick which updates to apply."
argument-hint: "dependency-upgrade [--report path/to/report.json] [--skip-build]"
---

# dependency-upgrade Skill — overthere

Interactive upgrade workflow: scan → select → confirm → apply → verify → (optionally) PR.

## Step 1 — Check scan report & ask user

Always ask the user one of these:

**If `dependency-upgrade-report.json` already exists in the repo root:**
> "A scan report already exists (generated on {timestamp}). Use existing report or re-scan?"
> - [1] Use existing report
> - [2] Re-scan now (all phases)

**If no report exists:**
> "No scan report found. Running dependency scan now..."
→ Automatically run:
```bash
python3 .github/skills/dependency-scan/scripts/scan-dependencies.py --phase scan
```
> Add `--phase all --xl-platform-dir ../xl-platform` if xl-platform scan is also requested.

> ⚠️ The scan script lives **only** in `dependency-scan/scripts/` — do NOT duplicate it here.

## Step 2 — Present numbered checkbox list

Group by bump type with defaults pre-applied:

```
Select updates to apply (defaults: patch ✅ minor ✅ major ☐):

Patch updates
  [x] 1. org.slf4j:slf4j-api          2.0.13 → 2.0.17      🔴 Logging
  [x] 2. org.slf4j:jcl-over-slf4j     2.0.13 → 2.0.17      🔴 Logging
  [x] 3. org.bouncycastle:bcprov-jdk18on 1.80 → 1.83        🔴 Security
  ...

Minor updates
  [x] 7. com.hierynomus:sshj          0.38.0 → 0.40.0       🟠 SSH
  ...

Major updates
  [ ] 12. org.apache.httpcomponents:httpclient  4.5.14 → 5.3.0  🟠 HTTP
  ...

Type toggle commands (e.g. +1, -3, all patch, ok) or see selection-protocol.md
```

## Step 3 — Toggle commands

See [selection-protocol.md](references/selection-protocol.md) for full command reference.

## Step 4 — Confirmation

After `ok`:
1. Show final summary table (checked items only)
2. Require explicit `yes` before any file is modified

## Step 5 — Apply xl-platform upgrades (if Phase 1 was run)

1. Edit `xl-reference.conf` / `xl-jakartaee-bom.conf` in xl-platform
2. Keep related versions in sync (e.g. `jacksonVersion` and `jacksonModuleScalaVersion`)
3. Build & publish xl-platform:
   ```bash
   cd ../xl-platform && ./gradlew clean build publishToMavenLocal -x test
   ```
4. If an artifact fails Nexus resolution → revert that dep, inform user, retry build
5. Update `xlPlatformVersion` in `gradle/dependencies.conf` to the SNAPSHOT version (if applicable)
6. Verify overthere: `cd {REPO} && ./gradlew clean build`

> **Note**: overthere does not currently consume the xl-platform BOM. Skip xl-platform steps unless you have added BOM consumption.

## Step 6 — Apply repo upgrades (build.gradle)

For each selected update:

1. **Backup** `build.gradle` with timestamp: `build.gradle.backup.YYYYMMDD_HHMMSS`
2. **Edit** in place:
   - Buildscript plugin: replace `'group:artifact:OLD'` → `'group:artifact:NEW'` in the `buildscript` block
   - Library dep: replace `'group:artifact:OLD'` → `'group:artifact:NEW'` in the `dependencies` block
   - Plugin block: replace `version "OLD"` → `version "NEW"` on the line with that plugin id
3. **Build verify**:
   ```bash
   ./gradlew clean build
   ```
   (on Windows: `gradlew.bat clean build`)

## Step 7 — Rollback on failure

If the build fails after any edit:
1. Restore all `.backup.*` files
2. Report which update caused the failure
3. Stop execution (do not attempt remaining updates)

## Step 8 — Offer PR creation

After a successful build:
> "All selected upgrades applied successfully. Create a PR? (yes/no)"

If yes, hand off to `create-pr` skill.

---

## Quick Scan + Upgrade (combined)

When user says "quick scan and upgrade" or similar:

1. Run scan silently (`--phase scan`)
2. Present **combined checkbox list** in one shot
3. Wait for user selection + `ok`
4. Apply in listed order
5. One build verify at the end

---

## Execute script

```bash
python3 .github/skills/dependency-upgrade/scripts/execute-upgrade.py \
  --report dependency-upgrade-report.json \
  --project-dir .

# Skip build verification (dry run)
python3 .github/skills/dependency-upgrade/scripts/execute-upgrade.py \
  --report dependency-upgrade-report.json \
  --project-dir . \
  --skip-build

# Auto-select specific items (non-interactive)
python3 .github/skills/dependency-upgrade/scripts/execute-upgrade.py \
  --report dependency-upgrade-report.json \
  --auto-select 1,3,5
```

---

## Rules

- ❌ Never auto-select updates without user confirmation
- ❌ Never apply upgrades without an explicit `yes`
- ❌ Never skip build verification after edits
- ✅ Always roll back on build failure
- ✅ Always invoke `create-pr` only after successful build and explicit user consent
