package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.protoss.ProtossReinforceBases;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProduceCannon {
    public static void produce() {
//        if (shouldProduce())
    }

    private static boolean shouldProduce() {
        if (A.everyFrameExceptNthFrame(47)) return false;

        if (Count.inProductionOrInQueue(Protoss_Photon_Cannon) >= 2) return false;

        if (ProtossReinforceBases.invoke()) return true;

        return false;
    }
}
