package atlantis;

import static atlantis.Atlantis.game;

import atlantis.combat.missions.MissionChanger;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.AUtil;
import atlantis.wrappers.ATech;
import bwapi.*;

import java.util.List;

/**
 * Represents various aspect of the game like time elapsed (in frames or approximated seconds), free supply
 * (from our point of view), game speed, enemy player etc.<br />
 * <br /><b>It's worth to study this class carefully as it contains some really useful methods.</b>
 */
public class AGame {

    private static boolean umtMode = false; // Should be set to `true` on UMT (custom) maps
    private static boolean isPaused = false; // On PauseBreak a pause mode can be enabled
    private static Player _enemy = null; // Cached enemy player

    // =========================================================
    /**
     * Returns object that is responsible for the production queue.
     */
//    public static AProductionQueue getBuildOrders() {
//        return AtlantisConfig.getBuildOrders();
//    }

    /**
     * Returns true if we have all techs needed for given unit (but we may NOT have some of the buildings!).
     */
    public static boolean hasTechToProduce(AUnitType unitType) {

        // Needs to have tech
        TechType techType = unitType.getRequiredTech();
        if (techType != null && techType != TechType.None && !ATech.isResearched(techType)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if we have all buildings needed for given unit.
     *
     * @param countUnfinished if true, then if it will require required units to be finished to return
     * true e.g. to produce Zealot you need at least one finished Gateway
     */
    public static boolean hasBuildingsToProduce(AUnitType unitType, boolean countUnfinished) {

        // Need to have every prerequisite building
        for (AUnitType requiredType : unitType.getRequiredUnits().keySet()) {
            if (requiredType.equals(AUnitType.Zerg_Larva)) {
                continue;
            }

            int requiredAmount = unitType.getRequiredUnits().get(requiredType);
            int weHaveAmount = requiredType.equals(AUnitType.Zerg_Larva)
                    ? Select.ourLarva().count() : Select.our().ofType(requiredType).count();
            if (weHaveAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if it's possible to produce unit (or building) of given type.
     */
    public static boolean hasTechAndBuildingsToProduce(AUnitType unitType) {
        return hasTechToProduce(unitType) && hasBuildingsToProduce(unitType, true);
    }

    // =========================================================
    
    /**
     * Quits the game gently, killing all processes and cleaning up.
     */
    public static void exit() {
        Atlantis.getInstance().onEnd(false);
    }

    /**
     * Quits the game gently, killing all processes and cleaning up.
     */
    public static void exit(String message) {
        System.err.println(message);
        Atlantis.getInstance().onEnd(false);
    }

    /**
     * Enable/disable pause.
     */
    public static void pauseModeToggle() {
        isPaused = !isPaused;
    }
    
    /**
     * Returns true if game is paused.
     */
    public static boolean isPaused() {
        return isPaused;
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
        return AGameSpeed.gameSpeed;
    }

    /**
     * Returns approximate number of in-game seconds elapsed.
     */
    public static int getTimeSeconds() {
        return Atlantis.game().getFrameCount() / 30;
    }

    /**
     * Returns number of frames elapsed.
     */
    public static int getTimeFrames() {
        return Atlantis.game().getFrameCount();
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
    public static int getSupplyFree() {
        return getSupplyTotal() - getSupplyUsed();
    }

    /**
     * Number of supply used.
     */
    public static int getSupplyUsed() {
        return Atlantis.game().self().supplyUsed() / 2;
    }

    public static boolean hasSupply(int minSupply) {
        return getSupplyUsed() >= minSupply;
    }

    /**
     * Number of supply totally available.
     */
    public static int getSupplyTotal() {
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
     * UMT maps are custom made maps, which may be used to test micro-management.
     */
    public static boolean isUmtMode() {
        return umtMode;
    }

    /**
     * UMT maps are custom made maps, which may be used to test micro-management.
     */
    public static void setUmtMode(boolean umtMode) {
        if (AGame.umtMode != umtMode) {
            AGame.umtMode = umtMode;
            System.out.println();
            System.out.println("### UMT mode enabled! ###");
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
        return AUtil.rand(min, max);
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

    public static int killsLossesResourceBalance() {
        return Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
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
