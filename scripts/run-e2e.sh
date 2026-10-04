#!/usr/bin/env bash
# Real-game end-to-end verdicts from scbw (IDEA-E2E-TESTS Stage 1).
#
# Usage:
#   bash scripts/run-e2e.sh --bot-jar <jar> [--opponent <name>] [--map <name>]
#                            [--frames N] [--games-dir <dir>] [--keep N]
#   bash scripts/run-e2e.sh --parse-only [--games-dir <dir>] [--out <dir>]
#   bash scripts/run-e2e.sh --self-test
#
# What it does, and why it is split in two:
#
#   1. PLAY (needs scbw + the bot container). Runs `scbw run` for the requested
#      opponent/map pair. This half cannot run in this workspace: scbw lives in a
#      Docker image (~/.scbw/docker) and needs a licensed StarCraft install, so it
#      is the owner's side. If scbw is not on PATH the script says so and stops -
#      it does not pretend to have played anything.
#
#   2. PARSE (works anywhere, and is the part this repository can verify).
#      Reads <games-dir>/GAME_*/result.json plus each game's bot log, and writes
#      a verdict table into _AI/e2e/. Reads only: nothing is written outside the
#      workspace, and no other part of the games tree is touched.
#
# Why the table is the deliverable and not a pass/fail: a real game against a
# real opponent is a signal, not a gate (IDEA-E2E-TESTS §9 - no per-commit
# full-game gates). The first table is a baseline to compare against, so the file
# records the numbers, the date and the versions next to them.
#
# The result.json schema was verified against real scbw runs on 2026-10-04
# (GAME_08C2DF7E and later): keys are headless, game_name, map_name, game_type,
# game_speed, seed_override, timeout, timeout_at_frame, hide_names,
# drop_players, allow_input, auto_launch, random_names, game_dir, bot_dir,
# map_dir, bwapi_data_bwta_dir, bwapi_data_bwta2_dir, vnc_base_port, vnc_host,
# capture_movement, docker_image, nano_cpus, mem_limit, read_overwrite, bots
# (list like ["AtlantisP:P", "Marine Hell:T"]), is_crashed, is_gametime_outed,
# is_realtime_outed, game_time, winner, loser, winner_race, loser_race.
# Frame-capped games end with winner=null plus is_gametime_outed=true (scbw
# exits 1, "Game has gametime outed!") - that is a finished test game, not a
# crash. Bot logs live in logs_0/bot.log and logs_1/bot.log under the game
# dir, not next to result.json. A game with none of the recognised keys is
# still reported as schema-unknown, not guessed at.
set -euo pipefail

cd "$(dirname "$0")/.."

BOT_JAR=""
OPPONENT=""
MAP_NAME=""
FRAMES=""
GAMES_DIR="${HOME}/.scbw/games"
KEEP=10
MODE="play"
OUT_DIR="_AI/e2e"   # inside the workspace: nothing is written outside it (§8)

while [ $# -gt 0 ]; do
    case "$1" in
        --bot-jar) BOT_JAR="${2:?--bot-jar needs a path}"; shift 2 ;;
        --opponent) OPPONENT="${2:?--opponent needs a name}"; shift 2 ;;
        --map) MAP_NAME="${2:?--map needs a name}"; shift 2 ;;
        --frames) FRAMES="${2:?--frames needs a number}"; shift 2 ;;
        --games-dir) GAMES_DIR="${2:?--games-dir needs a path}"; shift 2 ;;
        --out) OUT_DIR="${2:?--out needs a path}"; shift 2 ;;
        --keep) KEEP="${2:?--keep needs a number}"; shift 2 ;;
        --parse-only) MODE="parse"; shift ;;
        --self-test) MODE="self-test"; shift ;;
        -h|--help) sed -n '2,30p' "$0"; exit 0 ;;
        *) echo "unknown option: $1" >&2; exit 1 ;;
    esac
done

# --------------------------------------------------------------- self-test ---
# The parser is the part that can be verified without a game, so it gets a test
# with a synthetic games tree: known verdicts in, known table out. Anything the
# parser gets wrong (a missing key, a crash it must notice, an unknown schema it
# must not invent a verdict for) fails here rather than in the first real table.
if [ "$MODE" = "self-test" ]; then
    WORK="$(mktemp -d)"
    trap 'rm -rf "$WORK"' EXIT

    mkdir -p "$WORK/games/GAME_WIN" "$WORK/games/GAME_LOSS" "$WORK/games/GAME_CRASH" "$WORK/games/GAME_ODD"
    cat > "$WORK/games/GAME_WIN/result.json" <<'JSON'
{"Result": "Win", "Frames": 12345, "Map": "Python", "OpponentName": "Steamhammer", "GameType": "Ladder"}
JSON
    cat > "$WORK/games/GAME_LOSS/result.json" <<'JSON'
{"Result": "Loss", "Frames": 5000, "Map": "Python", "OpponentName": "Steamhammer", "GameType": "Ladder"}
JSON
    cat > "$WORK/games/GAME_CRASH/result.json" <<'JSON'
{"Result": "NoResult", "Frames": 900, "Map": "Python", "OpponentName": "Steamhammer"}
JSON
    cat > "$WORK/games/GAME_CRASH/0.log" <<'LOG'
