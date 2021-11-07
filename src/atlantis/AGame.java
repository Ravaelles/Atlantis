package atlantis;

import static atlantis.Atlantis.game;

import atlantis.combat.missions.MissionChanger;
import atlantis.production.Requirements;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.*;

import java.util.List;

/**
 * Represents various aspect of the game like time elapsed (in frames or approximated seconds), free supply
 * (from our point of view), game speed, enemy player etc.<br />
 * <br /><b>It's worth to study this class carefully as it contains some really useful methods.</b>
 */
public class AGame {

    private static boolean umsMode = false; // Should be set to `true` on UMS (custom) maps
    private static Player _enemy = null; // Cached enemy player

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
        System.err.println(message);
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
        return Atlantis.game().getFrameCount() / 30;
    }

    /**
     * Returns number of frames elapsed.
     */
    public static int getTimeFrames() {
        return Atlantis.game().getFrameCount();
    }

    /**
     * Current timestamp in frames.
     */
    public static int now() {
        return getTimeFrames();
    }

    /**
     * Return how many frames ago this moment was.
     */
    public static int framesAgo(int frame) {
        return getTimeFrames() - frame;
    }

    /**
     * Returns true once per n game frames.
     */
    public static boolean everyNthGameFrame(int n) {
        return Atlantis.game().getFrameCount() % n == 0;
    }

    /**
     * Returns true once per n game frames.
     */
    public static boolean notNthGameFrame(int n) {
        return Atlantis.game().getFrameCount() % n != 0;
    }

    /**
     * Number of minerals.
     */
    public static int getMinerals() {
        return Atlantis.game().self().minerals();
    }

    /**
     * Number of gas.
     */
    public static int getGas() {
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

    public static boolean hasSupply(int minSupply) {
        return supplyUsed() >= minSupply;
    }

    /**
     * Number of supply totally available.
     */
    public static int supplyTotal() {
        return Atlantis.game().self().supplyTotal() / 2;
    }

    /**
     * Returns current player.
     */
    public static Player getPlayerUs() {
        return Atlantis.game().self();
    }

    /**
     * Returns all players.
     */
    public static List<Player> getPlayers() {
        return Atlantis.game().getPlayers();
    }

    /**
     * Returns enemy player.
     */
    public static Player enemy() {
        if (_enemy == null) {
            _enemy = Atlantis.game().enemies().iterator().next();
        }
        return _enemy;
    }

    /**
     * Returns enemy player.
     */
    public static Player getEnemy() {
        if (_enemy == null) {
            _enemy = Atlantis.game().enemies().iterator().next();
        }
        return _enemy;
    }

    /**
     * Returns neutral player (minerals, geysers, critters).
     */
    public static Player getNeutralPlayer() {
        return Atlantis.game().neutral();
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
    public static void setUmsMode(boolean umsMode) {
        if (AGame.umsMode != umsMode) {
            AGame.umsMode = umsMode;
            System.out.println();
            System.out.println("### UMS mode enabled! ###");
            System.out.println();

            MissionChanger.forceMissionAttack();
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

    /**
     * Returns true if user plays as Terran.
     */
    public static boolean isPlayingAsTerran() {
        return AtlantisConfig.MY_RACE.equals(Race.Terran);
    }

    /**
     * Returns true if user plays as Protoss.
     */
    public static boolean isPlayingAsProtoss() {
        return AtlantisConfig.MY_RACE.equals(Race.Protoss);
    }

    /**
     * Returns true if user plays as Zerg.
     */
    public static boolean isPlayingAsZerg() {
        return AtlantisConfig.MY_RACE.equals(Race.Zerg);
    }

    /**
     * Returns true if enemy plays as Terran.
     */
    public static boolean isEnemyTerran() {
        return AGame.enemy().getRace().equals(Race.Terran);
    }

    /**
     * Returns true if enemy plays as Protoss.
     */
    public static boolean isEnemyProtoss() {
        return AGame.enemy().getRace().equals(Race.Protoss);
    }

    /**
     * Returns true if enemy plays as Zerg.
     */
    public static boolean isEnemyZerg() {
        return AGame.enemy().getRace().equals(Race.Zerg);
    }

    /**
     * Returns true if we can afford given amount of minerals.
     */
    public static boolean hasMinerals(int mineralsToAfford) {
        return getMinerals() >= mineralsToAfford;
    }

    /**
     * Returns true if we can afford given amount of gas.
     */
    public static boolean hasGas(int gasToAfford) {
        return getGas() >= gasToAfford;
    }

    /**
     * Returns true if we can afford minerals and gas for given unit type.
     */
    public static boolean canAfford(AUnitType unitType) {
        return hasMinerals(unitType.getMineralPrice()) && hasGas(unitType.getGasPrice());
    }

    /**
     * Returns true if we can afford minerals and gas for given upgrade.
     */
    public static boolean canAfford(UpgradeType upgrade) {
        //TODO: check whether we need to pass level 0 to match getMineral/GasPriceBase()
        return hasMinerals(upgrade.mineralPrice()) && hasGas(upgrade.gasPrice());
    }

    /**
     * Returns true if we can afford both so many minerals and gas at the same time.
     */
    public static boolean canAfford(int minerals, int gas) {
        return hasMinerals(minerals) && hasGas(gas);
    }

    /**
     * Returns true if we can afford both so many minerals and gas at the same time.
     * Takes into account planned constructions and orders.
     */
    public static boolean canAffordWithReserved(int minerals, int gas) {
        int[] reserved = AConstructionRequests.resourcesNeededForNotStartedConstructions();

        return canAfford(
                minerals + reserved[0],
                gas + reserved[1]
        );
    }

    public static boolean canAffordWithReserved(AUnitType type) {
        return canAffordWithReserved(type.getMineralPrice(), type.getGasPrice());
    }

    public static int killsLossesResourceBalance() {
        return Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
    }

    public static String getMapName() {
        return Atlantis.game().mapName();
    }

    // =========================================================
    // Utility
    /**
     * Sends in-game message that will be visible by other players.
     */
    public static void sendMessage(String message) {
        if (game() != null) {
            game().sendText(message);
        }
    }

}
