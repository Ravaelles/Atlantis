package atlantis;

import static atlantis.Atlantis.getBwapi;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import atlantis.wrappers.ATech;
import bwapi.Player;
import bwapi.Race;
import bwapi.TechType;
import bwapi.UpgradeType;
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
//    public static ABuildOrderManager getBuildOrders() {
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
     * @param boolean countUnfinished if true, then if it will require required units to be finished to return
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
    
    /**
     * Changes game speed. 0 - fastest 1 - very quick 20 - around default
     */
    public static void changeSpeedTo(int speed) {
        if (speed < 0) {
            speed = 0;
        }

        AtlantisConfig.GAME_SPEED = speed;
        
        try {
            getBwapi().setLocalSpeed(AtlantisConfig.GAME_SPEED);
            Thread.sleep(40);
            getBwapi().setLocalSpeed(AtlantisConfig.GAME_SPEED);
            Thread.sleep(40);
        } catch (InterruptedException ex) { 
            // Ignore
        }
        getBwapi().setLocalSpeed(AtlantisConfig.GAME_SPEED);

        String speedString = AtlantisConfig.GAME_SPEED + (AtlantisConfig.GAME_SPEED == 0 ? " (Max)" : "");
        sendMessage("Game speed: " + speedString);
    }

    /**
     * Changes game speed by given ammount of units. Total game speed: 0 - fastest 1 - very quick 20 - around
     * default
     */
    public static void changeSpeedBy(int deltaSpeed) {
        int speed = AtlantisConfig.GAME_SPEED + deltaSpeed;
        if (speed < 0) {
            speed = 0;
        }

        if (getBwapi() != null) {
            AtlantisConfig.GAME_SPEED = speed;
            changeSpeedTo(AtlantisConfig.GAME_SPEED);
        }
        else {
            System.err.println("Can't change game speed, bwapi is null.");
        }
    }

    /**
     * Returns game speed.
     */
    public static int getGameSpeed() {
        return AtlantisConfig.GAME_SPEED;
    }

    /**
     * Returns approximate number of in-game seconds elapsed.
     */
    public static int getTimeSeconds() {
        return Atlantis.getBwapi().getFrameCount() / 30;
    }

    /**
     * Returns number of frames elapsed.
     */
    public static int getTimeFrames() {
        return Atlantis.getBwapi().getFrameCount();
    }

    /**
     * Number of minerals.
     */
    public static int getMinerals() {
        return Atlantis.getBwapi().self().minerals();
    }

    /**
     * Number of gas.
     */
    public static int getGas() {
        return Atlantis.getBwapi().self().gas();
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
        return Atlantis.getBwapi().self().supplyUsed() / 2;
    }

    /**
     * Number of supply totally available.
     */
    public static int getSupplyTotal() {
        return Atlantis.getBwapi().self().supplyTotal() / 2;
    }

    /**
     * Returns current player.
     */
    public static Player getPlayerUs() {
        return Atlantis.getBwapi().self();
    }

    /**
     * Returns all players.
     */
    public static List<Player> getPlayers() {
        return Atlantis.getBwapi().getPlayers();
    }

    /**
     * Returns enemy player.
     */
    public static Player enemy() {
        if (_enemy == null) {
            _enemy = Atlantis.getBwapi().enemies().iterator().next();
        }
        return _enemy;
    }

    /**
     * Returns enemy player.
     */
    public static Player getEnemy() {
        if (_enemy == null) {
            _enemy = Atlantis.getBwapi().enemies().iterator().next();
        }
        return _enemy;
    }

    /**
     * Returns neutral player (minerals, geysers, critters).
     */
    public static Player getNeutralPlayer() {
        return Atlantis.getBwapi().neutral();
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
        AGame.umtMode = umtMode;
        if (umtMode) {
            System.out.println();
            System.out.println("### UMT mode enabled! ###");
            System.out.println();
        }
    }
    
    // =========================================================
    // Auxiliary
    /**
     * Returns random int number from range [min, max], both inclusive.
     */
    public static int rand(int min, int max) {
        return AtlantisUtilities.rand(min, max);
    }

    /**
     * Returns true if user plays as Terran.
     */
    public static boolean playsAsTerran() {
        return AtlantisConfig.MY_RACE.equals(Race.Terran);
    }

    /**
     * Returns true if user plays as Protoss.
     */
    public static boolean playsAsProtoss() {
        return AtlantisConfig.MY_RACE.equals(Race.Protoss);
    }

    /**
     * Returns true if user plays as Zerg.
     */
    public static boolean playsAsZerg() {
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

    // =========================================================
    // Utility
    /**
     * Sends in-game message that will be visible by other players.
     */
    public static void sendMessage(String message) {
        if (getBwapi() != null) {
            getBwapi().sendText(message);
        }
    }

}
