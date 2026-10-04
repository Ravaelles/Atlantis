# `_AI/e2e/` — real-game verdicts

One markdown table per run of `scripts/run-e2e.sh --parse-only`, each reading the
`GAME_*/result.json` of the games scbw left in `~/.scbw/games` (read-only, and
only there — CONVENTIONS §8).

**These files are a signal, not a gate.** A real game against a real opponent is
not a merge condition: opponents and maps drift between runs, and
`_AI/IDEA-E2E-TESTS.md` §9 says so explicitly. What a table is *for* is being the
baseline the next one is compared against — win/loss, frames played, whether the
log shows an exception, plus the keys the parser actually saw in `result.json`.

Two rules for the parser's honesty:

- A game whose `result.json` uses keys the parser does not recognise is written as
  `schema-unknown`, not guessed at. The schema is not verified yet (this repo has
  never read a real one), so the first real table rewrites the key lists in the
  script's header with what actually appeared.
- An empty table says so in words: "no game was played", not "no failures".

There is no table here yet — the tier has not been run against a real scbw
install. `scripts/run-e2e.sh --self-test` is what this repository can verify on
its own (four synthetic games: a win, a loss, a crash in the log, and an unknown
schema).