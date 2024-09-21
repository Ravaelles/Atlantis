package atlantis.debug.profiler;

public class RealTime {
    public static long gameStarted = System.currentTimeMillis();

    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    public static long gameLengthInRealSeconds() {
        long now = currentTimestamp();
        long gameLength = now - gameStarted;

        return gameLength / 1000;
    }
}
