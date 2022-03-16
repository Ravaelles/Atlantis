package atlantis.information.strategy;

import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnitType;

public class ZergStrategies extends AStrategy {
    
    // Rush
    public static final AStrategy ZERG_9_Pool_vP = new AStrategy();
    public static final AStrategy ZERG_9_Pool_vT = new AStrategy();
    public static final AStrategy ZERG_9_Pool_vZ = new AStrategy();

    // Cheese
    public static final AStrategy ZERG_4_Pool = new AStrategy();
    public static final AStrategy ZERG_5_Pool = new AStrategy();
    public static final AStrategy ZERG_6_Pool = new AStrategy();
    public static final AStrategy ZERG_9_Hatchery = new AStrategy();

    // Expansion
    public static final AStrategy ZERG_3_Hatch_Before_Pool = new AStrategy();
    public static final AStrategy ZERG_12_Hatch_vZ = new AStrategy();

    // Tech
    public static final AStrategy ZERG_1_Hatch_Lurker = new AStrategy();
    public static final AStrategy ZERG_2_Hatch_Lurker = new AStrategy();
    public static final AStrategy ZERG_13_Pool_Muta = new AStrategy();
    public static final AStrategy ZERG_2_Hatch_Hydra_vP = new AStrategy();

    // =========================================================

    public static void initialize() {
        
        // === Rushes ========================================
        
        ZERG_9_Pool_vP.setZerg().setName("9 Pool vP").setGoingRush();

        ZERG_9_Pool_vT.setZerg().setName("9 Pool vT").setGoingRush();

        ZERG_9_Pool_vZ.setZerg().setName("9 Pool vZ").setGoingRush();

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
        
        ZERG_3_Hatch_Before_Pool.setZerg().setName("3 Hatch Before Pool")
                .setGoingExpansion()
                .setUrl("http://wiki.teamliquid.net/starcraft/3_Hatch_Before_Pool_(vs._Terran)");

        ZERG_12_Hatch_vZ.setZerg().setName("12 Hatch vZ")
                .setGoingExpansion()
                .setUrl("12 Hatch vZ");

        // === Tech ==========================================
        
        ZERG_1_Hatch_Lurker.setZerg().setName("1 Hatch Lurker")
                .setGoingTech().setGoingHiddenUnits();

        ZERG_2_Hatch_Lurker.setZerg().setName("2 Hatch Lurker")
                .setGoingTech().setGoingHiddenUnits();

        ZERG_13_Pool_Muta.setZerg().setName("13 Pool Muta")
                .setGoingTech().setGoingAirUnitsQuickly();

        ZERG_2_Hatch_Hydra_vP.setZerg().setName("2 Hatch Hydra")
                .setGoingTech();

    }
    
    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int bases = EnemyUnits.discovered().bases().count();
        int lair = count(AUnitType.Zerg_Lair);
        int pool = count(AUnitType.Zerg_Spawning_Pool);
        int extractor = count(AUnitType.Zerg_Extractor);
        int spires = count(AUnitType.Zerg_Spire);
        int mutalisks = count(AUnitType.Zerg_Mutalisk);
        int hydraliskDen = count(AUnitType.Zerg_Hydralisk_Den);
        int drones = count(AUnitType.Zerg_Drone);
        int lings = count(AUnitType.Zerg_Zergling);
        
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
            return ZergStrategies.ZERG_9_Pool_vP;
        }
        
        // =========================================================
        
        return null;
    }
    
}
