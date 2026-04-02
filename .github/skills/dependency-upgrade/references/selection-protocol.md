# Selection Protocol

Reference for toggle commands when choosing which dependency upgrades to apply.

## Toggle commands

| Command | Effect |
|---------|--------|
| `+N` or `check N` | Check item N |
| `-N` or `uncheck N` | Uncheck item N |
| `+N, M` or `check N, M` | Check multiple items |
| `-N-M` or `uncheck N-M` | Uncheck a range (e.g. `-3-7` unchecks 3,4,5,6,7) |
| `except N M` | Keep all currently checked except items N and M |
| `only N M` | Check only items N and M (uncheck everything else) |
| `all patch` | Check only patch-level updates |
| `all patch+minor` | Check all patch and minor updates |
| `all minor` | Check only minor-level updates |
| `all major` | Check only major-level updates |
| `all` | Check every item |
| `none` | Uncheck every item |
| `ok` / `proceed` | Confirm the current selection and move to summary |
| `reset` | Reset to defaults (patch ✅ minor ✅ major ☐) |

## Confirmation flow

After typing `ok`:

1. A final summary table is shown (checked items only):
   ```
   Will apply 5 update(s):
   ┌──────────────────────────────────────────────┬─────────┬─────────┬───────┐
   │ Dependency                                   │ Current │ Latest  │ Bump  │
   ├──────────────────────────────────────────────┼─────────┼─────────┼───────┤
   │ org.slf4j:slf4j-api                          │ 2.0.13  │ 2.0.17  │ patch │
   │ org.bouncycastle:bcprov-jdk18on              │ 1.80    │ 1.83    │ patch │
   │ com.hierynomus:sshj                          │ 0.38.0  │ 0.40.0  │ minor │
   └──────────────────────────────────────────────┴─────────┴─────────┴───────┘
   ```
2. You must type **`yes`** (not just Enter) to proceed.
3. Typing `no`, `cancel`, or `reset` returns to the selection list.

## Risk guidance

| Emoji | Category | Guidance |
|-------|----------|----------|
| 🔴 | Security, Core Framework, Logging | Review changelogs before applying; test carefully |
| 🟠 | SSH, SMB, HTTP, Serialization, GCP, Data | Check for API changes |
| 🟡 | Testing, Observability, Jakarta EE | Low risk; usually safe to batch |
| 🟢 | Build plugins | Lowest risk; check Gradle compatibility |
| ⚪ | Unknown | Evaluate manually |

## Examples

```
# Check all patch + minor, plus major item 12:
all patch+minor
+12
ok

# Check only items 1, 2, 3:
only 1 2 3
ok

# Check all, then uncheck the two major HTTP updates:
all
-12-13
ok

# Start fresh:
reset
```