java.lang.NullPointerException: something nobody caught
	at atlantis.units.AUnit.doThing(AUnit.java:1)
LOG
    # Keys this parser does not know: the verdict must not be invented for it.
    cat > "$WORK/games/GAME_ODD/result.json" <<'JSON'
{"outcome": "weird-new-key", "turns": 42}
JSON
    # Real scbw shape (keys verified 2026-10-04): winner null, frame limit hit,
    # an exception trace in one side's log.
    mkdir -p "$WORK/games/GAME_REAL/logs_0" "$WORK/games/GAME_REAL/logs_1"
    cat > "$WORK/games/GAME_REAL/result.json" <<'JSON'
{"bots": ["AtlantisP:P", "Marine Hell:T"], "map_name": "sscai/(3)TauCross.scx", "timeout_at_frame": 5000, "is_crashed": null, "is_gametime_outed": true, "is_realtime_outed": false, "game_time": 31.6, "winner": null, "loser": null}
JSON
    cat > "$WORK/games/GAME_REAL/logs_0/bot.log" <<'LOG'
java.lang.NullPointerException
	at atlantis.map.choke.DefineMainChoke.defineFromMainBaseRegion(DefineMainChoke.java:38)
LOG
    echo "clean start, no exceptions here" > "$WORK/games/GAME_REAL/logs_1/bot.log"

    bash "$0" --parse-only --games-dir "$WORK/games" --out "$WORK/out" --keep 10

    table="$(find "$WORK/out" -name 'scbw-*.md' | head -1)"
    [ -n "$table" ] || { echo "self-test FAILED: the parser wrote no table"; exit 1; }

    expect() {
        grep -q -- "$2" "$table" \
            || { echo "self-test FAILED: table does not contain '$2'"; cat "$table"; exit 1; }
    }
    expect "$WORK/table.md" "| GAME_WIN | Win |" "a Win verdict is read as Win"
    expect "$WORK/table.md" "| GAME_LOSS | Loss |" "a Loss verdict is read as Loss"
    expect "$WORK/table.md" "CRASH" "an exception in the log is noticed"
    expect "$WORK/table.md" "schema-unknown" "an unrecognised schema is not guessed at"
    expect "$WORK/table.md" "12345" "the frame count is read"
    expect "$WORK/table.md" "Steamhammer" "the opponent is read"
    expect "$WORK/table.md" "gametime-out" "a frame-capped real game is not a crash"
    expect "$WORK/table.md" "GAME_REAL" "the real-shape game is parsed"

    echo "OK: parser self-test passed (4 synthetic games: win, loss, crash, unknown schema)"
    exit 0
fi

# --------------------------------------------------------------------- play ---
if [ "$MODE" = "play" ]; then
    [ -n "$BOT_JAR" ] || { echo "--bot-jar is required unless --parse-only" >&2; exit 1; }
    [ -f "$BOT_JAR" ] || { echo "no such jar: $BOT_JAR" >&2; exit 1; }
    command -v scbw >/dev/null 2>&1 || {
        cat >&2 <<'MSG'
scbw is not on PATH, so no game can be played from here.

scbw runs StarCraft in a container (~/.scbw/docker/game.dockerfile is the image
recipe), which needs the licensed game and the container runtime - both outside
this workspace, and both the owner's side. Two ways forward:

  * run scbw yourself with the jar this repository builds
    (bash scripts/build-bot-jar.sh bots/AtlantisP/AI/Atlantis.jar), then re-run this
    script with --parse-only to turn the games it left in ~/.scbw/games into a
    verdict table;
  * or point --games-dir at a copy of those games inside the workspace.

Nothing was played and nothing was written.
MSG
        exit 2
    }

    SCBW_ARGS=(--game-type "$([ -n "$MAP_NAME" ] && echo FFA || echo Ladder)"
               --bot1 race="$BOT_JAR")
    [ -n "$OPPONENT" ] && SCBW_ARGS+=(--bot2 race="$OPPONENT")
    [ -n "$MAP_NAME" ] && SCBW_ARGS+=(--map "$MAP_NAME")
    [ -n "$FRAMES" ] && SCBW_ARGS+=(--game-length "$FRAMES")
    echo "running: scbw run ${SCBW_ARGS[*]}"
    scbw run "${SCBW_ARGS[@]}"
fi

# -------------------------------------------------------------------- parse ---
python3 - "$GAMES_DIR" "$OUT_DIR" "$KEEP" <<'PY'
import json, os, re, sys, datetime

games_dir, out_dir, keep = sys.argv[1], sys.argv[2], int(sys.argv[3])

