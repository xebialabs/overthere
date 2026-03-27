#!/usr/bin/env python3
"""
execute-upgrade.py — Apply dependency upgrades to overthere
Reads dependency-upgrade-report.json, presents a selection UI, and patches
build.gradle in place.

Usage:
  python3 execute-upgrade.py --report dependency-upgrade-report.json --project-dir .
  python3 execute-upgrade.py --report dependency-upgrade-report.json --skip-build
  python3 execute-upgrade.py --report dependency-upgrade-report.json --auto-select all
  python3 execute-upgrade.py --report dependency-upgrade-report.json --auto-select 1,3,5
"""

import argparse
import json
import re
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional

# Windows: ensure stdout/stderr use UTF-8 so emoji and non-ASCII chars print correctly
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

IS_WINDOWS = sys.platform == "win32"

# Cross-platform gradlew: use gradlew.bat on Windows
BUILD_COMMAND = (
    ["gradlew.bat", "clean", "build"] if IS_WINDOWS else ["./gradlew", "clean", "build"]
)

BUILD_TIMEOUT = 600  # 10 minutes

# ---------------------------------------------------------------------------
# Colour helpers (ANSI — disabled on Windows if not supported)
# ---------------------------------------------------------------------------
_USE_COLOR = not IS_WINDOWS or (IS_WINDOWS and sys.stdout.isatty())


def _c(code: str, text: str) -> str:
    if _USE_COLOR:
        return f"\033[{code}m{text}\033[0m"
    return text


def red(t):    return _c("31", t)
def green(t):  return _c("32", t)
def yellow(t): return _c("33", t)
def cyan(t):   return _c("36", t)
def bold(t):   return _c("1", t)


# ---------------------------------------------------------------------------
# Backup helpers
# ---------------------------------------------------------------------------
def make_backup(path: Path) -> Path:
    ts = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
    backup = path.parent / f"{path.name}.backup.{ts}"
    shutil.copy2(str(path), str(backup))
    return backup


def restore_backup(backup: Path, original: Path):
    shutil.copy2(str(backup), str(original))
    print(f"  Restored: {original.name} from {backup.name}")


# ---------------------------------------------------------------------------
# File patchers
# ---------------------------------------------------------------------------
def patch_gradle_dep(
    path: Path, group: str, artifact: str, old_ver: str, new_ver: str
) -> bool:
    """Replace group:artifact:OLD → group:artifact:NEW in a Gradle file."""
    text = path.read_text(encoding="utf-8", errors="replace")
    old_coord = f"{group}:{artifact}:{old_ver}"
    new_coord = f"{group}:{artifact}:{new_ver}"
    if old_coord not in text:
        # Try with quotes
        for q in ("'", '"'):
            if f"{q}{old_coord}{q}" in text:
                text = text.replace(f"{q}{old_coord}{q}", f"{q}{new_coord}{q}")
                path.write_text(text, encoding="utf-8")
                return True
        return False
    text = text.replace(old_coord, new_coord)
    path.write_text(text, encoding="utf-8")
    return True


def patch_plugin_version(path: Path, plugin_id: str, old_ver: str, new_ver: str) -> bool:
    """Replace version on the line containing a plugin id declaration."""
    text = path.read_text(encoding="utf-8", errors="replace")
    # Pattern: id "some.plugin.id" version "OLD"
    pattern = re.compile(
        r"""(id\s+['"]""" + re.escape(plugin_id) + r"""['"]\s+version\s+['"])"""
        + re.escape(old_ver)
        + r"""(['"])""",
        re.MULTILINE,
    )
    new_text, count = pattern.subn(lambda m: m.group(1) + new_ver + m.group(2), text)
    if count == 0:
        return False
    path.write_text(new_text, encoding="utf-8")
    return True


def patch_properties_key(path: Path, key: str, old_ver: str, new_ver: str) -> bool:
    """Replace key=OLD → key=NEW in a .properties file."""
    text = path.read_text(encoding="utf-8", errors="replace")
    pattern = re.compile(
        r"^(" + re.escape(key) + r"\s*=\s*)" + re.escape(old_ver) + r"$",
        re.MULTILINE,
    )
    new_text, count = pattern.subn(lambda m: m.group(1) + new_ver, text)
    if count == 0:
        return False
    path.write_text(new_text, encoding="utf-8")
    return True


