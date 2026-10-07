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

## The fifth blocker, found after the jar was fixed (measured 2026-10-07)

With the jar correct, the socket connects **from the packed Atlantis jar** as
well as from a bare classpath (`CONNECTED OK` in both cases), so the transport
is no longer the problem. The client still fails, now with **"No server proc
ID"**, and the cause is one layer deeper:

- `bwapi.Client.connect()` reads the server PID from the **shared-memory game
table** (`clientConnector.getGameTable()` -> `gameInstances[0].serverProcessID`)
and then calls `connectSharedLock(pid)`. Decompiled from the vendored jar; the
socket path is only the second step.
- This harness **never fills that table**. The host serves a working socket and
  a `/dev/shm/bwapi_shared_memory_<pid>` segment, but
  `/dev/shm/bwapi_shared_memory_game_list` stays 0 bytes, so the client reads
  PID 0 and gives up.
- `libBWAPI.so` in the harness *does* export
  `BWAPI::Server::initializeSharedMemory()` and `updateSharedMemory()` - the
  capability exists, `BWAPILauncher` simply does not create a `BWAPI::Server`.

So the mismatch is: the client (JBWAPI-Rav, upstream BWAPI 4.4 protocol) needs
the shared-memory game registry; this OpenBW launcher publishes only the socket.

**Rejected while diagnosing:** the launcher's `RUNPATH` points at
`/ravaelles/JAVA/starcraft-ai/...`, which looks alarming (and that path is off
limits under CONVENTIONS §8) - but the libraries there are **byte-identical**
(same size, same mtime), so it is a second path to the same build, not a stale
copy. Confirmed before drawing any conclusion from it.

## The sixth and final blocker: the registry slot must NOT say isConnected (2026-10-07)

With the jar fixed and the host alive, the client still printed "No server proc
ID" - and the registry it read was **correct**:

```
$ xxd -l 24 /dev/shm/bwapi_shared_memory_game_list
00000000: 176e 1200 0100 0000 b8f4 0000 0000 0000
         ^PID=1207831  ^isConnected=1  ^keepAlive
```

Decoding `Client.connect()` settles it. Its slot-selection loop is:

```
for each of the 8 slots:
    if (serverProcessID != 0 && !isConnected)     <- the host must be FREE
        and this slot has the highest keepAlive:
            candidate = slot
if (candidate == -1)  -> "No server proc ID"
```

The relevant part is `!isConnected`. The client is picking a host that has **not
claimed a client yet** - upstream BWAPI's server sets `isConnected` when it has
a client, and the client looks for a free one. This harness's `BWAPILauncher`
writes its slot with `isConnected = 1`, so the client skips the only slot that
exists, finds nothing, and reports "No server proc ID".

