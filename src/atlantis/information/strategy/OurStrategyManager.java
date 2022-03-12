package atlantis.information.strategy;

import atlantis.util.Enemy;
import atlantis.util.We;

public class OurStrategyManager {

    /**
     * Choose initial strategy and therefore the Build Order.
     */
    public static void initialize() {
        AStrategy strategy;

        if (We.protoss()) {
            strategy = initForProtoss();
        } else if (We.terran()) {
            strategy = initForTerran();
        } else {
            strategy = initForZerg();
        }

        OurStrategy.setTo(strategy);
    }

    // =========================================================

    private static AStrategy initForProtoss() {
        if (Enemy.zerg()) {
            return ProtossStrategies.PROTOSS_2_Gate_Zealot_vZ;
//            return ProtossStrategies.PROTOSS_Speedzealot;
//            return ProtossStrategies.PROTOSS_One_Base_Speedzealot;
        }

//        return ProtossStrategies.PROTOSS_One_Base_Speedzealot;
//        return ProtossStrategies.PROTOSS_2_Gate_Zealot_vP;
//        return ProtossStrategies.PROTOSS_Zealot_Core_Zealot;
        return ProtossStrategies.PROTOSS_ZZZ_Core;
//        return ProtossStrategies.PROTOSS_Fast_DT;
    }

    private static AStrategy initForTerran() {
//        return TerranStrategies.TERRAN_3_Rax_MnM;
//        return TerranStrategies.TERRAN_Nada_2_Fac;
//        return TerranStrategies.TERRAN_1_Base_Vultures;

        if (Enemy.protoss()) {
            return TerranStrategies.TERRAN_3_Rax_Academy_vP;
//            return TerranStrategies.TERRAN_2_Rax_Academy_vP;
//            return TerranStrategies.TERRAN_Nada_2_Fac;
        }
        else if (Enemy.terran()) {
            return TerranStrategies.TERRAN_2_Rax_Academy_vT;
        }
        else {
//            return TerranStrategies.TERRAN_2_Rax_Academy_vP;
            return TerranStrategies.TERRAN_2_Rax_Academy_vZ;
        }
    }

    private static AStrategy initForZerg() {
        return ZergStrategies.ZERG_2_Hatch_Hydra_vP;
//        return ZergStrategies.ZERG_13_Pool_Muta;
//        return ZergStrategies.ZERG_12_Hatch_vZ;
//        return ZergStrategies.ZERG_9_Pool_vZ;
    }

}
