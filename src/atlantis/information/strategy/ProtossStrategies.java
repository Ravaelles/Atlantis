package atlantis.information.strategy;

import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.util.Enemy;


public class ProtossStrategies extends AStrategy {

    // Standard
    public static final AStrategy PROTOSS_Zealot_Core_Dragoon = protossStrategy();
    public static final AStrategy PROTOSS_Zealot_Core_Zealot = protossStrategy();
    public static final AStrategy PROTOSS_ZZ_Core = protossStrategy();
    public static final AStrategy PROTOSS_ZZZ_Core = protossStrategy();

    // Rush
    public static final AStrategy PROTOSS_2_Gate_Zealot_vP = protossStrategy();
    public static final AStrategy PROTOSS_2_Gate_Zealot_vZ = protossStrategy();
    public static final AStrategy PROTOSS_Speedzealot = protossStrategy();
    public static final AStrategy PROTOSS_One_Base_Speedzealot = protossStrategy();

    // Cheese
    public static final AStrategy PROTOSS_3_Gate = protossStrategy();

    // Expansion
    public static final AStrategy PROTOSS_12_Nexus = protossStrategy();

    // Tech
    public static final AStrategy PROTOSS_Dragoon_First = protossStrategy();
    public static final AStrategy PROTOSS_2_Gate_Range_Expand = protossStrategy();
    public static final AStrategy PROTOSS_2_Gate_DT = protossStrategy();
    public static final AStrategy PROTOSS_Carrier_Push = protossStrategy();
    public static final AStrategy PROTOSS_Fast_DT = protossStrategy();

    // =========================================================

    public static AStrategy initForProtoss() {
        if (Enemy.zerg()) {
//            return PROTOSS_2_Gate_Zealot_vZ;
//            return ProtossStrategies.PROTOSS_Speedzealot;
//            return ProtossStrategies.PROTOSS_One_Base_Speedzealot;
        }

//        return ProtossStrategies.PROTOSS_Zealot_Core_Dragoon;
//        return ProtossStrategies.PROTOSS_Zealot_Core_Zealot;
//        return ProtossStrategies.PROTOSS_One_Base_Speedzealot;
//        return ProtossStrategies.PROTOSS_2_Gate_Zealot_vP;
//        return PROTOSS_Dragoon_First;
//        return PROTOSS_ZZ_Core;
//        return PROTOSS_ZZZ_Core;
        return ProtossStrategies.PROTOSS_Fast_DT;
    }

    // =========================================================

    public static void initialize() {

        // === Balanced ======================================

        PROTOSS_Zealot_Core_Dragoon.setName("Zealot Core Dragoon");
        PROTOSS_Zealot_Core_Zealot.setName("Zealot Core Zealot");
        PROTOSS_ZZ_Core.setName("ZZ Core"); // Zealot-Zealot-Core
        PROTOSS_ZZZ_Core.setName("ZZZ Core"); // Zealot-Zealot-Zealot-Core

        // === Rushes ========================================

        PROTOSS_2_Gate_Zealot_vP.setName("2 Gate Zealot vP").setGoingRush();
        PROTOSS_2_Gate_Zealot_vZ.setName("2 Gate Zealot vZ").setGoingRush();

        PROTOSS_Speedzealot.setName("+1 Speedzealot").setGoingRush();

        PROTOSS_One_Base_Speedzealot.setName("One Base Speedzealot").setGoingRush();

        // === Cheese ========================================

        PROTOSS_3_Gate.setName("3 Gate").setGoingRush().setGoingCheese();

        // === Expansion =====================================

        PROTOSS_12_Nexus.setName("12 Nexus").setGoingExpansion();

        // === Tech ==========================================

        PROTOSS_Dragoon_First.setName("Dragoon First").setGoingTech();

        PROTOSS_2_Gate_Range_Expand.setName("2 Gate Range Expand").setGoingTech();

        PROTOSS_2_Gate_DT.setName("2 Gate DT").setGoingTech().setGoingHiddenUnits();

        PROTOSS_Carrier_Push.setName("Carrier Push").setGoingTech().setGoingAirUnitsLate();

        PROTOSS_Fast_DT.setName("Fast DT").setGoingTech().setGoingRush();
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
