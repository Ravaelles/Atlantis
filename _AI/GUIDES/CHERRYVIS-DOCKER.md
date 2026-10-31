# CherryVis Docker viewer (the setup we use)

Image/container: `cvis` on `:8770`, `/sc-ai/REPLAYS` mounted at `/my-replays`.
Upstream viewer code is cloned at build (TorchCraftAI `scripts/cherryvis` +
pinned OpenBW `4091cad` for the WASM matcher) - so a viewer fix must live in
the repo, not only in the running container.

Links:

- image repo (ours): <https://github.com/Ravaelles/cherryvis-docker>
- viewer sources built in: <https://github.com/TorchCraft/TorchCraftAI>
  (`scripts/cherryvis`; the Django server + static JS)
- matcher rules: `openbw/ui/unit_matcher.cpp` (nearest same-type within 25px,
  ambiguity guards, one-to-one, NO retry of a missed frame)
- matching driver (served): `replays/static/replays/cdb/cvis_dbg_matching.js`
- our trace writer: `Atlantis/src/atlantis/cherryvis/` (per-defect stories in
  `_AI/CASE-STUDIES.md` under #65/#68, backlog in `_AI/NEXT.md`)

Run / reload:

```bash
cd /sc-ai/CherryVis/cherryvis-docker && docker build -t cvis .
docker run -d --name cvis -p 8770:8770 -v /sc-ai/REPLAYS:/my-replays cvis \
  ./server.py --host 0.0.0.0:8770 --replays_pattern '/my-replays/*.rep'
```

Boot persistence (no cron/systemd needed): `scripts/cherryvis-start.sh`
(Atlantis repo) ensures the viewer runs now and after reboot - it enables
the docker daemon autostart, recreates a stale `cvis` container with
`--restart unless-stopped`, and verifies the server answers. Idempotent,
safe to run any time.

- The build needs more than 120 s wall (pip `zstandard` alone ~90 s): run it
  detached and poll, do not wrap the whole build in a 120 s foreground timeout
  (CONVENTIONS §13: shrink the step - background + poll).
- Static-file check: `curl -s localhost:8770/static/replays/cdb/cvis_dbg_matching.js`
- Browser: hard refresh (Ctrl+Shift+R) after any static change; the viewer
  pairs `<name>.rep` with `<name>.rep.cvis/`.

Patch pattern (viewer fixes that must survive rebuilds): upstream files arrive
by clone, so a fix lives as `<name>.diff` in the repo plus a COPY/`git apply`
RUN block in the `Dockerfile` - see `cvis-views-zstd-compat.diff` and
`cvis-matching-no-console.diff`. A live-container edit (`docker cp`) is
immediate but dies with the next rebuild: stopgap only, then bake the diff.

Traces: `--cvis` runs leave `/sc-ai/REPLAYS/<name>.rep` + `<name>.rep.cvis/`
(`trace.json` + `trace.json.zst`). Since NEXT #76 the trace carries direct
`unit_ids` pairs, so the viewer skips fuzzy matching entirely.
Flag: `CHERRYVIS_ENABLED=1` in the bot slot ENV (renamed 2026-10-26 from
`GENERATE_CHERRYVIS_REPLAY`). scbw launchers (`/sc-ai/BOTS/_*.sh`) run
through `/sc-ai/BOTS/play-cvis.sh`, which ensures the flag and pairs the
game's trace next to its root replay after the match.
