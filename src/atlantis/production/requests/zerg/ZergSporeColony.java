package atlantis.production.requests.zerg;

import atlantis.information.enemy.EnemyUnits;
import atlantis.production.requests.AntiAirBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Selection;

public class ZergSporeColony extends AntiAirBuildingCommander {

    @Override
    public AUnitType type() {
        return AUnitType.Zerg_Spore_Colony;
    }

    @Override
    public AUnitType typeToBuildFirst() {
        return AUnitType.Zerg_Creep_Colony;
    }

    @Override
    public int expected() {
        if (!Have.a(AUnitType.Zerg_Spawning_Pool)) {
            return 0;
        }

        Selection air = EnemyUnits.discovered().air();

        int mutas = air.ofType(AUnitType.Zerg_Mutalisk).count();
        if (mutas > 0) {
            return (mutas / 4) + 1;
        }

        int wraiths = air.ofType(AUnitType.Terran_Wraith).count();
        int battlecruisers = air.ofType(AUnitType.Terran_Battlecruiser).count();
        int scouts = air.ofType(AUnitType.Protoss_Scout).count();
        return (int) Math.ceil((wraiths + battlecruisers + scouts) / 4);
    }

    @Override
    public int existingWithUnfinished() {
        return Count.existingOrInProductionOrInQueue(type())
            + Count.existingOrInProductionOrInQueue(AUnitType.Zerg_Creep_Colony);
    }
}
