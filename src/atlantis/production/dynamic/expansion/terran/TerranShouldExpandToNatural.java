package atlantis.production.dynamic.expansion.terran;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class TerranShouldExpandToNatural {
    public static boolean shouldExpandToNatural() {
        if (A.hasMinerals(400) && Count.basesWithUnfinished() <= 1 && CountInQueue.bases() == 0) return true;

        if (Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) > 1) return false;

        if (
//            ArmyStrength.weAreStronger()
//                && (
            A.canAffordWithReserved(360, 0)
                || Count.withPlanned(AUnitType.Terran_Bunker) == 0
//            )
        ) return true;

        return false;
    }
}
