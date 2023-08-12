package atlantis.production.dynamic.zerg.units;

import atlantis.game.AGame;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProduceHydras {
    public static boolean hydras() {
        if (!Have.a(AUnitType.Zerg_Hydralisk_Den)) return false;

        int hydras = Count.hydralisks();

        if (Count.larvas() == 0) return false;

        if (hydras <= 2 || AGame.canAffordWithReserved(50, 0)) {
            AddToQueue.withStandardPriority(AUnitType.Zerg_Hydralisk);
            return true;
        }

        return false;
    }
}
