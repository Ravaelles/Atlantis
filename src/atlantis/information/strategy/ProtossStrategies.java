package atlantis.information.strategy;

import atlantis.game.AGame;
import atlantis.units.AUnitType;


public class ProtossStrategies extends AStrategy {

    // Standard
    public static final AStrategy PROTOSS_Zealot_Core_Zealot = new AStrategy();
    public static final AStrategy PROTOSS_ZZZ_Core = new AStrategy();

    // Rush
    public static final AStrategy PROTOSS_2_Gate_Zealot_vP = new AStrategy();
    public static final AStrategy PROTOSS_2_Gate_Zealot_vZ = new AStrategy();
    public static final AStrategy PROTOSS_Speedzealot = new AStrategy();
    public static final AStrategy PROTOSS_One_Base_Speedzealot = new AStrategy();

    // Cheese
    public static final AStrategy PROTOSS_3_Gate = new AStrategy();
    
    // Expansion
    public static final AStrategy PROTOSS_12_Nexus = new AStrategy();
    
    // Tech
    public static final AStrategy PROTOSS_2_Gate_Range_Expand = new AStrategy();
    public static final AStrategy PROTOSS_2_Gate_DT = new AStrategy();
    public static final AStrategy PROTOSS_Carrier_Push = new AStrategy();
    public static final AStrategy PROTOSS_Fast_DT = new AStrategy();

    // =========================================================

    public static void initialize() {

        // === Standard ======================================

        PROTOSS_Zealot_Core_Zealot.setProtoss().setName("Zealot Core Zealot")
                .setUrl("https://liquipedia.net/starcraft/1_Gate_Core_(vs._Protoss)");
        PROTOSS_ZZZ_Core.setProtoss().setName("ZZZ Core"); // Zealot-Zealot-Zealot-Core

        // === Rushes ========================================

        PROTOSS_2_Gate_Zealot_vP.setProtoss().setName("2 Gate Zealot vP")
                .setGoingRush()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_Zealot_(vs._Terran)");
        PROTOSS_2_Gate_Zealot_vZ.setProtoss().setName("2 Gate Zealot vZ")
                .setGoingRush()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_Zealot_(vs._Terran)");

        PROTOSS_Speedzealot.setProtoss().setName("+1 Speedzealot")
                .setGoingRush()
                .setUrl("https://liquipedia.net/starcraft/%2B1_Speedzealot_(vs_Zerg)");

        PROTOSS_One_Base_Speedzealot.setProtoss().setName("One Base Speedzealot")
                .setGoingRush()
                .setUrl("https://liquipedia.net/starcraft/One_Base_Speedzeal_(vs._Zerg)");

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

        PROTOSS_Fast_DT.setProtoss().setName("Fast DT")
                .setGoingTech().setGoingRush()
                .setUrl("---");
    }
    
    // =========================================================
    
    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int gateways = count(AUnitType.Protoss_Gateway);
        int nexus = count(AUnitType.Protoss_Nexus);
        int citadel = count(AUnitType.Protoss_Citadel_of_Adun);

        // === Dark Templar ========================================
        
        if (seconds < 400 && (citadel >= 1 || has(AUnitType.Protoss_Templar_Archives))) {
            return ProtossStrategies.PROTOSS_2_Gate_DT;
        }

        // === Three Gateway =======================================
        
        if (gateways >= 3 && seconds < 400) {
            return ProtossStrategies.PROTOSS_3_Gate;
        }

        // === Two Gateway =========================================
        
        if (gateways == 2 && seconds < 290) {
            return ProtossStrategies.PROTOSS_2_Gate_Zealot_vP;
        }

        // === 12 Nexus ============================================
        
        if (nexus == 2 && seconds < 290) {
            return ProtossStrategies.PROTOSS_12_Nexus;
        }
        
        // === Carrier Push ========================================
        
        int cannons = count(AUnitType.Protoss_Photon_Cannon);
        if (cannons >= 1 && nexus >= 2) {
            return PROTOSS_Carrier_Push;
        }
        
        // =========================================================
        
        return null;
    }

}
