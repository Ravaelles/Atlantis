package atlantis.information.strategy.protoss;

import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.information.strategy.AStrategy;
import atlantis.units.AUnitType;


public class ProtossStrategies extends AStrategy {
    // Standard
    public static final AStrategy ZealotCoreDragoon = protossStrategy();

    // =========================================================
    public static final AStrategy Zealot_Core_Zealot = protossStrategy();
    public static final AStrategy Zealot_Core_Zealot_vZ = protossStrategy();
    public static final AStrategy _4Zealot_Core_vZ = protossStrategy();
    public static final AStrategy ZealotIntoGoon = protossStrategy();
    public static final AStrategy ZZCore = protossStrategy();
    public static final AStrategy ZZZCore = protossStrategy();
    public static final AStrategy ZZZZCore = protossStrategy();
    // Rush
    public static final AStrategy _2GateZealot_vP = protossStrategy();
    public static final AStrategy _3GateZealot_vP = protossStrategy();
    public static final AStrategy _5GateZealot_vP = protossStrategy();
    public static final AStrategy _2GateZealot_vZ = protossStrategy();
    public static final AStrategy Speedzealot = protossStrategy();
    public static final AStrategy OneBaseSpeedzealot = protossStrategy();
    // Cheese
    public static final AStrategy _3Gate = protossStrategy();
    // Expansion
    public static final AStrategy _12Nexus = protossStrategy();
    public static final AStrategy ForgeFE_vZ = protossStrategy();
    public static final AStrategy ForgeFE_vP = protossStrategy();
    // Tech
    public static final AStrategy CoreDragoon = protossStrategy();
    public static final AStrategy DragoonFirst = protossStrategy();
    public static final AStrategy _2GateRangeExpand = protossStrategy();
    public static final AStrategy _2GateDT = protossStrategy();
    public static final AStrategy CarrierPush = protossStrategy();
    public static final AStrategy FastDT = protossStrategy();

    public static AStrategy protossChooseStrategy() {
//        if (true) return ProtossStrategies.PROTOSS_2_Gate_DT;

        if (Enemy.protoss()) {
//            return ProtossStrategies.PROTOSS_Zealot_into_Goon;
//            return ProtossStrategies.PROTOSS_Core_Dragoon;
//            return ProtossStrategies.PROTOSS_Core_Dragoon;
//            return ProtossStrategies.PROTOSS_Zealot_Core_Zealot;
//            return ProtossStrategies.PROTOSS_2_Gate_DT;
            return ProtossStrategies.ZealotCoreDragoon; // <--
//            return ProtossStrategies.PROTOSS_Forge_FE_vP;
//            return ProtossStrategies.PROTOSS_Forge_FE_vZ;
//            return ProtossStrategies.PROTOSS_3_Gate_Zealot_vP;
//            return ProtossStrategies.PROTOSS_5_Gate_Zealot_vP;
//            return ProtossStrategies.PROTOSS_Zealot_Core_Zealot;
//            return ProtossStrategies.PROTOSS_3_Gate_Zealot_vP;
//            return ProtossStrategies.PROTOSS_ZZZ_Core;
        }

        else if (Enemy.terran()) {
            return ProtossStrategies.ZealotCoreDragoon;
//            return ProtossStrategies.PROTOSS_Forge_FE_vP;
//            return ProtossStrategies.PROTOSS_Zealot_into_Goon;
//            return ProtossStrategies.PROTOSS_Core_Dragoon;
        }

        else if (Enemy.zerg()) {
            return ProtossStrategies.ZealotIntoGoon;
//            return ProtossStrategies.PROTOSS_Forge_FE_vZ;
//            return ProtossStrategies.PROTOSS_4Zealot_Core_vZ;
//            return ProtossStrategies.PROTOSS_Zealot_Core_Zealot_vZ;
//            return ProtossStrategies.PROTOSS_3_Gate_Zealot_vP;
//            return ProtossStrategies.PROTOSS_Core_Dragoon;
//            return ProtossStrategies.PROTOSS_Speedzealot;
//            return ProtossStrategies.PROTOSS_Zealot_Core_Dragoon;
//            return ProtossStrategies.PROTOSS_ZZZZ_Core;
        }

        return ProtossStrategies.ZealotIntoGoon;
//        return ProtossStrategies.PROTOSS_Forge_FE_vZ;
//        return ProtossStrategies.PROTOSS_Zealot_Core_Dragoon;
//        return ProtossStrategies.PROTOSS_Zealot_Core_Zealot;
//        return ProtossStrategies.PROTOSS_One_Base_Speedzealot;
//        return ProtossStrategies.PROTOSS_2_Gate_Zealot_vP;
//        return PROTOSS_Dragoon_First;
//        return PROTOSS_ZZ_Core;
//        return PROTOSS_ZZZ_Core;
//        return ProtossStrategies.PROTOSS_Fast_DT;
    }

