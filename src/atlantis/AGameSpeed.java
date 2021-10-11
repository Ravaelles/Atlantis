package atlantis;

import bwapi.Game;

import static atlantis.Atlantis.game;

public class AGameSpeed {

    /**
     * Game speed. Lower is faster. 0 is fastest, 20 is about normal game speed.
     * In game you can use buttons -/+ to change the game speed.
     */
    public static int gameSpeed = 0;

    /**
     * By skipping rendering of game frames, we can make the game much quicker, regardless of the game speed.
     * Value 3 means we render every 3th game frame, skipping 67% of total rendering.
     */
    public static int frameSkip = 3;

    private static final int DYNAMIC_SLOWDOWN_FRAME_SKIP = 80;

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
    public static void allowToDynamicallySlowdownGameOnFirstFighting() {
        dynamicSlowdown_previousSpeed = gameSpeed;
        dynamicSlowdown_lastTimeUnitDestroyed = AGame.getTimeSeconds();
        dynamicSlowdown_isSlowdownActive = true;

        Atlantis.game().setLocalSpeed(0);
        Atlantis.game().setFrameSkip(DYNAMIC_SLOWDOWN_FRAME_SKIP);

        System.out.println("SLOWDOWN is active. Frame skip = " + DYNAMIC_SLOWDOWN_FRAME_SKIP);
    }

    public static void disableDynamicSlowdown() {
        dynamicSlowdown_isSlowdownActive = false;

        Atlantis.game().setLocalSpeed(dynamicSlowdown_previousSpeed);
        Atlantis.game().setFrameSkip(5);
//        Atlantis.game().setFrameSkip(0);

        System.out.println("Disabled SLOWDOWN");
    }

    // =========================================================

    public static boolean isDynamicSlowdownActive() {
        return dynamicSlowdown_isSlowdownActive;
    }

    /**
     * Changes game speed. 0 - fastest 1 - very quick 20 - around default
     */
    public static void changeSpeedTo(int speed) {
        if (speed < 0) {
            speed = 0;
        }

        gameSpeed = speed;

//        try {
//            game().setLocalSpeed(AtlantisConfig.GAME_SPEED);
//            Thread.sleep(40);
//            game().setLocalSpeed(AtlantisConfig.GAME_SPEED);
//            Thread.sleep(40);
//        } catch (InterruptedException ex) {
//            // Ignore
//        }
        game().setLocalSpeed(gameSpeed);
        AGame.sendMessage("/speed " + gameSpeed);
        game().setLocalSpeed(gameSpeed);

//        String speedString = AtlantisConfig.GAME_SPEED + (AtlantisConfig.GAME_SPEED == 0 ? " (Max)" : "");
//        sendMessage("Game speed: " + speedString);
    }

    /**
     * Changes game speed by given ammount of units. Total game speed: 0 - fastest 1 - very quick 20 - around
     * default
     */
    public static void changeSpeedBy(int deltaSpeed) {
        int speed = gameSpeed + deltaSpeed;
        if (speed < 0) {
            speed = 0;
        }

        if (deltaSpeed > 0) {
            frameSkip += 10;
        } else {
            frameSkip /= 2;
        }
        game().setFrameSkip(frameSkip);

        if (game() != null) {
            gameSpeed = speed;
            changeSpeedTo(gameSpeed);
        }
        else {
            System.err.println("Can't change game speed, bwapi is null.");
        }
    }

    /**
     * Change game rendering frame skipping - speeds up game considerably.
     */
    public static void changeFrameSkipTo(int newFrameSkip) {
        if (frameSkip <= 1) {
            frameSkip = 0;
        }

        Game game = game();
        if (game != null) {
            frameSkip = newFrameSkip;
            game.setFrameSkip(frameSkip);
            game.setFrameSkip(frameSkip);
        }
        else {
            System.err.println("Can't change game speed, bwapi is null.");
        }
    }
}
