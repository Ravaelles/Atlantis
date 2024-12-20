package atlantis.production.dynamic.protoss.buildings;

import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProduceCannon {
    public static boolean produce() {
        return false;
//        if (shouldProduce())
    }

    private static boolean shouldProduce() {
//        if (A.everyFrameExceptNthFrame(29)) return false;

        if (Count.inProductionOrInQueue(Protoss_Photon_Cannon) >= 4) return false;

//        if (ProtossSecureBasesCommander.invoke()) return true;

        return false;
    }
}
