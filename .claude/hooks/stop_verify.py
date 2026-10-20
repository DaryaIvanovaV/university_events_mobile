"""Stop hook: compile Kotlin and run unit tests when .kt/.kts files changed.

Exit 0 -> let Claude stop. Exit 2 -> block the stop and send the error tail to Claude.
"""
import json
import os
import subprocess
import sys

MAX_ERR_CHARS = 4000
DEFAULT_JAVA_HOME = r"C:\Program Files\Android\Android Studio\jbr"
GRADLE_ARGS = [":app:compileDebugKotlin", "testDebugUnitTest", "--console=plain", "-q"]


def changed_kotlin_files(project_dir):
    out = subprocess.run(
        ["git", "status", "--porcelain", "--untracked-files=all"],
        cwd=project_dir, capture_output=True, text=True, encoding="utf-8", errors="replace",
    ).stdout
    paths = [line[3:].strip().strip('"') for line in out.splitlines() if len(line) > 3]
    # Rename entries look like "old -> new"; keep the new path.
    paths = [p.split(" -> ")[-1] for p in paths]
    return [p for p in paths if p.endswith((".kt", ".kts"))]


def main():
    try:
        data = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        data = {}
    if data.get("stop_hook_active"):
        return 0

    project_dir = os.environ.get("CLAUDE_PROJECT_DIR") or data.get("cwd") or os.getcwd()
    if not changed_kotlin_files(project_dir):
        return 0

    env = os.environ.copy()
    if not env.get("JAVA_HOME") and os.path.isdir(DEFAULT_JAVA_HOME):
        env["JAVA_HOME"] = DEFAULT_JAVA_HOME

    gradlew = os.path.join(project_dir, "gradlew.bat" if os.name == "nt" else "gradlew")
    proc = subprocess.run(
        [gradlew, *GRADLE_ARGS], cwd=project_dir, env=env,
        capture_output=True, text=True, encoding="utf-8", errors="replace",
    )
    if proc.returncode == 0:
        return 0

    log = (proc.stdout or "") + (proc.stderr or "")
    error_lines = [l for l in log.splitlines() if l.startswith("e:") or "error" in l.lower() or "FAILED" in l]
    report = "\n".join(error_lines) or log
    sys.stderr.write(
        f"Verification failed: gradlew {' '.join(GRADLE_ARGS)} (exit {proc.returncode})\n"
        + report[-MAX_ERR_CHARS:]
        + "\nFix the errors above before finishing.\n"
    )
    return 2


if __name__ == "__main__":
    sys.exit(main())
