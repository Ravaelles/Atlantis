package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AEnemyZergStrategy extends AEnemyStrategy {
    
    // Rush
    public static final AEnemyStrategy ZERG_9_Pool = new AEnemyZergStrategy();
    
    // Cheese
    public static final AEnemyStrategy ZERG_4_Pool = new AEnemyZergStrategy();
    public static final AEnemyStrategy ZERG_5_Pool = new AEnemyZergStrategy();
    public static final AEnemyStrategy ZERG_6_Pool = new AEnemyZergStrategy();
    
    // Expansion
    public static final AEnemyStrategy ZERG_3_Hatch_Before_Pool = new AEnemyZergStrategy();
    
    // Tech
    public static final AEnemyStrategy ZERG_1_Hatch_Lurker = new AEnemyZergStrategy();
    public static final AEnemyStrategy ZERG_2_Hatch_Lurker = new AEnemyZergStrategy();
    public static final AEnemyStrategy ZERG_13_Pool_Muta = new AEnemyZergStrategy();
    
    // =========================================================

    protected static void initialize() {
        
        // === Rushes ========================================
        
        ZERG_9_Pool.setProtoss().setName("9 Pool")
                .setGoingRush()
                .setUrl("http://wiki.teamliquid.net/starcraft/9_Pool_(vs._Terran)");

        // === Cheese ========================================
        
        ZERG_4_Pool.setProtoss().setName("4 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/4/5_Pool");
        
        ZERG_5_Pool.setProtoss().setName("5 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/5_Pool_(vs._Terran)");
        
        ZERG_6_Pool.setProtoss().setName("6 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("---");

        // === Expansion =====================================
        
        ZERG_3_Hatch_Before_Pool.setProtoss().setName("3_Hatch_Before_Pool")
                .setGoingExpansion()
                .setUrl("http://wiki.teamliquid.net/starcraft/3_Hatch_Before_Pool_(vs._Terran)");

        // === Tech ==========================================
        
        ZERG_1_Hatch_Lurker.setProtoss().setName("1 Hatch Lurker")
                .setGoingTech().setGoingHiddenUnits()
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Hatch_Lurker");
        
        ZERG_2_Hatch_Lurker.setProtoss().setName("1 Hatch Lurker")
                .setGoingTech().setGoingHiddenUnits()
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Hatch_Lurker");
        
        ZERG_13_Pool_Muta.setProtoss().setName("13 Pool Muta")
                .setGoingTech().setGoingAirUnitsQuickly()
                .setUrl("http://wiki.teamliquid.net/starcraft/13_Pool_Muta_(vs._Terran)");
        
    }
    
    public static AEnemyStrategy detectStrategy() {
        int seconds = AGame.getTimeSeconds();
        int bases = Select.enemy().bases().count();
        int lair = Select.enemy().countUnitsOfType(AUnitType.Zerg_Lair);
        int pool = Select.enemy().countUnitsOfType(AUnitType.Zerg_Spawning_Pool);
        int extractor = Select.enemy().countUnitsOfType(AUnitType.Zerg_Extractor);
        int spires = Select.enemy().countUnitsOfType(AUnitType.Zerg_Spire);
        int hydraliskDen = Select.enemy().countUnitsOfType(AUnitType.Zerg_Hydralisk_Den);
        int drones = Select.enemy().countUnitsOfType(AUnitType.Zerg_Drone);
        int lings = Select.enemy().countUnitsOfType(AUnitType.Zerg_Zergling);
        
        // === Expansion ===========================================
        
        if (pool == 0 && bases >= 3 && seconds <= 350) {
            return ZERG_3_Hatch_Before_Pool;
        }
        
        // === Tech ================================================
        
        if (extractor >= 1 && hydraliskDen == 0 && bases >= 2 && drones >= 12 || spires >= 1) {
            return ZERG_13_Pool_Muta;
        }
        
        if (extractor >= 1 && pool >= 1 && lair >= 1 && bases < 2) {
            return ZERG_1_Hatch_Lurker;
        }
        
        if (extractor >= 1 && pool >= 1 && lair >= 1 && bases >= 2) {
            return ZERG_2_Hatch_Lurker;
        }
        
        // === Cheese ==============================================
        
        if (pool == 1 && drones <= 4 && seconds < 120) {
            return AEnemyZergStrategy.ZERG_4_Pool;
        }
        
        if (pool == 1 && drones <= 5 && seconds < 140) {
            return AEnemyZergStrategy.ZERG_5_Pool;
        }
        
        if (pool == 1 && drones <= 6 && seconds < 160) {
            return AEnemyZergStrategy.ZERG_6_Pool;
        }
        
        // === Rushes ==============================================
        
        if (pool == 1 && drones <= 10 && seconds < 220) {
            return AEnemyZergStrategy.ZERG_9_Pool;
        }
        
        // =========================================================
        
        return null;
    }
    
}
