package atlantis.util;

import atlantis.architecture.Commander;
import atlantis.game.A;

import java.util.HashMap;

/**
 * Currently obsolete.
 */
public class CodeProfiler {
    public static final String ASPECT_PRODUCTION = "production";
    public static final String ASPECT_COMBAT = "combat";
    public static final String ASPECT_PAINTING = "painting";
    public static final String ASPECT_OTHER = "other";
    public static final String ASPECT_SCOUTING = "scouting";
    public static final String ASPECT_STRATEGY = "strategy";
    public static final String ASPECT_WORKERS = "workers";

    // =========================================================

    private static final HashMap<String, Long> aspectsStart = new HashMap<>();
    private static final HashMap<String, Double> aspectsLength = new HashMap<>();

    // =========================================================

    /**
     * Indicates that from now on, until endMeasuring is executed, the bot is calculating things related
     * to <b>title</b>.
     * <br />Used for determining total time that it took to handle given type of activity e.g.
     * `production related stuff`. Displayed in full painting mode (press 3 in game) as relative
     * time-consumptions bars.
     */
    public static void startMeasuring(String title) {
        measureAspect(title);
    }

    public static void startMeasuring(Commander commander) {
        measureAspect(forCommander(commander));
    }

    /**
     * Indicates that we've stopped handling <b>title</b>-related stuff. Now we can calculate how long
     * it took by comparing two timestamps.
     * <br />Used for determining total time that it took to handle given type of activity and
     * displayed in full painting mode (press 3 in game) as relative time-consumptions bars.
     */
    public static void endMeasuring(String title) {
        if (aspectsStart.containsKey(title)) {
            long measured = now() - aspectsStart.get(title);
            aspectsLength.put(title, (double) measured);
        }
    }

    public static void endMeasuring(Commander commander) {
        endMeasuring(forCommander(commander));
    }

    public static String forCommander(Commander commander) {
        return classNameToProfilerName(commander.getClass().getSimpleName());
    }

    public static HashMap<String, Double> getAspectsTimeConsumption() {
        return aspectsLength;
    }

    public static double getTotalFrameLength() {
        double total = 0;
        for (Double value : aspectsLength.values()) {
            total += value;
        }
        return (int) Math.pow(total / 12, 0.35);
    }

    // =========================================================

    private static void measureAspect(String title) {
        // if (!aspectsStart.containsKey(title)) {
        // aspectsStart.put(title, now());
        // } else {
        aspectsStart.put(title, now());
        // }
    }

    private static long now() {
        return System.nanoTime();
//        return System.currentTimeMillis();
    }

    private static String classNameToProfilerName(String className) {
        String profilerName = className.replace("Commander", "");

        if (profilerName.charAt(0) == 'A' && Character.isUpperCase(profilerName.charAt(1))) {
            profilerName = profilerName.substring(1);
        }

        return A.ucfirst(profilerName);
    }

}
