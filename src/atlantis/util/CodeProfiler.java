package atlantis.util;

import java.util.HashMap;

public class CodeProfiler {
    
    public static final String ASPECT_COMBAT = "combat";
    public static final String ASPECT_PAINTING = "painting";
    public static final String ASPECT_PRODUCTION = "production";
    public static final String ASPECT_SCOUTING = "scouting";
    public static final String ASPECT_WORKERS = "workers";
    
    // =========================================================

    private static HashMap<String, Long> aspectsStart = new HashMap<>();
    private static HashMap<String, Double> aspectsLength = new HashMap<>();
    
    // =========================================================

    public static void startMeasuring(String title) {
        measureAspect(title);
    }

    public static void endMeasuring(String title) {
        long measured = now() - aspectsStart.get(title);

//        if (!aspectsLength.containsKey(title)) {
//            aspectsLength.put(title, (double) measured);
//        } else {
//            aspectsLength.put(title, aspectsLength.get(title) * 0.6 + measured * 0.4);
            aspectsLength.put(title, (double) measured);
//        }
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

}