# Keys scbw's result.json uses, verified against real runs 2026-10-04 (see the
# header). The legacy spellings stay so the self-test's synthetic games keep
# exercising the tolerant path; a game with none of these keys is reported,
# not interpreted.
VERDICT_KEYS = ('Result', 'result', 'verdict', 'winner')
FRAMES_KEYS = ('Frames', 'frames', 'FramesPlayed', 'frames_played',
               'game_time', 'timeout_at_frame')
OPPONENT_KEYS = ('OpponentName', 'opponent', 'Opponent', 'bots')
MAP_KEYS = ('Map', 'map', 'MapName', 'map_name')

def first(d, keys):
    for k in keys:
        if k in d:
            return d[k]
    return None

def bot_logs(game_dir):
    """All bot logs scbw left: logs_0/bot.log, logs_1/bot.log (real layout),
    plus any *.log directly in the game dir (legacy/synthetic)."""
    found = []
    for root, _, files in os.walk(game_dir):
        for name in sorted(files):
            if name.endswith('.log'):
                path = os.path.join(root, name)
                rel = os.path.relpath(path, game_dir)
                try:
                    with open(path, 'r', errors='replace') as f:
                        found.append((rel, f.read()))
                except OSError:
                    continue
    return found

CRASH = re.compile(r'^\s*(java\.lang\.\w+Exception|Exception in thread|\tat atlantis\.)', re.M)

rows = []
if os.path.isdir(games_dir):
    for game in sorted(os.listdir(games_dir)):
        game_dir = os.path.join(games_dir, game)
        result_path = os.path.join(game_dir, 'result.json')
        if not os.path.isdir(game_dir) or not os.path.isfile(result_path):
            continue
        try:
            with open(result_path) as f:
                data = json.load(f)
        except (OSError, ValueError) as exc:
            rows.append((game, 'unreadable result.json (%s)' % exc.__class__.__name__,
                         '', '', '', sorted(os.listdir(game_dir))))
            continue

        logs = bot_logs(game_dir)
        crashed_in = sorted(rel for rel, content in logs if CRASH.search(content))
        verdict = first(data, VERDICT_KEYS)
        if verdict is None and not any(k in data for k in
                VERDICT_KEYS + FRAMES_KEYS + OPPONENT_KEYS + MAP_KEYS
                + ('is_crashed', 'is_gametime_outed', 'is_realtime_outed',
                   'bots', 'game_time')):
            verdict = 'schema-unknown'          # never guess
        elif verdict is None and data.get('is_crashed'):
            verdict = 'crashed'
        elif verdict is None and data.get('is_gametime_outed'):
            verdict = 'gametime-out (frame limit), undecided'
        elif verdict is None and data.get('is_realtime_outed'):
            verdict = 'realtime-out'
        elif verdict is None:
            verdict = 'undecided (%s)' % ','.join(
                k for k in ('winner', 'is_crashed', 'is_gametime_outed')
                if k in data)
        elif verdict in ('NoResult', 'Error', 'Disconnect'):
            verdict = '%s (no verdict)' % verdict
        if crashed_in:
            verdict = '%s + CRASH in %s' % (verdict, ','.join(crashed_in))

        rows.append((game, str(verdict), str(first(data, FRAMES_KEYS) or ''),
                     str(first(data, OPPONENT_KEYS) or ''), str(first(data, MAP_KEYS) or ''),
                     sorted(data.keys())))

os.makedirs(out_dir, exist_ok=True)
stamp = datetime.datetime.now().strftime('%Y-%m-%d_%H%M%S')
out_path = os.path.join(out_dir, 'scbw-%s.md' % stamp)

with open(out_path, 'w') as f:
    f.write('# scbw verdicts - %s\n\n' % stamp)
    f.write('Source: `%s` (read-only, CONVENTIONS §8).\n\n' % games_dir)
    if not rows:
        f.write('No `GAME_*` directory with a `result.json` was found.\n\n')
        f.write('That is what an empty games tree looks like, and it is not a pass: '
                'no game was played.\n')
    else:
        f.write('| game | verdict | frames | opponent | map |\n')
        f.write('|---|---|---|---|---|\n')
        for game, verdict, frames, opponent, map_name, _ in rows:
            f.write('| %s | %s | %s | %s | %s |\n' % (game, verdict, frames, opponent, map_name))
        f.write('\n## Keys seen in result.json\n\n')
        for game, _, _, _, _, keys in rows:
            f.write('- `%s`: %s\n' % (game, ', '.join('`%s`' % k for k in keys) or '(none)'))

    # Retention: the replays are the expensive part, and they live outside the
    # workspace, so this only reports what is there - deleting someone else's
    # game history is not this script's call.
    if os.path.isdir(games_dir):
        replays = [g for g in sorted(os.listdir(games_dir))
                   if os.path.isdir(os.path.join(games_dir, g))]
        f.write('\n## Retention\n\n')
        f.write('%d game directories under `%s`, policy is to keep the last %d. '
                'Nothing was deleted: those directories are the owner\'s history, '
                'not a build artefact.\n' % (len(replays), games_dir, keep))

print('OK: %s (%d games)' % (out_path, len(rows)))
PY