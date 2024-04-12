package atlantis.game;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.config.AtlantisConfig;
import atlantis.config.MapSpecificCommander;

import bwapi.*;

import java.util.ArrayList;
import java.util.List;

import static atlantis.Atlantis.game;

/**
 * Represents various aspect of the game like time elapsed (in frames or approximated seconds), free supply
 * (from our point of view), game speed, enemy APlayer etc.<br />
 * <br /><b>It's worth to study this class carefully as it contains some really useful methods.</b>
 */
public class AGame {
    private static boolean umsMode = false; // Should be set to `true` on UMS (custom) maps
    private static APlayer _enemy = null; // Cached enemy APlayer
    private static APlayer _our = null; // Cached our APlayer
    private static int _framesNow = 0; // Cached current frames count
    private static int _secondsNow = 0; // Cached current frames count

    // =========================================================

    /**
     * Quits the game gently, killing all processes and cleaning up.
     */
    public static void exit() {
        Atlantis.getInstance().onEnd(false);
    }

    public static void quit() {
        exit();
    }

    /**
     * Quits the game gently, killing all processes and cleaning up.
     */
    public static void exit(String message) {
        A.println(message);
        Atlantis.getInstance().exitGame();
    }

    public static void changeDisableUI(boolean disableUI) {
        AtlantisConfig.DISABLE_GUI = disableUI;
        Game game = game();
        if (game != null) {
            game.setGUI(AtlantisConfig.DISABLE_GUI);
        }
    }

    /**
     * Returns game speed.
     */
    public static int getGameSpeed() {
        return GameSpeed.gameSpeed;
    }

    /**
     * Returns approximate number of in-game seconds elapsed.
     */
    public static int timeSeconds() {
        return _secondsNow;
    }

    /**
     * Returns number of game frames elapsed.
     */
    public static int now() {
        return _framesNow;
    }

    public static void cacheFrameNow() {
        _framesNow = Atlantis.game().getFrameCount();
    }

    /**
     * Return how many frames ago this moment was.
     */
    public static int framesAgo(int frame) {
        return now() - frame;
    }

    /**
     * Returns true once per n game frames.
     */
    public static boolean everyNthGameFrame(int n) {
        return Atlantis.game().getFrameCount() % n == 0;
    }

    /**
     * Returns false once per n game frames.
     */
    public static boolean notNthGameFrame(int n) {
        return Atlantis.game().getFrameCount() % n != 0;
    }

    /**
     * Number of minerals.
     */
    public static int minerals() {
        return Atlantis.game().self().minerals();
    }

    /**
     * Number of gas.
     */
    public static int gas() {
        return Atlantis.game().self().gas();
    }

    /**
     * Number of free supply.
     */
    public static int supplyFree() {
        return supplyTotal() - supplyUsed();
    }

    /**
     * Number of supply used.
     */
    public static int supplyUsed() {
        return Atlantis.game().self().supplyUsed() / 2;
    }

    /**
     * Number of supply totally available.
     */
    public static int supplyTotal() {
        return Atlantis.game().self().supplyTotal() / 2;
    }

    /**
     * Returns current APlayer.
     */
    public static APlayer getPlayerUs() {
        if (_our == null) {
            _our = new APlayer(Atlantis.game().self());
        }

        return _our;
    }

    /**
     * Returns all APlayers.
     */
    public static List<APlayer> getPlayers() {
        List<APlayer> players = new ArrayList<>();
        for (Player p : game().getPlayers()) {
            players.add(APlayer.create(p));
        }
        return players;
    }

    /**
     * Returns enemy APlayer.
     */
    public static APlayer enemy() {
        if (_enemy == null) {
            _enemy = new APlayer(Atlantis.game().enemies().iterator().next());
        }
        return _enemy;
    }

    /**
     * Returns neutral APlayer (minerals, geysers, critters).
     */
    public static APlayer neutralPlayer() {
        return new APlayer(Atlantis.game().neutral());
    }

    /**
     * UMS maps are custom made maps, which may be used to test micro-management. They can cause a lot of exceptions.
     */
    public static boolean isUms() {
        return umsMode;
    }

    /**
     * UMS maps are custom made maps, which may be used to test micro-management.
     */
    public static void setUmsMode() {
        if (MapSpecificCommander.shouldTreatAsNormalMap()) {
            return;
        }

        if (!AGame.umsMode) {
            AGame.umsMode = true;
            A.println("### UMS mode enabled! ###");

            MissionChanger.forceMissionAttack("UmsAlwaysAttack");
        }
    }

    // =========================================================
    // Auxiliary

    /**
     * Returns random int number from range [min, max], both inclusive.
     */
    public static int rand(int min, int max) {
        return A.rand(min, max);
    }

    public static int killsLossesResourceBalance() {
        return Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
    }

    public static String getMapName() {
        return Atlantis.game().mapName();
    }

    public static void calcSeconds() {
        _secondsNow = Atlantis.game().getFrameCount() / 30;
        A.s = _secondsNow;
        A.fr = _framesNow;
    }

    // =========================================================
    // Utility

    /**
     * Sends in-game message that will be visible by other APlayers.
     */
    public static void sendMessage(String message) {
        if (game() != null) {
            game().sendText(message);
        }
    }
}
