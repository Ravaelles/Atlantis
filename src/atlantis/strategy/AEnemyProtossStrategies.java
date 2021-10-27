package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.Select;


public class AEnemyProtossStrategies extends AEnemyStrategy {
    
    // Rush
    public static final AEnemyStrategy PROTOSS_2_Gate = new AEnemyProtossStrategies();
    
    // Cheese
    public static final AEnemyStrategy PROTOSS_3_Gate = new AEnemyProtossStrategies();
    
    // Expansion
    public static final AEnemyStrategy PROTOSS_12_Nexus = new AEnemyProtossStrategies();
    
    // Tech
    public static final AEnemyStrategy PROTOSS_2_Gate_DT = new AEnemyProtossStrategies();
    public static final AEnemyStrategy PROTOSS_Carrier_Push = new AEnemyProtossStrategies();
    
    // =========================================================

    protected static void initialize() {
        
        // === Rushes ========================================
        
        PROTOSS_2_Gate.setProtoss().setName("2 Gate")
                .setGoingRush()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_Zealot_(vs._Terran)");

        // === Cheese ========================================
        
        PROTOSS_3_Gate.setProtoss().setName("3 Gate")
                .setGoingRush().setGoingCheese()
                .setUrl("---");

        // === Expansion =====================================
        
        PROTOSS_12_Nexus.setProtoss().setName("12 Nexus")
                .setGoingExpansion()
                .setUrl("http://wiki.teamliquid.net/starcraft/12_Nexus");

        // === Tech ==========================================
        
        PROTOSS_2_Gate_DT.setProtoss().setName("2 Gate DT")
                .setGoingTech().setGoingHiddenUnits()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_DT");
        
        PROTOSS_Carrier_Push.setProtoss().setName("Carrier Push")
                .setGoingTech().setGoingAirUnitsLate()
                .setUrl("---");
    }
    
    // =========================================================
    
    public static AEnemyStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int gateways = Select.enemy().countUnitsOfType(AUnitType.Protoss_Gateway);
        int nexus = Select.enemy().countUnitsOfType(AUnitType.Protoss_Nexus);
        int citadel = Select.enemy().countUnitsOfType(AUnitType.Protoss_Citadel_of_Adun);

        // === Dark Templar ========================================
        
        if (citadel >= 1 && seconds < 320) {
            return AEnemyProtossStrategies.PROTOSS_2_Gate_DT;
        }

        // === Three Gateway =======================================
        
        if (gateways >= 3 && seconds < 300) {
            return AEnemyProtossStrategies.PROTOSS_3_Gate;
        }

        // === Two Gateway =========================================
        
        if (gateways == 2 && seconds < 290) {
            return AEnemyProtossStrategies.PROTOSS_2_Gate;
        }

        // === 12 Nexus ============================================
        
        if (nexus == 2 && seconds < 290) {
            return AEnemyProtossStrategies.PROTOSS_12_Nexus;
        }
        
        // === Carrier Push ========================================
        
        int cannons = Select.enemy().countUnitsOfType(AUnitType.Protoss_Photon_Cannon);
        if (cannons >= 1 && nexus >= 2) {
            return PROTOSS_Carrier_Push;
        }
        
        // =========================================================
        
        return null;
    }
    
}
