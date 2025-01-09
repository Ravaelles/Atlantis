package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ProduceControlTower {
    /**
     * If there are buildings without addons, build them.
     */
    public static boolean controlTowers() {
        if (!Have.starport() || Have.controlTower()) return false;

        if (A.canAfford(Terran_Control_Tower)) {
//            AddToQueue.maxAtATime(Terran_Control_Tower, 1, ProductionOrderPriority.HIGH);
            return ProduceAddon.buildNow(Terran_Control_Tower);
        }

        return false;
    }
}