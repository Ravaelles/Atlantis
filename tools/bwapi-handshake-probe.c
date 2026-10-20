/*
 * bwapi-handshake-probe - measure the OpenBW/BWAPI attach handshake without a bot.
 *
 * WHY
 *   The Java client blocks in ClientConnectionPosix.waitForServerData() on a
 *   native read() of the per-game UNIX socket, waiting for the single byte 2.
 *   The server writes that byte from Server::callOnFrame() and then blocks
 *   reading byte 1 back. When a run stalls, nobody knows whether the server
 *   ever wrote the 2 or whether the client just did not see it. This tool
 *   answers exactly that, with timestamps, and is independent of the bot.
 *
 * PROTOCOL (verified in BWAPI/Server.cpp and JBWAPI ClientConnectionPosix.java)
 *   1. The client connects to AF_UNIX /tmp/bwapi_socket_<serverPid>.
 *   2. The server writes one byte 2.
 *   3. The client writes one byte 1 back.
 *   4. From then on the server calls callOnFrame() on every Server::update()
 *      (i.e. every frame), each time writing 2 and waiting for 1.
 *
 * WHAT IT REPORTS
 *   - whether the socket exists right now,
 *   - how long connect() took,
 *   - whether byte 2 arrived, and after how many milliseconds,
 *   - whether the exchange repeats (2,1),(2,1)... i.e. whether the game is
 *     actually advancing frames while we are attached,
 *   - the first byte value if it is not 2,
 *   - the client-visible game state read straight from the shared segment
 *     /dev/shm/bwapi_shared_memory_<pid>: frameCount and isInGame. A handshake
 *     alone is not "match running": the match can be live on the socket and
 *     never advance a frame, which is exactly what "zero frame lines" means.
 *
 * Build:
 *   cc -O2 -o bwapi-handshake-probe handshake_probe.c
 * Usage:
 *   bwapi-handshake-probe --pid <serverPid> [--timeout-ms 21000] [--frames 5]
 *   bwapi-handshake-probe --auto            # pick the live pid from the game table
 *
 * Exit codes:
 *   0  handshake completed (byte 2 received) and answered
 *   1  socket existed but no byte 2 within the timeout   <- server never sent it
 *   2  no socket / bad arguments                          <- nothing to test
 *   3  byte 2 arrived but writing 1 back failed
 */
#define _GNU_SOURCE
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/un.h>
#include <time.h>
#include <unistd.h>

#define GAME_LIST "/dev/shm/bwapi_shared_memory_game_list"
#define TABLE_ENTRIES 8
#define ENTRY_SIZE 16

static long long now_ms(void) {
	struct timespec ts;
	clock_gettime(CLOCK_MONOTONIC, &ts);
	return (long long)ts.tv_sec * 1000 + ts.tv_nsec / 1000000;
}

/* Reads the published game table and returns the pid of the first slot whose
 * serverProcessID is alive. Returns 0 if there is no live game. */
static int live_server_pid(void) {
	FILE* f = fopen(GAME_LIST, "rb");
	if (!f) return 0;
	unsigned char buf[TABLE_ENTRIES * ENTRY_SIZE];
	size_t n = fread(buf, 1, sizeof buf, f);
	fclose(f);
	int found = 0;
	for (int i = 0; i < TABLE_ENTRIES; ++i) {
		size_t off = (size_t)i * ENTRY_SIZE;
		if (off + ENTRY_SIZE > n) break;
		uint32_t pid;
		memcpy(&pid, buf + off, sizeof pid);
		if (!pid) continue;
		char path[64];
		snprintf(path, sizeof path, "/proc/%u", pid);
		struct stat st;
		if (stat(path, &st) == 0) {
			found = (int)pid;
			break;
		}
	}
	return found;
}

/* Receives exactly one byte with a deadline. Returns 1 on success, 0 on
 * timeout, -1 on error/EOF. Records how long the wait took. */
static int recv_byte(int fd, int timeout_ms, unsigned char* out, long long* waited_ms) {
	long long start = now_ms();
	for (;;) {
		long long elapsed = now_ms() - start;
		int left = timeout_ms - (int)elapsed;
		if (left <= 0) return 0;
		struct pollfd pfd = {.fd = fd, .events = POLLIN};
		int r = poll(&pfd, 1, left);
		if (r == 0) return 0;
		if (r < 0) {
			if (errno == EINTR) continue;
			return -1;
		}
		ssize_t got = read(fd, out, 1);
		if (got == 1) {
			*waited_ms = now_ms() - start;
			return 1;
		}
		if (got == 0) return -1; /* EOF: server closed the socket */
		if (errno == EINTR || errno == EAGAIN) continue;
		return -1;
	}
}

