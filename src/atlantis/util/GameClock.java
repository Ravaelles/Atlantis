package atlantis.util;

/**
 * The frame and second counters, published by the game layer for the kernel.
 *
 * <p>This exists because "what time is it?" is the one question
 * {@code atlantis.util} kept asking {@code atlantis.game} - and the kernel is not
 * allowed to look upward (REVIEW §13, the rule the ArchUnit store tracks). Seven
 * call sites did it: {@code Cache}'s TTL, {@code Log}'s expiry, {@code ErrorLog}'s
 * once-a-minute throttle, {@code ConsoleLog}'s frame prefix, {@code TimeMoment}'s
 * "ago", and the log kernel's own throttle.</p>
 *
 * <p>The dependency is the other way round, which is the point: the game layer
 * publishes the counters once per frame ({@code AGame.calcSeconds()} in a game, the
 * harness's {@code useFakeTime} in a test) and everything below reads a field. Same
 * shape as {@code A.now} itself, which the game layer also writes every frame and
 * "nothing reads any more" - this one has readers.</p>
 *
 * <p>Before the first publish the counters are 0, exactly like {@code A.now}'s
 * initial value, so a cache that asks during start-up sees frame 0 rather than an
 * exception.</p>
 */
public class GameClock {

    private static int frames = 0;
    private static int seconds = 0;

    private GameClock() {
    }

    /**
     * Called once per frame by whoever owns the clock: the game layer in a game, the
     * harness in a test. Both write {@code A.now} / {@code A.s} in the same breath,
     * so the two views of "now" cannot drift apart.
     */
    public static void publish(int framesNow, int secondsNow) {
        frames = framesNow;
        seconds = secondsNow;
    }

    public static int frames() {
        return frames;
    }

    public static int seconds() {
        return seconds;
    }

    /**
     * The kernel's {@code A.everyNthGameFrame(n)}: same arithmetic on the same
     * number, one layer down.
     */
    public static boolean everyNthFrame(int n) {
        return frames % n == 0;
    }

    /**
     * Frames since {@code framesThen}. The counterpart of {@code A.ago(int)}.
     */
    public static int framesSince(int framesThen) {
        return frames - framesThen;
    }
}