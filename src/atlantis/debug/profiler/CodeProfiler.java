package atlantis.debug.profiler;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisConfig;
import atlantis.game.A;

import java.util.*;

public class CodeProfiler {
    /**
     * Minimal time in milliseconds that has to be consumed by given aspect to be included in profiling results.
     */
    public static final int MIN_MS_TO_INCLUDE = 3;

    public static final String ASPECT_PAINTING = "painting";

    private static final HashMap<String, Long> aspectsStart = new HashMap<>();
    private static final HashMap<String, Long> aspectsLength = new HashMap<>();
    private static final int divideMeasurementBy = 1; // Needed if using nano instead of millis
    private static long currentFrameStart = -1;
    private static long currentFrameLength = 0;

    // =========================================================
    public static void startMeasuring(Commander commander) {
        if (!commander.shouldProfile()) return;
        if (!AtlantisConfig.USE_CODE_PROFILER) return;

        measureAspect(forCommander(commander));
    }

    /**
     * Indicates that we've stopped handling <b>title</b>-related stuff. Now we can calculate how long
     * it took by comparing two timestamps.
     * <br />Used for determining total time that it took to handle given type of activity and
     * displayed in full painting mode (press 3 in game) as relative time-consumptions bars.
     */
    public static void endMeasuring(String title) {
        if (!AtlantisConfig.USE_CODE_PROFILER) return;

        if (aspectsStart.containsKey(title)) {
            long measuredLengthInMs = realNowInMs() - aspectsStart.get(title);
            aspectsLength.put(title, measuredLengthInMs);

            LongFrames.checkPotentialLongMeasurement(measuredLengthInMs, title);
        }
    }

    public static void endMeasuring(Commander commander) {
        if (!commander.shouldProfile()) return;
        if (!AtlantisConfig.USE_CODE_PROFILER) return;

        endMeasuring(forCommander(commander));
    }

    public static String forCommander(Commander commander) {
        return classNameToProfilerName(commander.getClass().getSimpleName());
    }

    public static int lastFrameLength() {
        return (int) currentFrameLength;
    }

    public static void printSummary() {
        if (!AtlantisConfig.USE_CODE_PROFILER) {
            System.err.println("### Code profiler is disabled in AtlantisConfig ###");
            return;
        }

        System.out.println("### Commanders time consumption ##########");
        System.out.println(String.format("%25s:  ", "Total frame length:") + lastFrameLength() + "ms\n");

        Map<String, Integer> aspectsLength = aspectLengthSorted();
        for (String aspectTitle : aspectsLength.keySet()) {
            int value = aspectsLength.get(aspectTitle);
            if (value >= MIN_MS_TO_INCLUDE) {
                System.out.println(String.format("%25s:  ", aspectTitle) + value + "ms");
            }
        }

        if (aspectLengthSorted().isEmpty()) {
            System.out.println("No aspects measured, looks like a bug.");
        }

        System.out.println("### END OF Commanders time consumption ###");
    }

    public static void startMeasuringTotalFrame() {
        currentFrameStart = realNowInMs();
    }

    public static void endMeasuringTotalFrame() {
        currentFrameLength = realNowInMs() - currentFrameStart;
    }

    public static Map<String, Integer> aspectLengthSorted() {
        List<Map.Entry<String, Long>> entryList = new ArrayList<>(aspectsLength.entrySet());

        // Custom comparator for sorting by values in descending order
        Comparator<Map.Entry<String, Long>> valueComparator =
            (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue());

        // Sort the list using the custom comparator
        entryList.sort(valueComparator);

        // Iterate through the sorted list and print the entries
//        for (Map.Entry<String, Double> entry : entryList) {

//        }

        Map<String, Integer> results = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : entryList) {
            results.put(entry.getKey(), entry.getValue().intValue() / divideMeasurementBy);
        }

        return results;
    }

    // =========================================================

    private static void measureAspect(String title) {
        aspectsStart.put(title, realNowInMs());
    }

    private static long realNowInMs() {
//        return System.nanoTime();
        return System.currentTimeMillis();
    }

    private static String classNameToProfilerName(String className) {
        String profilerName = className.replace("Commander", "");

        if (profilerName.charAt(0) == 'A' && Character.isUpperCase(profilerName.charAt(1))) {
            profilerName = profilerName.substring(1);
        }

        return A.ucfirst(profilerName);
    }
}