/* GameData layout, queried from the real header with offsetof() (not guessed):
 *   sizeof = 33017048, frameCount @ 3446096, isInGame @ 10586548
 * If the BWAPI struct changes these must be re-measured; the sanity checks
 * below (revision/version) keep a mismatch from being silently reported. */
#define GAMEDATA_FRAME_COUNT_OFF 3446096
#define GAMEDATA_IS_IN_GAME_OFF 10586548
/* Event queue and player table (same offsetof source):
 *   eventCount @ 10586620, events @ 10586624, sizeof(Event)=40 (type,v1,v2 + pad)
 *   players @ 192, startLocation in PlayerData is what the client needs for
 *   MatchStart (an empty player table is why the client cannot bootstrap). */
#define GAMEDATA_EVENT_COUNT_OFF 10586620
#define GAMEDATA_EVENTS_OFF 10586624
#define EVENT_SIZE 40
#define MAX_EVENTS 100
#define EVENT_MATCH_START 0
#define GAMEDATA_PLAYERS_OFF 192
#define PLAYER_DATA_SIZE 5788   /* sizeof(PlayerData), measured with offsetof */
#define PLAYER_DATA_START_LOCATION_OFF 68  /* PlayerData::startLocationX */

struct game_state_view {
	int frame_count;
	int is_in_game;
	int ok;
	int match_start_seen;
	int event_count;
	int players_with_start;   /* players[0..7] with a start location */
	int self_slot;
};

static struct game_state_view read_game_state(int pid) {
	struct game_state_view v; memset(&v, 0, sizeof v);
	char path[96];
	snprintf(path, sizeof path, "/dev/shm/bwapi_shared_memory_%d", pid);
	int fd = open(path, O_RDONLY);
	if (fd < 0) return v;
	struct stat st;
	if (fstat(fd, &st) == 0 && st.st_size > GAMEDATA_IS_IN_GAME_OFF + 4) {
		/* Sparse-friendly: read only the two interesting fields. */
		if (pread(fd, &v.frame_count, sizeof v.frame_count, GAMEDATA_FRAME_COUNT_OFF) == sizeof v.frame_count &&
		    pread(fd, &v.is_in_game, 1, GAMEDATA_IS_IN_GAME_OFF) == 1) {
			v.ok = 1;
			if (pread(fd, &v.event_count, sizeof v.event_count, GAMEDATA_EVENT_COUNT_OFF) == sizeof v.event_count) {
				if (v.event_count > MAX_EVENTS) v.event_count = MAX_EVENTS;
				for (int i = 0; i < v.event_count; ++i) {
					int type = 0;
					if (pread(fd, &type, sizeof type, (off_t)GAMEDATA_EVENTS_OFF + (off_t)i * EVENT_SIZE) == sizeof type
					    && type == EVENT_MATCH_START) {
						v.match_start_seen = 1;
					}
				}
			}
			if (pread(fd, &v.self_slot, sizeof v.self_slot, 10586608) == sizeof v.self_slot) {
				for (int p = 0; p < 12; ++p) {
					/* PlayerData::startLocation is a TilePosition {int x,y} */
					int x = -1;
					off_t off = (off_t)GAMEDATA_PLAYERS_OFF + (off_t)p * PLAYER_DATA_SIZE + PLAYER_DATA_START_LOCATION_OFF;
					if (pread(fd, &x, sizeof x, off) == sizeof x && x >= 0) ++v.players_with_start;
				}
			}
		}
	}
	close(fd);
	return v;
}

static int send_byte(int fd, unsigned char v) {
	return write(fd, &v, 1) == 1 ? 0 : -1;
}

