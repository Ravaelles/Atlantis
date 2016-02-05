package atlantis;

import static atlantis.Atlantis.getBwapi;
import atlantis.production.strategies.AtlantisProductionStrategy;
import atlantis.util.RUtilities;
import atlantis.wrappers.AtlantisTech;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Player;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.TechType;
import jnibwapi.types.UnitType;
import jnibwapi.types.UpgradeType;

/**
 * Represents various aspect of the game like time elapsed (in frames or approximated seconds),
 * free supply (from our point of view), game speed, enemy player etc.<br />
 * <br /><b>It's worth to study this class carefully as it contains some really useful methods.</b>
 */
public class AtlantisGame {

    private static Player _enemy = null; // Cached enemy player

    // =========================================================
    
    /**
     * Returns object that is responsible for the production queue.
     */
    public static AtlantisProductionStrategy getProductionStrategy() {
        return AtlantisConfig.getProductionStrategy();
    }

    /**
     * Returns true if we have all techs needed for given unit (but we may NOT have some of the buildings!).
     */
    public static boolean hasTechToProduce(UnitType unitType) {

        // Needs to have tech
        TechType techType = TechType.TechTypes.getTechType(unitType.getRequiredTechID());
        if (techType != null && techType != TechType.TechTypes.None && !AtlantisTech.isResearched(techType)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if we have all buildings needed for given unit.
     */
    public static boolean hasBuildingsToProduce(UnitType unitType) {

        // Need to have every prerequisite building
        for (Integer unitTypeID : unitType.getRequiredUnits().keySet()) {
            UnitType requiredUnitType = UnitType.getByID(unitTypeID);
            
//            if (requiredUnitType.isLarva()) {
//                continue;
//            }
//            System.out.println("=req: " + requiredUnitType);
            if (!requiredUnitType.isBuilding()) {
//                System.out.println("  continue");
                continue;
            }
            
            int requiredAmount = unitType.getRequiredUnits().get(unitTypeID);
            int weHaveAmount = requiredUnitType.isLarva() ? 
                    SelectUnits.ourLarva().count() : SelectUnits.our().ofType(requiredUnitType).count();
//            System.out.println(requiredUnitType + "    x" + requiredAmount);
//            System.out.println("   and we have: " + weHaveAmount);
            if (weHaveAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if it's possible to produce unit (or building) of given type.
     */
    public static boolean hasTechAndBuildingsToProduce(UnitType unitType) {
        return hasTechToProduce(unitType) && hasBuildingsToProduce(unitType);
    }

    // =========================================================
    
    /**
     * Changes game speed. 0 - fastest 1 - very quick 20 - around default
     */
    public static void changeSpeed(int speed) {
        AtlantisConfig.GAME_SPEED = speed;
        getBwapi().setGameSpeed(AtlantisConfig.GAME_SPEED);
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
        return Atlantis.getBwapi().getSelf().getMinerals();
    }

    /**
     * Number of gas.
     */
    public static int getGas() {
        return Atlantis.getBwapi().getSelf().getGas();
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
        return Atlantis.getBwapi().getSelf().getSupplyUsed() / 2;
    }

    /**
     * Number of supply totally available.
     */
    public static int getSupplyTotal() {
        return Atlantis.getBwapi().getSelf().getSupplyTotal() / 2;
    }

    /**
     * Returns current player.
     */
    public static Player getPlayerUs() {
        return Atlantis.getBwapi().getSelf();
    }

    /**
     * Returns enemy player.
     */
    public static Player enemy() {
        if (_enemy == null) {
            _enemy = Atlantis.getBwapi().getEnemies().iterator().next();
        }
        return _enemy;
    }

    /**
     * Returns enemy player.
     */
    public static Player getEnemy() {
        if (_enemy == null) {
            _enemy = Atlantis.getBwapi().getEnemies().iterator().next();
        }
        return _enemy;
    }

    // =========================================================
    // Auxiliary
    
    /**
     * Returns random int number from range [min, max], both inclusive.
     */
    public static int rand(int min, int max) {
        return RUtilities.rand(min, max);
    }

    /**
     * Returns true if user plays as Terran.
     */
    public static boolean playsAsTerran() {
        return AtlantisConfig.MY_RACE.equals(RaceTypes.Terran);
    }

    /**
     * Returns true if user plays as Protoss.
     */
    public static boolean playsAsProtoss() {
        return AtlantisConfig.MY_RACE.equals(RaceTypes.Protoss);
    }

    /**
     * Returns true if user plays as Zerg.
     */
    public static boolean playsAsZerg() {
        return AtlantisConfig.MY_RACE.equals(RaceTypes.Zerg);
    }

    /**
     * Returns true if enemy plays as Terran.
     */
    public static boolean isEnemyTerran() {
        return AtlantisGame.enemy().getRace().equals(RaceTypes.Terran);
    }

    /**
     * Returns true if enemy plays as Protoss.
     */
    public static boolean isEnemyProtoss() {
        return AtlantisGame.enemy().getRace().equals(RaceTypes.Protoss);
    }

    /**
     * Returns true if enemy plays as Zerg.
     */
    public static boolean isEnemyZerg() {
        return AtlantisGame.enemy().getRace().equals(RaceTypes.Zerg);
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
    public static boolean canAfford(UnitType unitType) {
        return hasMinerals(unitType.getMineralPrice()) && hasGas(unitType.getGasPrice());
    }

    /**
     * Returns true if we can afford minerals and gas for given upgrade.
     */
    public static boolean canAfford(UpgradeType upgrade) {
        return hasMinerals(upgrade.getMineralPriceBase()) && hasGas(upgrade.getGasPriceBase());
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
        getBwapi().sendText(message);
    }
    
}
