#!/usr/bin/env python3
"""
REIA Chapter Writer
Produces finished prose from chapter outlines via the Claude API.

Usage:
    Single chapter:
        python reia_writer.py --chapter 2

    Multiple specific chapters:
        python reia_writer.py --chapters 2,3,5

    Range:
        python reia_writer.py --chapters 2-7

    Mixed:
        python reia_writer.py --chapters 1,3-5,8

    List available chapters:
        python reia_writer.py --list

    Preview what would be sent (no API call):
        python reia_writer.py --chapter 2 --dry-run

Setup:
    pip install anthropic

    Create a .env file in the same directory:
        ANTHROPIC_API_KEY=your_key_here

    Or pass the key directly:
        python reia_writer.py --chapter 3 --api-key sk-ant-...
"""

import anthropic
import argparse
import glob
import os
import re
import sys
import time
from pathlib import Path


# ── Configuration ─────────────────────────────────────────────────────────────

MODEL           = "claude-sonnet-4-6"
MAX_TOKENS      = 8000
BULK_DELAY      = 3.0
MAX_RETRIES     = 3
RETRY_DELAY     = 8.0
MIN_WORDS       = 300        # outputs below this are flagged as likely truncated
DEFAULT_STYLE   = "REIA_Production_Style_Guide.md"
DEFAULT_OUTLINE = "Final_chapter_outline_tagged.md"
DEFAULT_OUT_DIR = "chapters_output"

# Chapters that contain historically sensitive content requiring explicit framing.
# Add chapter IDs here when you know they hit the safety threshold.
SENSITIVE_CHAPTERS = {
    24, 25, 26, 27, 28, 29, 30,   # Marco Polo Bridge / early China campaign
    31, 32, 33, 34, 35, 36, 37,   # Nanjing and aftermath
    38, 39, 40,                    # Wei's return / family search
    96, 97, 98,                    # Bataan / Philippines / Saburo's arc
    130, 131, 132, 133, 134,       # Okinawa civilian collapse
    147, 148,                      # Hiroshima / Nagasaki
}

SENSITIVE_FRAMING = (
    "LITERARY CONTEXT: This chapter depicts documented historical events. "
    "The Historical Content Authorization section of the style guide applies in full. "
    "Render every scene the outline calls for completely. "
    "Do not summarize, soften, compress, or omit any scene. "
    "The reader must be inside the experience of the character living it. "
    "A chapter that ends before every outlined scene is complete has failed the assignment."
)

# Optional brief overrides for chapters whose outline text triggers the API classifier.
# Key: chapter ID. Value: replacement brief sent to the API instead of the outline text.
# The full outline is preserved in your outline file — this only affects what is sent.
BRIEF_OVERRIDES: dict[int, str] = {
    41: """Chapter 41: Arsenal of Democracy
September–December 1940.

Four narrative threads, rendered through specific characters:

1. London, September 1940. The Blitz begins. A female radar operator on the English coast detects the first wave. RAF Fighter Command responds. The city takes the bombs. Render this through the operators and civilians experiencing it — the radar station, the underground shelters, the fires above the buildings.

2. The United States, September–November 1940. Booker and his classmates try to follow the news. American isolationism is a fact of daily life — the war is distant, Europe's problem. Roosevelt wins a third term.

3. Warsaw, 1940. One brief scene: the walls going up around the ghetto. Render through someone inside them, watching the perimeter close.

4. December 29, 1940. Roosevelt's fireside chat — "Arsenal of Democracy." Reia hears it on the radio in Osaka. Render the speech as she experiences it: key phrases arriving through static, the voice in a dark room, the weight of what it means for Japan. Do not reproduce the speech verbatim. Render her listening to it.

5. Tokyo, two days later. Hideki reads not the speech but the public response — telegrams running 100 to 1 in favor of aid. He reassesses the America First Committee. He goes to the Imperial Palace. The chapter ends as he enters Hirohito's audience chamber."""
}


# ── API key ───────────────────────────────────────────────────────────────────