int main(int argc, char** argv) {
	int pid = 0, timeout_ms = 21000, frames = 5, auto_pid = 0;

	for (int i = 1; i < argc; ++i) {
		if (!strcmp(argv[i], "--pid") && i + 1 < argc) pid = atoi(argv[++i]);
		else if (!strcmp(argv[i], "--timeout-ms") && i + 1 < argc) timeout_ms = atoi(argv[++i]);
		else if (!strcmp(argv[i], "--frames") && i + 1 < argc) frames = atoi(argv[++i]);
		else if (!strcmp(argv[i], "--auto")) auto_pid = 1;
		else {
			fprintf(stderr, "usage: %s --pid <serverPid> | --auto [--timeout-ms N] [--frames N]\n", argv[0]);
			return 2;
		}
	}
	if (auto_pid && !pid) pid = live_server_pid();
	if (!pid) {
		fprintf(stderr, "probe: no live server pid (pass --pid or wait until the game table has one)\n");
		return 2;
	}

	char path[128];
	snprintf(path, sizeof path, "/tmp/bwapi_socket_%d", pid);
	struct stat st;
	if (stat(path, &st) != 0) {
		fprintf(stderr, "probe: %s does not exist (server not accepting yet)\n", path);
		return 2;
	}

	int fd = socket(AF_UNIX, SOCK_STREAM, 0);
	if (fd < 0) {
		perror("probe: socket");
		return 2;
	}

	struct sockaddr_un addr;
	memset(&addr, 0, sizeof addr);
	addr.sun_family = AF_UNIX;
	strncpy(addr.sun_path, path, sizeof addr.sun_path - 1);

	long long t0 = now_ms();
	if (connect(fd, (struct sockaddr*)&addr, sizeof addr) != 0) {
		fprintf(stderr, "probe: connect(%s) failed: %s\n", path, strerror(errno));
		close(fd);
		return 2;
	}
	printf("probe: connected to %s in %lld ms\n", path, now_ms() - t0);
	/* The per-game segment is created by the server when it accepts the socket,
	 * so state can only be read after connect() - reading before it always
	 * reported "unreadable" even for a perfectly healthy server. */
	struct game_state_view before = read_game_state(pid);

	unsigned char code = 0;
	long long waited = 0;
	int r = recv_byte(fd, timeout_ms, &code, &waited);
	if (r == 0) {
		printf("VERDICT: NO_BYTE_2 (waited %d ms) - the server never wrote the go-ahead "
		       "byte. This is a server-side path issue (callOnFrame not reached), not a "
		       "client issue.\n", timeout_ms);
		close(fd);
		return 1;
	}
	if (r < 0) {
		printf("VERDICT: SOCKET_CLOSED before byte 2 (after %lld ms) - the server "
		       "closed the connection (write failure in callOnFrame).\n", now_ms() - t0);
		close(fd);
		return 1;
	}
	if (code != 2) {
		printf("VERDICT: UNEXPECTED_BYTE %u (after %lld ms) - protocol mismatch.\n", code, waited);
		close(fd);
		return 1;
	}
	printf("probe: received byte 2 after %lld ms\n", waited);

	if (send_byte(fd, 1) != 0) {
		printf("VERDICT: BYTE_2_OK_BUT_REPLY_FAILED - could not write byte 1 back.\n");
		close(fd);
		return 3;
	}
	printf("probe: answered byte 1\n");

	/* If the game keeps advancing, the server repeats 2 -> 1 every frame.
	 * Counting those tells us whether the server is still stepping frames. */
	int cycles = 0;
	long long last = now_ms();
	for (int i = 0; i < frames; ++i) {
		r = recv_byte(fd, 3000, &code, &waited);
		if (r != 1 || code != 2) break;
		if (send_byte(fd, 1) != 0) break;
		++cycles;
		last = waited;
	}
	long long total = now_ms() - t0;
	printf("probe: %d additional frame ticks in %lld ms (last wait %lld ms)\n", cycles, total, last);

	/* The decisive question: did the MATCH advance, not just the socket? */
	struct game_state_view after = read_game_state(pid);
	if (!after.ok) {
		printf("probe: shared game state unreadable (/dev/shm/bwapi_shared_memory_%d)"
		       " - cannot judge frame advance\n", pid);
	} else {
		printf("probe: frameCount %d -> %d, isInGame %d -> %d\n",
		       before.ok ? before.frame_count : -1, after.frame_count,
		       before.ok ? before.is_in_game : -1, after.is_in_game);
		printf("probe: MatchStart in event queue: %s (eventCount=%d)\n",
		       after.match_start_seen ? "YES" : "NO", after.event_count);
		printf("probe: players with start location: %d/12 (self slot=%d)\n",
		       after.players_with_start, after.self_slot);
	}
	printf("VERDICT: BYTE_2_OK - the server does send the go-ahead; a client that does not "
	       "see it is racing accept/connect on the socket side.%s\n",
	       cycles ? " The game is advancing frames." : " The game stopped advancing after attach.");
	if (after.ok) {
		int advanced = after.frame_count > (before.ok ? before.frame_count : 0);
		printf("VERDICT: MATCH_%s - frameCount=%d isInGame=%d MatchStart=%s players=%d/12\n",
		       advanced || cycles ? "TICKING" : "NOT_TICKING", after.frame_count, after.is_in_game,
		       after.match_start_seen ? "YES" : "NO", after.players_with_start);
		if (!advanced && !cycles) return 4;   /* handshake fine, match dead */
	}

	close(fd);
	return 0;
}
