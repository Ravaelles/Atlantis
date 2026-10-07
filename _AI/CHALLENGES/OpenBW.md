# OpenBW: making the Atlantis Java client attach to the headless engine

The single most expensive problem in the production-v2 session (2026-10-07):
the client never attached, and every symptom pointed somewhere else. Read this
before touching `scripts/run-openbw-e2e.sh`, `build-bot-jar.sh`'s library list,
or anything that starts a game.

## TL;DR - the four independent blockers, in the order they were found

1. **junixsocket 1.0.x cannot run on Java 9+.** JBWAPI-Rav bundles the old fork
   (`no.fiken.oss.junixsocket`), whose `AFUNIXSocket` calls
   `java.net.Socket.setCreated()` - removed after Java 8. On this machine
   (Java 17) it throws `IllegalStateException: Cannot find method "setCreated"`.
   JBWAPI swallows it and reports a plain connection failure, so the client
   loops on "Unable to open communications socket" against a healthy server.
2. **A mixed junixsocket package is worse than an old one.** Replacing only the
   classes present in both versions leaves the new `AFUNIXSocket` (2.10.1,
   extends `AFSocket`) next to the old `AFUNIXSocketImpl` (1.0.2, extends
   `Socket`). The whole `org/newsclub/` tree must be dropped and re-laid.
3. **The native payload is versioned, compiler-tagged and descriptor-driven.**
   - 2.10.1 ships `lib/amd64-Linux-clang/jni/libjunixsocket-native-2.10.1.so`,
     but the loader builds its path from the JVM's architecture and asks for
     `lib/amd64-Linux/jni/...` - so the .so must also be aliased to the plain
     architecture name.
   - The `META-INF/native-image/**/resource-config.json` descriptors decide
     *which* native is chosen; dropping them (easy, they are not `.class`) makes
     the loader report "architecture not supported" with the .so present.
   - The jar must declare **`Multi-Release: true`** in its manifest: the
     packaged junixsocket is a multi-release jar and the JVM ignores
     `META-INF/versions/**` without that line.
   - The old fork's `META-INF/nar/.../nar.properties` and
     `META-INF/maven/no.fiken.oss.junixsocket/` metadata must be removed too:
     the loader reads the version from them and looks for the wrong artefact.
   All five were needed; each one alone still failed to attach.
4. **The server must outlive the client's start, and `game_list` is a trap.**
   `run-openbw-server.sh` ends with `exec BWAPILauncher`, so the host's PID is
   the script's PID. If the host dies (or is killed by a `timeout`, or by the
   terminal session ending), `/dev/shm/bwapi_shared_memory_game_list` keeps the
   dead PID and the next client adopts it, then loops on "No server proc ID".
   A stale entry is indistinguishable from a real one from the client's side.

## The decisive test that separated the layers

Connect to the socket by hand, with the same junixsocket, from a 20-line Java
program. It isolates "is the transport reachable" from "does JBWAPI work":

```java
AFUNIXSocket s = AFUNIXSocket.newInstance();
s.connect(new AFUNIXSocketAddress(new File(socketPath)));
```

- Czysty classpath (`junixsocket-common` + `-native-common`) -> `CONNECTED OK`.
- Ten sam program z `Atlantis.jar` na classpath -> `UnsatisfiedLinkError` /
  `setCreated` - i to jest moment, w którym wiadomo, że problem jest
  w **spakowanym** jarze, a nie w serwerze ani w kodzie klienta.

Without that split the investigation goes in circles: the client log says
"socket", the socket exists, and everything looks correct.

## Process-ID traps (cost the most time)

- `BWAPILauncher` **forks**: the PID that `$!` returns is not always the PID
  that owns the transport. Compare `pgrep -x BWAPILauncher` with the PID in
  `game_list` before concluding anything.
- A wrapper script that starts the host and then exits kills the host with its
  process group. Use `setsid` + `nohup`, and start the client **while the host
  is still alive** - in one command, not two terminal calls.
- `game_list` is not cleared when a host dies. Always
  `rm -f /dev/shm/bwapi_shared_memory_* /tmp/bwapi_socket_*` and
  `pkill -9 -x BWAPILauncher` **before** hosting, never after.
- `pkill -f <pattern>` matches the shell running it: `pkill -f BWAPILauncher`
  SIGKILLs your own session before it prints anything. Use `pkill -x`.

## What the client actually needs (verified by decompiling the vendored jar)

`bwapi.Client` picks the transport from `os.name`: Windows -> `ClientConnectionW32`,
everything else -> `ClientConnectionPosix`. There is no third option and no
property to force one. `ClientConnectionPosix` opens, in order:

1. `/bwapi_shared_memory_game_list` (shm_open -> `/dev/shm/...`) - the registry
   of running games, one line per game: `index | serverPid | state | size`,
2. `/bwapi_shared_memory_<serverPid>` - the game's shared segment,
3. `/tmp/bwapi_socket_<serverPid>` - the sync channel (`AFUNIXSocket`).

So on Linux the shared segment is what matters first; the socket line in the
error message is a *consequence* of the segment being unreachable, not the
cause. Do not chase the socket.

## Rules this produced

- CONVENTIONS §14: never start StarCraft/ChaosLauncher/Wine; OpenBW is the E2E
  engine, and the two setups live in separate directories
  (`bots/AtlantisP/AI` = Wine, `bots/AtlantisOpenBW/AI` = OpenBW).
- `build-bot-jar.sh` now asserts the packaged junixsocket is the 2.10.1 one:
  no `setCreated`, `AFSocket` present, no `junixsocket-native-1.*`, descriptors
  and the plain-architecture alias present. A jar that fails these cannot
  attach, and the failure is silent - which is why it is a build error now.
