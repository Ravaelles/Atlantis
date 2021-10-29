package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;


public class ZergStrategies extends AStrategy {
    
    // Rush
    public static final AStrategy ZERG_9_Pool = new AStrategy();
    
    // Cheese
    public static final AStrategy ZERG_4_Pool = new AStrategy();
    public static final AStrategy ZERG_5_Pool = new AStrategy();
    public static final AStrategy ZERG_6_Pool = new AStrategy();
    public static final AStrategy ZERG_9_Hatchery = new AStrategy();

    // Expansion
    public static final AStrategy ZERG_3_Hatch_Before_Pool = new AStrategy();
    
    // Tech
    public static final AStrategy ZERG_1_Hatch_Lurker = new AStrategy();
    public static final AStrategy ZERG_2_Hatch_Lurker = new AStrategy();
    public static final AStrategy ZERG_13_Pool_Muta = new AStrategy();
    
    // =========================================================

    public static void initialize() {
        
        // === Rushes ========================================
        
        ZERG_9_Pool.setZerg().setName("9 Pool")
                .setGoingRush()
                .setUrl("https://liquipedia.net/starcraft/9_Pool_(vs._Protoss)");

        // === Cheese ========================================
        
        ZERG_4_Pool.setZerg().setName("4 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/4/5_Pool");
        
        ZERG_5_Pool.setZerg().setName("5 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/5_Pool_(vs._Terran)");
        
        ZERG_6_Pool.setZerg().setName("6 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("---");

        // === Expansion =====================================
        
        ZERG_3_Hatch_Before_Pool.setZerg().setName("3_Hatch_Before_Pool")
                .setGoingExpansion()
                .setUrl("http://wiki.teamliquid.net/starcraft/3_Hatch_Before_Pool_(vs._Terran)");

        // === Tech ==========================================
        
        ZERG_1_Hatch_Lurker.setZerg().setName("1 Hatch Lurker")
                .setGoingTech().setGoingHiddenUnits()
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Hatch_Lurker");
        
        ZERG_2_Hatch_Lurker.setZerg().setName("2 Hatch Lurker")
                .setGoingTech().setGoingHiddenUnits()
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Hatch_Lurker");
        
        ZERG_13_Pool_Muta.setZerg().setName("13 Pool Muta")
                .setGoingTech().setGoingAirUnitsQuickly()
                .setUrl("http://wiki.teamliquid.net/starcraft/13_Pool_Muta_(vs._Terran)");
        
    }
    
    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int bases = Select.enemy().bases().count();
        int lair = Select.enemy().countUnitsOfType(AUnitType.Zerg_Lair);
        int pool = Select.enemy().countUnitsOfType(AUnitType.Zerg_Spawning_Pool);
        int extractor = Select.enemy().countUnitsOfType(AUnitType.Zerg_Extractor);
        int spires = Select.enemy().countUnitsOfType(AUnitType.Zerg_Spire);
        int mutalisks = Select.enemy().countUnitsOfType(AUnitType.Zerg_Mutalisk);
        int hydraliskDen = Select.enemy().countUnitsOfType(AUnitType.Zerg_Hydralisk_Den);
        int drones = Select.enemy().countUnitsOfType(AUnitType.Zerg_Drone);
        int lings = Select.enemy().countUnitsOfType(AUnitType.Zerg_Zergling);
        
        // === Expansion ===========================================
        
        if (pool == 0 && bases >= 3 && seconds <= 350) {
            return ZERG_3_Hatch_Before_Pool;
        }
        
        // === Tech ================================================
        
        if (extractor >= 1 && hydraliskDen == 0 && bases >= 2 && drones >= 12 || spires >= 1 || mutalisks >= 1) {
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
            return ZergStrategies.ZERG_4_Pool;
        }
        
        if (pool == 1 && drones <= 5 && seconds < 140) {
            return ZergStrategies.ZERG_5_Pool;
        }
        
        if (pool == 1 && drones <= 6 && seconds < 160) {
            return ZergStrategies.ZERG_6_Pool;
        }
        
        // === Rushes ==============================================
        
        if (pool == 1 && drones <= 10 && seconds < 220) {
            return ZergStrategies.ZERG_9_Pool;
        }
        
        // =========================================================
        
        return null;
    }
    
}