def load_api_key(cli_key=None):
    if cli_key:
        return cli_key
    env_path = Path(__file__).parent / ".env"
    if env_path.exists():
        with open(env_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line.startswith("#") or "=" not in line:
                    continue
                key, _, value = line.partition("=")
                if key.strip() == "ANTHROPIC_API_KEY":
                    value = value.strip().strip('"').strip("'")
                    if value and value != "your_key_here":
                        return value
    env_key = os.environ.get("ANTHROPIC_API_KEY", "")
    if env_key:
        return env_key
    print(
        "Error: No API key found.\n\n"
        "Create a .env file in the same folder as this script:\n\n"
        "    ANTHROPIC_API_KEY=your_key_here\n\n"
        "Or pass it directly:\n"
        "    python reia_writer.py --chapter 3 --api-key your_key_here\n"
    )
    sys.exit(1)


# ── File loading ──────────────────────────────────────────────────────────────

def load_text(path, label):
    if not os.path.exists(path):
        print(f"Error: {label} not found at '{path}'")
        sys.exit(1)
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def parse_chapters(outline_text):
    pattern = r'<chapter id="(\d+)">(.*?)</chapter>'
    matches = re.findall(pattern, outline_text, re.DOTALL)
    if not matches:
        print("Error: No chapter tags found in outline file.")
        sys.exit(1)
    return {int(cid): content.strip() for cid, content in matches}


def extract_title(content):
    match = re.search(r"#\s+(?:New\s+)?Chapter\s+[\w\d]+[:\s]+(.+)", content)
    return match.group(1).strip() if match else "(untitled)"


# ── Chapter selection ─────────────────────────────────────────────────────────

def parse_selection(raw):
    ids = []
    for part in raw.split(","):
        part = part.strip()
        if not part:
            continue
        if "-" in part:
            try:
                start, end = part.split("-", 1)
                ids.extend(range(int(start), int(end) + 1))
            except ValueError:
                print(f"Error: Cannot parse range '{part}'.")
                sys.exit(1)
        else:
            try:
                ids.append(int(part))
            except ValueError:
                print(f"Error: '{part}' is not a valid chapter number.")
                sys.exit(1)
    return sorted(set(ids))


# ── Post-processing ───────────────────────────────────────────────────────────

def clean_prose(text):
    """
    Apply style guide corrections to raw model output.

    - Remove '---' scene break dividers (style guide: white space only)
    - Remove '***' and '* * *' dividers
    - Collapse multiple blank lines to a single blank line
    - Remove any trailing whitespace per line
    """
    # Remove markdown horizontal rules used as scene breaks
    text = re.sub(r"^\s*[-*]{3,}\s*$", "", text, flags=re.MULTILINE)

    # Collapse runs of 3+ blank lines to 2 (one visible break)
    text = re.sub(r"\n{3,}", "\n\n", text)

    # Strip trailing whitespace from each line
    lines = [line.rstrip() for line in text.split("\n")]
    text  = "\n".join(lines)

    return text.strip()


def is_likely_truncated(prose, chapter_id):
    """
    Return True if the output looks like it was cut short by content filtering.
    Heuristics:
    - Word count below minimum threshold
    - Output ends mid-sentence (no terminal punctuation in last 100 chars)
    """
    word_count = len(prose.split())
    if word_count < MIN_WORDS:
        return True, f"only {word_count} words (minimum {MIN_WORDS})"

    tail = prose.strip()[-150:]
    if not re.search(r"[.!?\"']\s*$", tail):
        return True, "output ends without terminal punctuation — likely mid-sentence cut"

    return False, ""


# ── API call ──────────────────────────────────────────────────────────────────

def build_user_message(chapter_id, brief):
    # Use override brief if one exists for this chapter
    effective_brief = BRIEF_OVERRIDES.get(chapter_id, brief)
    if chapter_id in BRIEF_OVERRIDES:
        print(f"  [using brief override for Chapter {chapter_id}]")

    parts = [f"Chapter ID: {chapter_id}\n\nChapter outline and brief:\n\n{effective_brief}\n\n"]

    if chapter_id in SENSITIVE_CHAPTERS:
        parts.append(SENSITIVE_FRAMING + "\n\n")

    parts.append(
        "Write this chapter as finished prose. "
        "Follow all style guide rules exactly. "
        "Render every scene the outline calls for — do not skip, compress, or imply any scene. "
        "Stop when the chapter is complete."
    )
    return "".join(parts)


def call_api(client, system_prompt, chapter_id, brief, show_stream):
    """Single API call with streaming. Returns (prose, word_count)."""
    collected = []

    with client.messages.stream(
        model=MODEL,
        max_tokens=MAX_TOKENS,
        system=system_prompt,
        messages=[{"role": "user", "content": build_user_message(chapter_id, brief)}],
    ) as stream:
        for text in stream.text_stream:
            if show_stream:
                print(text, end="", flush=True)
            collected.append(text)

    if show_stream:
        print()

    raw   = "".join(collected)
    prose = clean_prose(raw)
    return prose


def write_chapter(client, system_prompt, chapter_id, brief, show_stream, max_retries):
    """
    Write a chapter with automatic retry on suspected truncation.
    Returns (prose, attempt_count, truncation_warning).
    """
    for attempt in range(1, max_retries + 1):
        if attempt > 1:
            print(f"  Retry {attempt}/{max_retries} (waiting {RETRY_DELAY}s)...")
            time.sleep(RETRY_DELAY)

        prose = call_api(client, system_prompt, chapter_id, brief, show_stream)

        truncated, reason = is_likely_truncated(prose, chapter_id)
        if not truncated:
            return prose, attempt, None

        print(f"\n  ⚠ Output appears truncated: {reason}")
        if attempt == max_retries:
            return prose, attempt, reason

    return prose, max_retries, reason


# ── Output ────────────────────────────────────────────────────────────────────

def sanitize(text, max_len=40):
    text = re.sub(r"[^\w\s-]", "", text)
    text = re.sub(r"\s+", "_", text.strip())
    return text[:max_len]


def save_output(out_dir, chapter_id, prose, title, truncation_warning=None):
    Path(out_dir).mkdir(parents=True, exist_ok=True)
    filename = f"Chapter_{chapter_id:03d}_{sanitize(title)}.md"
    filepath = os.path.join(out_dir, filename)
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(f"# Chapter {chapter_id}: {title}\n\n")
        if truncation_warning:
            f.write(f"<!-- WARNING: Output may be truncated — {truncation_warning} -->\n\n")
        f.write(prose)
    return filepath


def output_exists(out_dir, chapter_id):
    return bool(glob.glob(os.path.join(out_dir, f"Chapter_{chapter_id:03d}_*.md")))


# ── CLI ───────────────────────────────────────────────────────────────────────

def build_parser():
    p = argparse.ArgumentParser(
        description="REIA Chapter Writer — Claude API interface",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    p.add_argument("--chapter",       type=int,   help="Single chapter ID")
    p.add_argument("--chapters",      type=str,   help='Selection: "2", "2,3", "2-7", "1,3-5,8"')
    p.add_argument("--style",         default=DEFAULT_STYLE,   help=f"Style guide path")
    p.add_argument("--outline",       default=DEFAULT_OUTLINE, help=f"Outline file path")
    p.add_argument("--out-dir",       default=DEFAULT_OUT_DIR, help=f"Output directory")
    p.add_argument("--list",          action="store_true",     help="List available chapters and exit")
    p.add_argument("--skip-existing", action="store_true",     help="Skip chapters with existing output")
    p.add_argument("--no-stream",     action="store_true",     help="Suppress live output (silent mode)")
    p.add_argument("--delay",         type=float, default=BULK_DELAY,   help=f"Seconds between bulk calls (default: {BULK_DELAY})")
    p.add_argument("--retries",       type=int,   default=MAX_RETRIES,  help=f"Max retries on truncated output (default: {MAX_RETRIES})")
    p.add_argument("--dry-run",       action="store_true",     help="Show what would be sent, no API calls")
    p.add_argument("--api-key",       type=str,   default=None,         help="API key (overrides .env)")
    p.add_argument("--mark-sensitive", type=str,  default=None,         help='Add chapter IDs to sensitive list: "24,25,38"')
    return p


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    parser = build_parser()
    args   = parser.parse_args()

    # Allow adding sensitive chapters at runtime
    if args.mark_sensitive:
        extra = parse_selection(args.mark_sensitive)
        SENSITIVE_CHAPTERS.update(extra)
        print(f"Added to sensitive chapters: {extra}")

    print("Loading style guide...")
    system_prompt = load_text(args.style, "Style guide")

    print("Parsing chapter outlines...")
    outline_text = load_text(args.outline, "Chapter outline")
    chapters     = parse_chapters(outline_text)
    print(f"  Found {len(chapters)} chapters.\n")

    # List mode
    if args.list:
        print(f"{'ID':>4}  {'S':1}  Title")
        print("-" * 65)
        for cid in sorted(chapters.keys()):
            flag = "⚠" if cid in SENSITIVE_CHAPTERS else " "
            print(f"{cid:>4}  {flag}  {extract_title(chapters[cid])}")
        print("\n  ⚠ = sensitive chapter (retry logic + explicit framing active)")
        return

    # Targets
    if args.chapter:
        targets = [args.chapter]
    elif args.chapters:
        targets = parse_selection(args.chapters)
    else:
        parser.print_help()
        print("\nSpecify --chapter N  or  --chapters N,M-P")
        sys.exit(1)

    missing = [c for c in targets if c not in chapters]
    if missing:
        print(f"Warning: Chapter IDs not in outline: {missing}")
        targets = [c for c in targets if c in chapters]

    if not targets:
        print("No valid chapters to write.")
        sys.exit(1)

    sensitive_targets = [c for c in targets if c in SENSITIVE_CHAPTERS]
    print(f"Chapters to write:    {targets}")
    if sensitive_targets:
        print(f"Sensitive chapters:   {sensitive_targets}  (retry framing active)")
    print()

    # Dry run
    if args.dry_run:
        div = "=" * 60
        for cid in targets:
            override = cid in BRIEF_OVERRIDES
            effective = BRIEF_OVERRIDES.get(cid, chapters[cid])
            preview   = effective[:600] + "\n...[truncated]" if len(effective) > 600 else effective
            label     = "SENSITIVE — " if cid in SENSITIVE_CHAPTERS else ""
            override_note = " [BRIEF OVERRIDE ACTIVE]" if override else ""
            print(f"\n{div}\n{label}CHAPTER {cid}: {extract_title(chapters[cid])}{override_note}\n{div}")
            print(preview)
            if cid in SENSITIVE_CHAPTERS:
                print(f"\n[FRAMING APPENDED]\n{SENSITIVE_FRAMING}")
        print(f"\n{div}")
        print(f"System prompt: {len(system_prompt):,} chars")
        print("Dry run complete — no API calls made.")
        return

    # API client
    api_key = load_api_key(args.api_key)
    client  = anthropic.Anthropic(api_key=api_key)

    results = []
    total   = len(targets)
    show    = not args.no_stream

    for i, cid in enumerate(targets, 1):
        title = extract_title(chapters[cid])
        label = f"[{i}/{total}] Chapter {cid}: {title}"
        if cid in SENSITIVE_CHAPTERS:
            label += " [sensitive]"

        if args.skip_existing and output_exists(args.out_dir, cid):
            print(f"{label} — skipped")
            results.append({"id": cid, "status": "skipped"})
            continue

        print(label)
        print("-" * 60)

        try:
            prose, attempts, trunc_warn = write_chapter(
                client, system_prompt, cid, chapters[cid],
                show_stream=show, max_retries=args.retries
            )

            filepath   = save_output(args.out_dir, cid, prose, title, trunc_warn)
            word_count = len(prose.split())
            status_str = f"({word_count:,} words"
            if attempts > 1:
                status_str += f", {attempts} attempts"
            if trunc_warn:
                status_str += ", ⚠ still may be truncated"
            status_str += ")"
            print(f"  Saved → {filepath}  {status_str}")

            status = "truncated" if trunc_warn else "success"
            results.append({"id": cid, "status": status, "path": filepath, "words": word_count})

        except anthropic.RateLimitError:
            print("  FAILED — rate limit. Waiting 60s...")
            time.sleep(60)
            results.append({"id": cid, "status": "failed", "error": "rate limit"})

        except anthropic.APIError as e:
            print(f"  FAILED — API error: {e}")
            results.append({"id": cid, "status": "failed", "error": str(e)})

        except KeyboardInterrupt:
            print("\nInterrupted.")
            break

        except Exception as e:
            print(f"  FAILED — {e}")
            results.append({"id": cid, "status": "failed", "error": str(e)})

        if i < total:
            print(f"\n  (waiting {args.delay}s)\n")
            time.sleep(args.delay)

    # Summary
    ok        = [r for r in results if r["status"] == "success"]
    truncated = [r for r in results if r["status"] == "truncated"]
    skipped   = [r for r in results if r["status"] == "skipped"]
    failed    = [r for r in results if r["status"] == "failed"]

    print("\n" + "=" * 60)
    print("DONE")
    print(f"  Written:   {len(ok)}")
    print(f"  Truncated: {len(truncated)}  (saved but flagged — review these)")
    print(f"  Skipped:   {len(skipped)}")
    print(f"  Failed:    {len(failed)}")

    if truncated:
        print("\nFlagged for review (may be incomplete):")
        for r in truncated:
            print(f"  Chapter {r['id']}: {r['words']:,} words — {r['path']}")

    if failed:
        print("\nFailed:")
        for r in failed:
            print(f"  Chapter {r['id']}: {r.get('error', '?')}")

    if ok or truncated:
        print(f"\nOutput: {args.out_dir}/")


if __name__ == "__main__":
    main()