package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class ProtossStrategies extends AStrategy {
    
    // Rush
    public static final AStrategy PROTOSS_2_Gate_Zealot = new AStrategy();
    
    // Cheese
    public static final AStrategy PROTOSS_3_Gate = new AStrategy();
    
    // Expansion
    public static final AStrategy PROTOSS_12_Nexus = new AStrategy();
    
    // Tech
    public static final AStrategy PROTOSS_2_Gate_Range_Expand = new AStrategy();
    public static final AStrategy PROTOSS_2_Gate_DT = new AStrategy();
    public static final AStrategy PROTOSS_Carrier_Push = new AStrategy();
    
    // =========================================================

    public static void initialize() {
        
        // === Rushes ========================================
        
        PROTOSS_2_Gate_Zealot.setProtoss().setName("2 Gate Zealot")
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
        
        PROTOSS_2_Gate_Range_Expand.setProtoss().setName("2 Gate Range Expand")
                .setGoingTech()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_Range_Expand");

        PROTOSS_2_Gate_DT.setProtoss().setName("2 Gate DT")
                .setGoingTech().setGoingHiddenUnits()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_DT");

        PROTOSS_Carrier_Push.setProtoss().setName("Carrier Push")
                .setGoingTech().setGoingAirUnitsLate()
                .setUrl("---");
    }
    
    // =========================================================
    
    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int gateways = Select.enemy().countUnitsOfType(AUnitType.Protoss_Gateway);
        int nexus = Select.enemy().countUnitsOfType(AUnitType.Protoss_Nexus);
        int citadel = Select.enemy().countUnitsOfType(AUnitType.Protoss_Citadel_of_Adun);

        // === Dark Templar ========================================
        
        if (citadel >= 1 && seconds < 320) {
            return ProtossStrategies.PROTOSS_2_Gate_DT;
        }

        // === Three Gateway =======================================
        
        if (gateways >= 3 && seconds < 300) {
            return ProtossStrategies.PROTOSS_3_Gate;
        }

        // === Two Gateway =========================================
        
        if (gateways == 2 && seconds < 290) {
            return ProtossStrategies.PROTOSS_2_Gate_Zealot;
        }

        // === 12 Nexus ============================================
        
        if (nexus == 2 && seconds < 290) {
            return ProtossStrategies.PROTOSS_12_Nexus;
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
