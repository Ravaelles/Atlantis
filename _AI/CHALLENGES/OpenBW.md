# OpenBW: making the Atlantis Java client attach to the headless engine

Read this before touching `scripts/run-openbw-e2e.sh`, `build-bot-jar.sh`'s
library list, or anything that starts a game. Every failure below presented as a
different problem than it was, and each one cost a research cycle.

**Status 2026-10-09: solved and bounded.** The client attaches, the bot plays,
and every run self-terminates (CONVENTIONS §17). This file is the reference for
the traps, not a task list.

## The five packaging rules (`build-bot-jar.sh` asserts all of them)

A jar that cannot attach is a build failure, and the failure is silent - it
surfaces as "the bot never joins the game", so the assertions exist.

1. **junixsocket 1.0.x cannot run on Java 9+.** JBWAPI-Rav bundles the old fork
   (`no.fiken.oss.junixsocket`), whose `AFUNIXSocket` calls
   `java.net.Socket.setCreated()` - removed after Java 8. On Java 17 it throws
   `IllegalStateException: Cannot find method "setCreated"`, JBWAPI swallows it,
   and the client loops on "Unable to open communications socket" against a
   healthy server.
2. **A mixed junixsocket package is worse than an old one.** Replacing only the
   classes present in both versions leaves the new `AFUNIXSocket` (2.10.1,
   extends `AFSocket`) next to the old `AFUNIXSocketImpl` (1.0.2, extends
   `Socket`). The whole `org/newsclub/` tree must be dropped and re-laid.
3. **The native payload is versioned, compiler-tagged and descriptor-driven.**
   - 2.10.1 ships `lib/amd64-Linux-clang/jni/libjunixsocket-native-2.10.1.so`,
     but the loader asks for `lib/amd64-Linux/jni/...` - so the .so must also be
     aliased to the plain architecture name.
   - The `META-INF/native-image/**/resource-config.json` descriptors decide
     *which* native is chosen; dropping them makes the loader report
     "architecture not supported" with the .so present.
   - The jar must declare **`Multi-Release: true`**: the packaged junixsocket is
     a multi-release jar and the JVM ignores `META-INF/versions/**` without it.
   - The old fork's `META-INF/nar/.../nar.properties` and
     `META-INF/maven/no.fiken.oss.junixsocket/` metadata must be removed: the
     loader reads the version from them and looks for the wrong artefact.

## The decisive test that separated the layers

Connect to the socket by hand, with the same junixsocket, from a 20-line Java
program:

```java
AFUNIXSocket s = AFUNIXSocket.newInstance();
s.connect(new AFUNIXSocketAddress(new File(socketPath)));
```

- A clean classpath (`junixsocket-common` + `-native-common`) -> `CONNECTED OK`.
- The same program with `Atlantis.jar` -> `UnsatisfiedLinkError` / `setCreated`.

That is how the problem was localized to the **packed jar**, not the server and
not the client code. Without the split the investigation circles: the log says
"socket", the socket exists, and everything looks correct.

## The host registry: one setting, and the stale-state trap

- **The registry is created only when `shared_memory` resolves to ON.**
  `Server.cpp` gates it on
  `LoadConfigStringUCase("config", "shared_memory", "ON") == "ON"`, and
  `Config.cpp` resolves that from `BWAPI_CONFIG_<SECTION>__<KEY>` *before* any
  `bwapi.ini`. Without the flag the host serves a socket but publishes no game
  registry, and the client loops on "No server proc ID" while the socket it
  blames is fine. `scripts/run-openbw-e2e.sh` and `OpenBWHost` set it now.
  Pinned by `OpenBWLauncherTest`.
- *Two earlier readings of this were wrong and are removed from this file:* the
  registry slot is written with `isConnected = 0` by a live host (which is what
  the client's free-slot search wants); the `isConnected = 1` reading came from a
  **dead** host's stale entry. And `Server.cpp` does call
  `initializeSharedMemory()` from its constructor - nothing server-side needed to
  change.
- **A stale `/dev/shm` entry looks exactly like a live-host bug.** Clear
  `/dev/shm/bwapi_shared_memory_*` and `/tmp/bwapi_socket_*` **before** hosting,
  never after, and never decode a registry slot without checking
  `pgrep -x BWAPILauncher` first.
- **A stale segment makes the next launch a silent no-op.** If the segment
  already exists, `Server` sets `localOnly = true` and creates no socket at all -
  nothing in any log. This is why the clearing is mandatory, not cosmetic.

## Process-ID traps (cost the most time)

- `BWAPILauncher` **forks**: the PID `$!` returns is not always the one that owns
  the transport. Compare `pgrep -x BWAPILauncher` with the PID in
  `/dev/shm/bwapi_shared_memory_game_list` before concluding anything.
- A wrapper script that starts the host and then exits kills it with its process
  group. Use `setsid` + `nohup`, and start the client while the host is alive -
  in one command, not two terminal calls.
- **`pkill -x`, never `pkill -f`.** A `-f` pattern matches the command line of
  the shell running it, so the shell SIGKILLs itself before it can print
  anything, which reads as a mysterious hang.
- The host accepts **one** client, polls for 5 s, then unlinks the socket and
  `shm_unlink()`s the segment. The socket file is a rendezvous that disappears on
  success, so a late client sees "no socket" on a healthy host.

## What the client needs (from the vendored jar)

`bwapi.Client` picks the transport from `os.name`: Windows ->
`ClientConnectionW32`, everything else -> `ClientConnectionPosix`. There is no
third option. On Linux, `ClientConnectionPosix` opens, in order:

1. `/dev/shm/bwapi_shared_memory_game_list` - the registry, one line per game:
   `index | serverPid | state | size`;
2. `/dev/shm/bwapi_shared_memory_<serverPid>` - the game's shared segment;
3. `/tmp/bwapi_socket_<serverPid>` - the sync channel.

Slot layout: int `serverProcessID` at +0, byte `isConnected` at +4, int
`lastKeepAliveTime` at +8 (12 bytes per slot, 8 slots). The client takes the
first slot with `serverProcessID != 0 && !isConnected`. So on Linux the shared
segment is what matters first; the "socket" in an error message is usually a
*consequence* of the segment being unreachable.

## Rules this produced

- **Read the harness source before recording a blocker against it.** Two full
  sessions went into a "needs a server-side change" conclusion that one
  `grep serverEnabled` in `Server.cpp` overturned.
- **Clear stale state before hosting** (see above) - always.
- `build-bot-jar.sh` asserts the packaged junixsocket is 2.10.1: no `setCreated`,
  `AFSocket` present, no `junixsocket-native-1.*`, descriptors and the
  plain-architecture alias present.
- CONVENTIONS §14 keeps Wine/StarCraft off limits for models; §15 holds the
  lifecycle rules; §17 bounds every simulation.

## The two real bugs found on our side (fixed, tested)

These were ours, not the harness, and both looked like "the client cannot
attach":

- the launcher killed the host it was about to join;
- `ENV`/`build_orders` were written to the wrong directory, so the bot played
  with no production and said nothing.
