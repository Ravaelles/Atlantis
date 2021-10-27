package atlantis.strategy;


public class AStrategyInformations {
    
    public static int antiLandBuildingsNeeded = 0;
    public static int antiAirBuildingsNeeded = 0;
    public static int detectorsNeeded = 0;

    // === Setters ========================================
    
    public static void antiLandBuildingsNeeded(int min) {
        if (antiLandBuildingsNeeded < min) {
            antiLandBuildingsNeeded = min;
        }
    }

    public static void antiAirBuildingsNeeded(int min) {
        if (antiAirBuildingsNeeded < min) {
            antiAirBuildingsNeeded = min;
        }
    }

    public static void detectorsNeeded(int min) {
        if (detectorsNeeded < min) {
            detectorsNeeded = min;
        }
    }

}
