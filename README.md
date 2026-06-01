# REIA Chapter Writer

Produces finished prose from chapter outlines via the Claude API.

---

## Setup

**1. Install the dependency**

```
pip install anthropic
```

**2. Add your API key**

Create a file called `.env` in the same folder as `reia_writer.py` and put this in it:

```
ANTHROPIC_API_KEY=your_key_here
```

Replace `your_key_here` with your actual key. No quotes needed. That's it.

**3. Place these files in the same directory**

```
reia_writer.py
REIA_Production_Style_Guide.md
Final_chapter_outline_tagged.md
.env
```

---

## Usage

### Single chapter

```
python reia_writer.py --chapter 3
```

### Multiple specific chapters

```
python reia_writer.py --chapters 2,3,5
```

### Range

```
python reia_writer.py --chapters 2-7
```

### Mixed

```
python reia_writer.py --chapters 1,3-5,8
```

### List all available chapters

```
python reia_writer.py --list
```

### Preview without calling the API

```
python reia_writer.py --chapter 3 --dry-run
```

### Pass the API key directly (skips .env)

```
python reia_writer.py --chapter 3 --api-key sk-ant-...
```

---

## Options

| Flag | Default | Description |
|---|---|---|
| `--chapter N` | — | Single chapter ID |
| `--chapters STR` | — | Selection: `2`, `2,3`, `2-7`, `1,3-5,8` |
| `--api-key STR` | — | API key (overrides .env and environment variable) |
| `--style PATH` | `REIA_Production_Style_Guide.md` | Style guide file |
| `--outline PATH` | `Final_chapter_outline_tagged.md` | Chapter outline file |
| `--out-dir PATH` | `chapters_output/` | Output directory |
| `--list` | — | List available chapters and exit |
| `--skip-existing` | off | Skip chapters that already have output files |
| `--no-stream` | off | Silent mode — suppress live text output |
| `--delay N` | `3.0` | Seconds between API calls in bulk mode |
| `--dry-run` | off | Show what would be sent; no API calls made |

---

## Output

Each chapter is saved as a markdown file:

```
chapters_output/
    Chapter_002_The_Rising_Son.md
    Chapter_003_The_Indiscretion.md
    ...
```

---

## Bulk production tips

- Each chapter streams to the terminal as it is written. Use `--no-stream` for silent background runs.
- A 3-second delay between calls is the default. Increase with `--delay N` on long bulk runs.
- If a bulk run is interrupted, rerun with `--skip-existing` to pick up where it stopped.
- Rate limit errors trigger an automatic 60-second wait before moving on.

---

## Recommended first run

```
# Confirm chapters loaded correctly
python reia_writer.py --list

# Preview chapter 3 before spending tokens
python reia_writer.py --chapter 3 --dry-run

# Write chapter 3
python reia_writer.py --chapter 3

# Once that looks right, run Act II
python reia_writer.py --chapters 2-13 --skip-existing
```