def patch_conf_key(path: Path, key: str, old_ver: str, new_ver: str) -> bool:
    """Replace key: "OLD" or key = "OLD" in a HOCON-style .conf file."""
    text = path.read_text(encoding="utf-8", errors="replace")
    pattern = re.compile(
        r"""(^[ \t]*""" + re.escape(key) + r"""[ \t]*[:=][ \t]*["']?)"""
        + re.escape(old_ver)
        + r"""(["']?)""",
        re.MULTILINE,
    )
    new_text, count = pattern.subn(lambda m: m.group(1) + new_ver + m.group(2), text)
    if count == 0:
        return False
    path.write_text(new_text, encoding="utf-8")
    return True


# ---------------------------------------------------------------------------
# Build runner
# ---------------------------------------------------------------------------
def run_build(project_dir: Path) -> bool:
    """Run ./gradlew clean build in project_dir. Returns True on success."""
    print(f"\n  Running: {' '.join(BUILD_COMMAND)}")
    try:
        result = subprocess.run(
            BUILD_COMMAND,
            cwd=project_dir,
            capture_output=True,
            text=True,
            timeout=BUILD_TIMEOUT,
            shell=IS_WINDOWS,  # .bat files require shell=True on Windows
        )
        if result.returncode == 0:
            print(green("  BUILD SUCCESS"))
            return True
        else:
            print(red("  BUILD FAILED"))
            print(result.stdout[-3000:] if result.stdout else "")
            print(result.stderr[-2000:] if result.stderr else "")
            return False
    except subprocess.TimeoutExpired:
        print(red(f"  BUILD TIMED OUT after {BUILD_TIMEOUT}s"))
        return False
    except FileNotFoundError:
        print(red(f"  BUILD COMMAND NOT FOUND: {BUILD_COMMAND[0]}"))
        return False


# ---------------------------------------------------------------------------
# Interactive selection
# ---------------------------------------------------------------------------
RISK_ORDER = {"🔴": 0, "🟠": 1, "🟡": 2, "🟢": 3, "⚪": 4}
BUMP_ORDER = {"major": 0, "minor": 1, "patch": 2, "unknown": 3}


def build_item_list(updates: list[dict]) -> list[dict]:
    """Flatten updates into a numbered list sorted by bump type, then risk."""
    items = sorted(
        updates,
        key=lambda u: (
            BUMP_ORDER.get(u.get("bump", "unknown"), 9),
            RISK_ORDER.get(u.get("risk", "⚪"), 9),
            u.get("group", ""),
        ),
    )
    # Assign numbers
    for i, item in enumerate(items, 1):
        item["_num"] = i
    return items


def default_checked(item: dict) -> bool:
    return item.get("bump") in ("patch", "minor")


def print_selection(items: list[dict], checked: set[int]):
    print()
    current_bump = None
    for item in items:
        bump = item.get("bump", "unknown")
        if bump != current_bump:
            current_bump = bump
            print(f"\n  {bold(bump.capitalize() + ' updates')}")
        num = item["_num"]
        mark = "[x]" if num in checked else "[ ]"
        coord = f"{item['group']}:{item['artifact']}"
        cur = item.get("current", "?")
        lat = item.get("latest", "?")
        risk = item.get("risk", "⚪")
        cat = item.get("category", "Unknown")
        print(f"  {mark} {num:>3}. {coord:<50} {cur:>12} → {lat:<12} {risk} {cat}")
    print()


