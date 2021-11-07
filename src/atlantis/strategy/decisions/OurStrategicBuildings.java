package atlantis.strategy.decisions;


public class OurStrategicBuildings {

    private static int antiLandBuildingsNeeded = 0;
    private static int antiAirBuildingsNeeded = 0;
    private static int detectorsNeeded = 0;

    // === Setters ========================================
    
    public static void setAntiLandBuildingsNeeded(int min) {
        if (antiLandBuildingsNeeded < min) {
            antiLandBuildingsNeeded = min;
        }
    }

    public static void setAntiAirBuildingsNeeded(int min) {
        if (antiAirBuildingsNeeded < min) {
            antiAirBuildingsNeeded = min;
        }
    }

    public static void setDetectorsNeeded(int min) {
        if (detectorsNeeded < min) {
            detectorsNeeded = min;
        }
    }

    // =========================================================

    public static int antiLandBuildingsNeeded() {
        return antiLandBuildingsNeeded;
    }

    public static int antiAirBuildingsNeeded() {
        return antiAirBuildingsNeeded;
    }

    public static int detectorsNeeded() {
        return detectorsNeeded;
    }

}
