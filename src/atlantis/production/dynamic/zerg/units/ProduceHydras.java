package atlantis.production.dynamic.zerg.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProduceHydras {
    public static boolean hydras() {
        if (!Have.a(AUnitType.Zerg_Hydralisk_Den)) return false;

        int hydras = Count.hydralisks();

        if (hydras <= 2 || A.hasGas(350)) {
            return produceHydra();
        }

        if (A.supplyUsed() <= 190 && canAfford()) return produceHydra();

        return false;
    }

    private static boolean canAfford() {
        return A.canAfford(450, 200) || AGame.canAffordWithReserved(50, 150);
    }

    private static boolean produceHydra() {
        return AddToQueue.maxAtATime(AUnitType.Zerg_Hydralisk, 8);
    }
}