def parse_toggle(cmd: str, items: list[dict], checked: set[int]) -> Optional[set[int]]:
    """Parse a toggle command and return updated checked set, or None if unrecognised."""
    cmd = cmd.strip().lower()
    all_nums = {i["_num"] for i in items}
    patch_nums = {i["_num"] for i in items if i.get("bump") == "patch"}
    minor_nums = {i["_num"] for i in items if i.get("bump") == "minor"}
    major_nums = {i["_num"] for i in items if i.get("bump") == "major"}

    if cmd in ("all",):
        return set(all_nums)
    if cmd == "none":
        return set()
    if cmd == "reset":
        return {i["_num"] for i in items if default_checked(i)}
    if cmd in ("all patch",):
        return set(patch_nums)
    if cmd in ("all minor",):
        return set(minor_nums)
    if cmd in ("all major",):
        return set(major_nums)
    if cmd in ("all patch+minor", "all minor+patch"):
        return patch_nums | minor_nums

    # +N or check N [, M ...]
    m = re.match(r"^(\+|check\s+)(.+)$", cmd)
    if m:
        for part in re.split(r"[,\s]+", m.group(2)):
            part = part.strip()
            if part.isdigit():
                checked.add(int(part))
        return checked

    # -N or uncheck N [, M ...] or N-M range
    m = re.match(r"^(-|uncheck\s+)(.+)$", cmd)
    if m:
        spec = m.group(2).strip()
        # Range: N-M
        r = re.match(r"^(\d+)-(\d+)$", spec)
        if r:
            for n in range(int(r.group(1)), int(r.group(2)) + 1):
                checked.discard(n)
        else:
            for part in re.split(r"[,\s]+", spec):
                if part.isdigit():
                    checked.discard(int(part))
        return checked

    # only N M ...
    m = re.match(r"^only\s+(.+)$", cmd)
    if m:
        new = set()
        for part in re.split(r"[,\s]+", m.group(1)):
            if part.isdigit():
                new.add(int(part))
        return new

    # except N M ...
    m = re.match(r"^except\s+(.+)$", cmd)
    if m:
        for part in re.split(r"[,\s]+", m.group(1)):
            if part.isdigit():
                checked.discard(int(part))
        return checked

    return None


def interactive_select(items: list[dict]) -> Optional[list[dict]]:
    """Interactive selection UI. Returns selected items, or None if user aborted."""
    checked = {i["_num"] for i in items if default_checked(i)}

    print(bold("\nSelect updates to apply (defaults: patch ✅ minor ✅ major ☐)"))
    print("Commands: +N/-N, all, none, all patch, all patch+minor, ok, reset, quit")
    print("See .github/skills/dependency-upgrade/references/selection-protocol.md for full reference")
    print_selection(items, checked)

    while True:
        try:
            cmd = input("  > ").strip()
        except (EOFError, KeyboardInterrupt):
            print()
            return None

        if not cmd:
            continue
        if cmd.lower() in ("ok", "proceed"):
            selected = [i for i in items if i["_num"] in checked]
            if not selected:
                print(yellow("  Nothing selected. Type 'all' to select all, or 'quit' to exit."))
                continue
            # Show confirmation table
            print()
            print(bold(f"Will apply {len(selected)} update(s):"))
            print(f"  {'Dependency':<52} {'Current':>12}  {'Latest':<12} {'Bump'}")
            print("  " + "-" * 90)
            for sel in selected:
                coord = f"{sel['group']}:{sel['artifact']}"
                print(
                    f"  {coord:<52} {sel.get('current','?'):>12}  {sel.get('latest','?'):<12} {sel.get('bump','?')}"
                )
            print()
            confirm = input(bold("  Type 'yes' to confirm, or anything else to go back: ")).strip().lower()
            if confirm == "yes":
                return selected
            else:
                print("  Returning to selection.")
                print_selection(items, checked)
                continue
        if cmd.lower() in ("quit", "exit", "abort"):
            return None

        result = parse_toggle(cmd, items, set(checked))
        if result is not None:
            checked = result
            print_selection(items, checked)
        else:
            print(yellow(f"  Unrecognised command: '{cmd}'"))


