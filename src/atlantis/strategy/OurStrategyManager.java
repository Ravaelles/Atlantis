package atlantis.strategy;

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
//            return ProtossStrategies.PROTOSS_2_Gate_Zealot;
            return ProtossStrategies.PROTOSS_Speedzealot;
        }

        return ProtossStrategies.PROTOSS_2_Gate_Range_Expand;
    }

    private static AStrategy initForTerran() {
//        return TerranStrategies.TERRAN_3_Rax_MnM;
//        return TerranStrategies.TERRAN_Nada_2_Fac;
        return TerranStrategies.TERRAN_2_Rax_Academy_vZ;
//        return TerranStrategies.TERRAN_1_Base_Vultures;
    }

    private static AStrategy initForZerg() {
//        return ZergStrategies.ZERG_13_Pool_Muta;
//        return ZergStrategies.ZERG_12_Hatch_vZ;
        return ZergStrategies.ZERG_9_Pool_vZ;
    }

}
