---
name: create-pr
description: "Use this skill to create a pull request after dependency upgrades (or any successful, committed changes). Validates repo state, generates a structured PR description, requires explicit approval before pushing."
argument-hint: "create-pr [--base master]"
---

# create-pr Skill

Generic PR creation after successful changes. Works for any repo — not specific to dependency upgrades.

## Prerequisites

### GitHub CLI (`gh`) must be installed and authenticated

**Install GitHub CLI** (if not already installed):

```bash
# macOS (Homebrew)
brew install gh

# Linux (apt)
sudo apt install gh

# Windows — choose one:
winget install GitHub.cli          # Windows 11 / winget (recommended)
choco install gh                   # Chocolatey
scoop install gh                   # Scoop
# or: download the *.msi installer from https://github.com/cli/cli/releases
```

**Authenticate:**
```bash
gh auth status    # check current auth
gh auth login     # if not authenticated
```

**Requirements before invoking this skill:**
- Changes must be committed (or staged for commit)
- Build must be green (`./gradlew clean build`)
- You must **not** be on `master` or `main` (or be prepared to create a new branch)

---

## Workflow

### Step 1 — Validate repo state

```bash
git status
git branch --show-current
git log master..HEAD --oneline
```

Check:
- Not on `master`/`main` → if so, go to Step 2 branch handling
- Has at least one commit ahead of base branch
- No unrelated staged changes (warn user if mix of changes detected)

### Step 2 — Branch handling

If already on a feature branch: proceed.

If on `master`/`main`:
> "You are on `master`. A new branch is needed. Suggest: `chore/deps-update-YYYY-MM-DD`. Use this name or provide your own?"

Create branch only after explicit confirmation:
```bash
git checkout -b chore/deps-update-2026-03-27
```

### Step 3 — Build validation

Check the [Jenkinsfile](../../Jenkinsfile) for the canonical build steps. For overthere:

```bash
# Standard build (always run)
./gradlew clean build
# Windows:
gradlew.bat clean build

# Integration tests (run if SSH/SMB/WinRM/CIFS deps changed)
# Requires Overcast cloud infra — skip if not available:
# ./gradlew itest
```

**STOP if the build fails.** Do not create a PR with a broken build.

### Step 4 — Collect change context

```bash
git diff --name-only master...HEAD
git diff master...HEAD
```

Also read (if available):
- `dependency-upgrade-report.md` — summary of applied upgrades
- `dependency-upgrade-report.json` — structured upgrade data

### Step 5 — Generate PR title

Conventional commit format:

```
chore(deps): upgrade {primary-libraries}
```

Examples:
- `chore(deps): upgrade slf4j 2.0.13→2.0.17, bouncycastle 1.80→1.83`
- `chore(deps): upgrade sshj 0.38→0.40, smbj 0.13→0.14`
- `chore(deps): upgrade 8 patch/minor dependencies`

### Step 6 — Generate PR description

Use this template (remove irrelevant lines; keep checkboxes **unchecked**):

```markdown
**Summary**
- Dependency upgrades applied via dependency-scan skill.
- {Briefly describe which libraries were upgraded and why (e.g. security patch, new features needed)}.

**What Changed**
- [ ] Dependency version bumps in `build.gradle`
- [ ] Build/configuration updates (if any)
- [ ] Tests/docs updates (if any)

**Validation**
- [ ] `./gradlew clean build` passes
- [ ] Integration tests considered (SSH/SMB/WinRM paths affected? If yes, run `./gradlew itest`)

**Risk Notes**
- [ ] Backward compatibility considered
- [ ] Security-category updates reviewed against changelogs
- [ ] Major version bumps (if any) reviewed for API changes

**Optional Dependency Notes** _(include only when relevant)_
- [ ] Reviewed `dependency-upgrade-report.md`
- [ ] Related versions kept in sync (e.g. bcprov + bcpkix, slf4j-api + jcl-over-slf4j)

**Checklist**
- [ ] Branch pushed to origin
- [ ] Build is green
- [ ] PR labels added as needed
- [ ] Ready for review
```

**CI labels to consider** (add with `--label`):
- `ci-skip-integration-tests` — when only build plugin or test-scope deps changed
- `ci-run-platform-build` — when runtime deps changed and full verification is needed
- `ci-skip-sonar-analysis` — if sonar quota is low and changes are mechanical

### Step 7 — Show to user and ask for approval

Display the generated title and body in full, then:
> "Create PR with the above title and description? (yes / edit / no)"

If `edit`: open editor or accept freeform corrections before proceeding.

### Step 8 — Push and create PR

```bash
git push --set-upstream origin <branch-name>

gh pr create \
  --base master \
  --title "<TITLE>" \
  --body "<BODY>"

# With labels:
gh pr create \
  --base master \
  --title "<TITLE>" \
  --body "<BODY>" \
  --label "ci-skip-integration-tests"
```

### Step 9 — Return PR URL

Report back:
> "PR created: https://github.com/xebialabs/overthere/pull/NNN"

---

## Guardrails

- ❌ Never create PR without explicit user `yes`
- ❌ Never skip build validation
- ❌ Never run destructive git commands (`--force`, `reset --hard`, `push --force`)
- ❌ Never invent versions or change summaries — use actual `git diff` and report data
- ✅ Always check `gh auth status` before attempting PR creation
- ✅ Always warn if there are uncommitted changes before pushing
