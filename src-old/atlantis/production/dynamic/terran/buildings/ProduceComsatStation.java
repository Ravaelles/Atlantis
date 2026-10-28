package atlantis.production.dynamic.terran.buildings;

import atlantis.information.enemy.EnemyInfo;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Comsat_Station;

public class ProduceComsatStation {
    public static boolean comsats() {
        if (!Have.academy()) {
            return false;
        }

        if (EnemyInfo.hasHiddenUnits()) {
            return produce();
        }

        if (
            Count.bases() > Count.withPlanned(Terran_Comsat_Station)
                && Count.inQueueOrUnfinished(Terran_Comsat_Station, 20) <= 0
        ) {
            return produce();
        }

        return false;
    }

    private static boolean produce() {
        return ProduceAddon.buildNow(Terran_Comsat_Station);
    }
}