    // =========================================================

    /**
     * Every Protoss strategy instance, in declaration order.
     */
    public static java.util.List<AStrategy> allProtossStrategies() {
        java.util.List<AStrategy> protoss = new java.util.ArrayList<>();
        for (AStrategy strategy : allStrategies) {
            if (strategy.isProtoss()) protoss.add(strategy);
        }
        return protoss;
    }

    public static void initialize() {

        // === Balanced ======================================

        ZealotCoreDragoon.setName("Zealot Core Dragoon").setGoingTech();
        Zealot_Core_Zealot.setName("Zealot Core Zealot");
        Zealot_Core_Zealot_vZ.setName("Zealot Core Zealot vZ");
        _4Zealot_Core_vZ.setName("4Zealot Core vZ");
        ZealotIntoGoon.setName("Zealot into Goon");
        ZZCore.setName("ZZ Core"); // Zealot-Zealot-Core
        ZZZCore.setName("ZZZ Core"); // Zealot-Zealot-Zealot-Core
        ZZZZCore.setName("ZZZZ Core"); // ZZZZ-Core

        // === Rushes ========================================

        _2GateZealot_vP.setName("2 Gate Zealot vP").setGoingRush();
        _3GateZealot_vP.setName("3 Gate Zealot vP").setGoingRush();
        _5GateZealot_vP.setName("5 Gate Zealot vP").setGoingRush();

        _2GateZealot_vZ.setName("2 Gate Zealot vZ").setGoingRush();

        Speedzealot.setName("+1 Speedzealot").setGoingRush();

        OneBaseSpeedzealot.setName("One Base Speedzealot").setGoingRush();

        // === Cheese ========================================

        _3Gate.setName("3 Gate").setGoingRush().setGoingCheese().setUnplayable();

        // === Expansion =====================================

        _12Nexus.setName("12 Nexus").setGoingExpansion().setUnplayable();
        ForgeFE_vZ.setName("Forge FE vZ").setGoingExpansion();
        ForgeFE_vP.setName("Forge FE vP").setGoingExpansion();

        // === Tech ==========================================

        CoreDragoon.setName("Core Dragoon").setGoingTech();
        ZealotIntoGoon.setName("Zealot into Goon").setGoingTech();
        DragoonFirst.setName("Dragoon First").setGoingTech();

        _2GateRangeExpand.setName("2 Gate Range Expand").setGoingTech();

        _2GateDT.setName("2 Gate DT").setGoingTech().setGoingHiddenUnits();

        CarrierPush.setName("Carrier Push").setGoingTech().setGoingAirUnitsLate().setUnplayable();

        FastDT.setName("Fast DT").setGoingTech().setGoingRush();
    }

    // =========================================================

    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int gateways = count(AUnitType.Protoss_Gateway);
        int nexus = count(AUnitType.Protoss_Nexus);
        int citadel = count(AUnitType.Protoss_Citadel_of_Adun);

        // === Dark Templar ========================================

        if (seconds < 400 && (citadel >= 1 || has(AUnitType.Protoss_Templar_Archives))) {
            return ProtossStrategies._2GateDT;
        }

        // === Three Gateway =======================================

        if (gateways >= 3 && seconds < 400) {
            return ProtossStrategies._3Gate;
        }

        // === Two Gateway =========================================

        if (gateways == 2 && seconds < 290) {
            return ProtossStrategies._2GateZealot_vP;
        }

        // === 12 Nexus ============================================

        if (nexus == 2 && seconds < 290) {
            return ProtossStrategies._12Nexus;
        }

        // === Carrier Push ========================================

        int cannons = count(AUnitType.Protoss_Photon_Cannon);
        if (cannons >= 1 && nexus >= 2) {
            return CarrierPush;
        }

        // =========================================================

        return null;
    }
}
