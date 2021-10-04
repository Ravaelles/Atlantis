package atlantis;

public class AGameSpeed {

    private static int DYNAMIC_SLOWDOWN_FRAME_SKIP = 9;

    // DYNAMIC SLOWDOWN - game speed adjustment, fast initially, slow down when there's fighting - see AtlantisConfig
    private static boolean dynamicSlowdown_isSlowdownActive = false;

    // Last time unit has died; when unit dies, game slows down
    private static int dynamicSlowdown_lastTimeUnitDestroyed = 0;

    // Normal game speed, outside autoSlodown mode.
    private static int dynamicSlowdown_previousSpeed = 0;

    // =========================================================

//    public AGameSpeed() {
//        Atlantis.game().setLocalSpeed(0);
//    }

    // =========================================================

    /**
     * Decreases game speed to the value specified in AtlantisConfig when action happens.
     */
    public static void activateDynamicSlowdownMode() {
        dynamicSlowdown_previousSpeed = AtlantisConfig.GAME_SPEED;
        dynamicSlowdown_lastTimeUnitDestroyed = AGame.getTimeSeconds();
        dynamicSlowdown_isSlowdownActive = true;

        Atlantis.game().setLocalSpeed(0);
        Atlantis.game().setFrameSkip(DYNAMIC_SLOWDOWN_FRAME_SKIP);

        System.out.println("SLOWDOWN is active. Frame skip = " + DYNAMIC_SLOWDOWN_FRAME_SKIP);
    }

    public static void disableDynamicSlowdown() {
        dynamicSlowdown_isSlowdownActive = false;

        Atlantis.game().setLocalSpeed(dynamicSlowdown_previousSpeed);
        Atlantis.game().setFrameSkip(0);

        System.out.println("Disabled SLOWDOWN");
    }

    // =========================================================

    public static boolean isDynamicSlowdownActive() {
        return dynamicSlowdown_isSlowdownActive;
    }
}