# ---------------------------------------------------------------------------
# Apply upgrades
# ---------------------------------------------------------------------------
def apply_update(update: dict, project_dir: Path) -> tuple[bool, str]:
    """
    Apply a single update to the relevant file.
    Returns (success, message).
    """
    group = update.get("group", "")
    artifact = update.get("artifact", "")
    old_ver = update.get("current", "")
    new_ver = update.get("latest", "")
    src = update.get("source_file", "build.gradle")
    scope = update.get("scope", "")

    file_path = project_dir / src
    if not file_path.exists():
        # Fallback to build.gradle
        file_path = project_dir / "build.gradle"

    if scope == "plugin":
        # Plugin block: id "group.artifact" version "OLD"
        ok = patch_plugin_version(file_path, group, old_ver, new_ver)
        if not ok:
            # Fall back to dep coordinate replacement
            ok = patch_gradle_dep(file_path, group, artifact, old_ver, new_ver)
    elif src.endswith(".properties"):
        # Try: treat as plugin/variable name key
        key = f"{group}:{artifact}"
        ok = patch_properties_key(file_path, key, old_ver, new_ver)
        if not ok:
            ok = patch_gradle_dep(file_path, group, artifact, old_ver, new_ver)
    elif src.endswith(".conf"):
        var_name = update.get("variable", "")
        ok = patch_conf_key(file_path, var_name, old_ver, new_ver) if var_name else False
    else:
        ok = patch_gradle_dep(file_path, group, artifact, old_ver, new_ver)

    if ok:
        return True, f"Updated {group}:{artifact} {old_ver} → {new_ver} in {file_path.name}"
    return False, f"Could not find {group}:{artifact}:{old_ver} in {file_path.name}"


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    parser = argparse.ArgumentParser(description="Apply dependency upgrades to overthere")
    parser.add_argument("--report", default="dependency-upgrade-report.json",
                        help="Path to dependency-upgrade-report.json")
    parser.add_argument("--project-dir", default=".",
                        help="Root directory of the overthere project")
    parser.add_argument("--skip-build", action="store_true",
                        help="Skip build verification (dry run)")
    parser.add_argument("--auto-select",
                        help="Non-interactive auto-select: 'all' or comma-separated item numbers (e.g. '1,3,5')")
    args = parser.parse_args()

    project_dir = Path(args.project_dir).resolve()
    report_path = Path(args.report)
    if not report_path.is_absolute():
        report_path = project_dir / report_path

    if not report_path.exists():
        print(red(f"Report not found: {report_path}"))
        print("Run scan-dependencies.py first.")
        sys.exit(1)

    with open(report_path, encoding="utf-8") as f:
        report = json.load(f)

    generated = report.get("generated", "unknown")
    print(bold(f"Dependency upgrade report: {report_path}"))
    print(f"Generated: {generated}")

    # Collect all updates
    all_updates: list[dict] = (
        report.get("xl_platform_updates", []) + report.get("repo_updates", [])
    )

    if not all_updates:
        print(green("\nNo updates to apply. All dependencies are up to date."))
        sys.exit(0)

    items = build_item_list(all_updates)

    # Selection
    if args.auto_select:
        if args.auto_select.lower() == "all":
            selected = items
        else:
            nums = {int(n.strip()) for n in args.auto_select.split(",") if n.strip().isdigit()}
            selected = [i for i in items if i["_num"] in nums]
        if selected:
            print(f"\nAuto-selected {len(selected)} update(s).")
        else:
            print(yellow("No items matched auto-select criteria."))
            sys.exit(0)
    else:
        selected = interactive_select(items)
        if selected is None:
            print("\nUpgrade cancelled.")
            sys.exit(0)

    # Collect unique files that will be modified → create backups
    files_to_modify: set[Path] = set()
    for upd in selected:
        src = upd.get("source_file", "build.gradle")
        p = project_dir / src
        if not p.exists():
            p = project_dir / "build.gradle"
        files_to_modify.add(p)

    backups: dict[Path, Path] = {}
    for fpath in sorted(files_to_modify):
        if fpath.exists():
            bp = make_backup(fpath)
            backups[fpath] = bp
            print(f"  Backup: {fpath.name} → {bp.name}")

    # Apply all selected updates
    applied = []
    failed = []
    print(f"\nApplying {len(selected)} update(s)...")
    for upd in selected:
        ok, msg = apply_update(upd, project_dir)
        if ok:
            print(f"  {green('✓')} {msg}")
            applied.append(upd)
        else:
            print(f"  {yellow('?')} {msg}")
            failed.append(upd)

    if failed:
        print(yellow(f"\n  {len(failed)} update(s) could not be applied automatically:"))
        for upd in failed:
            print(f"    - {upd['group']}:{upd['artifact']} ({upd['source_file']})")
        print("  These may need manual editing.")

    if not applied:
        print(red("\nNo updates were applied."))
        sys.exit(1)

    # Build verification
    if args.skip_build:
        print(yellow("\n  Skipping build verification (--skip-build)."))
        print(green(f"\n{len(applied)} update(s) applied successfully (build not verified)."))
        sys.exit(0)

    build_ok = run_build(project_dir)
    if build_ok:
        print(green(f"\n✅ {len(applied)} update(s) applied and build verified."))
        if failed:
            print(yellow(f"  {len(failed)} update(s) could not be applied — manual review needed."))
    else:
        print(red("\n❌ Build failed. Rolling back all changes..."))
        for fpath, bp in backups.items():
            restore_backup(bp, fpath)
        print(red("Rollback complete. No changes were kept."))
        sys.exit(1)


if __name__ == "__main__":
    main()