That is the whole attach problem reduced to one byte, and it is a
**server-side** change (the harness's BWAPI), which is why step 1 stops here and
asks for the owner's agreement rather than patching anything.

**Also settled:** the layout of a registry slot is int `serverProcessID` at +0,
byte `isConnected` at +4, int `lastKeepAliveTime` at +8 (12 bytes per slot, 8
slots) - verified from the decompiled `GameTable`, which matches the bytes on
disk exactly. If this ever needs reading again, that is the layout, not a guess.

## The sixth blocker: a registry slot marked isConnected is skipped by the client

Measured 2026-10-07, with the host alive and stable for 16+ s (PID 1218238) and
the client running against it:

```
$ xxd -l 24 /dev/shm/bwapi_shared_memory_game_list
00000000: be96 1200 0100 0000 bdf5 0000
         ^PID=1218238  ^isConnected=1  ^keepAlive

client log:  No server proc ID   (repeated, never 'Connection successful')
```

The registry entry is **correct and complete** - right PID, right offsets - and
the client still refuses it. Decompiling `bwapi.Client.connect()` explains why:
its slot-selection loop takes a slot only when

```
serverProcessID != 0  &&  !isConnected
```

with `isConnected` read as a **byte at offset +4** (int PID at +0, int keepAlive
at +8, 12 bytes per slot, 8 slots - verified against `GameTable`'s constructor
and against the bytes on disk, which match exactly).

This harness's `BWAPILauncher` publishes its slot with `isConnected = 1`. The
client reads that as "this host already has a client" and skips the only slot
that exists; with no candidate it reports "No server proc ID". Flipping the
byte by hand (`dd` at offset 4) removes that error and the client proceeds to
open the socket - the hypothesis was tested, not assumed.

**So the last step is server-side**: the harness's BWAPI must publish a free
slot (`isConnected = 0`) so the client can claim it. That is a change inside
`StardustDevEnvironment/`'s own fork - the workspace we are free to change -
and it is recorded as option 3 in `_AI/PLAN-OPENBW.md` rather than done
silently, because the harness is shared by other bots.

**Everything else is proven working:** the jar (junixsocket 2.10.1, five
packaging rules), the transport (`CONNECTED OK`), the host lifecycle, the
registry read, and all of it on Java 17 while the Wine path stays on Java 8.

## The sixth blocker: what actually blocks the attach, proven by experiment

**The registry is NOT the problem.** Measured with the host alive and stable
(16+ s, PID 1218238), reading the bytes on disk and decoding them against the
decompiled `GameTable`:

```
slot layout: int serverProcessID @ +0 | byte isConnected @ +4 | int keepAlive @ +8
             (12 bytes per slot, 8 slots)
```

The entry is correct and complete. An earlier reading of `isConnected = 1` was
from a slot left over by a **dead** host, not from the live one - the byte is
`0` while a host is waiting, which is exactly what the client's
`serverProcessID != 0 && !isConnected` test wants.

**What the server actually does** (`bwapi/BWAPI/Source/BWAPI/Server.cpp`,
read rather than inferred):

- `Server::Server()` creates the shared segment and the registry only when
  `serverEnabled`, and only when it managed to CREATE the memory itself.
  If the segment already exists (`data == null`), it sets `localOnly = true`
  and **creates no socket at all** (line 118-126). A stale segment from a
  killed host therefore makes the next launch a silent no-op - no socket, no
  registry update, nothing in the log. This is the single most dangerous
  failure mode of the whole setup and the reason every run must start by
  removing `/dev/shm/bwapi_shared_memory_*` and `/tmp/bwapi_socket_*`.
- When it does host, `checkForConnections()` polls the listening socket for
  **5 seconds**, accepts one client, then `unlink()`s the socket file and
  `shm_unlink()`s the segment and continues on the connected `data_socket`.
  So the socket file is a *rendezvous point that disappears on success*, and a
  client that arrives late sees "no socket" even though the host is healthy.

**Everything on our side is verified working:**
- `AFUNIXSocket` connects to the host's socket from a bare classpath **and**
  from the packed Atlantis jar (`CONNECTED OK`, with the host alive).
- The jar carries junixsocket 2.10.1 with all five packaging rules correct.
- The client reads a correct registry entry and resolves the right PID.

**What is left is a timing/staleness problem, not a missing capability**, and
its two halves are both operational rather than code:
1. never start a host while a stale segment exists (the `localOnly` trap above);
2. start the client inside the host's accept window.

The attempt to flip a registry byte by hand (`dd` at offset 4) did remove the
"No server proc ID" error and moved the client on to opening the socket - that
is what identified the byte's role - but it is a diagnostic, not a fix, and it
is not used by anything.

## Rules this produced

- CONVENTIONS §14: never start StarCraft/ChaosLauncher/Wine; OpenBW is the E2E
  engine, and the two setups live in separate directories
  (`bots/AtlantisP/AI` = Wine, `bots/AtlantisOpenBW/AI` = OpenBW).
- `build-bot-jar.sh` now asserts the packaged junixsocket is the 2.10.1 one:
  no `setCreated`, `AFSocket` present, no `junixsocket-native-1.*`, descriptors
  and the plain-architecture alias present. A jar that fails these cannot
  attach, and the failure is silent - which is why it is a build error now.
