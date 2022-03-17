package atlantis.production.requests.zerg;

import atlantis.information.enemy.EnemyUnits;
import atlantis.production.requests.AntiAirBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Selection;

public class ZergSporeColony extends AntiAirBuildingManager {

    @Override
    public AUnitType type() {
        return AUnitType.Zerg_Spore_Colony;
    }

    @Override
    public boolean shouldBuildNew() {
        if (!Have.a(type())) {
            return false;
        }

        Selection air = EnemyUnits.discovered().air();

        int existing = Count.ofType(type());
        if (existing == 0) {
            int wraiths = air.ofType(AUnitType.Terran_Wraith).count();
            int battlecruisers = air.ofType(AUnitType.Terran_Battlecruiser).count();
            int scouts = air.ofType(AUnitType.Protoss_Scout).count();
            return wraiths > 0 || battlecruisers > 0 || scouts > 0;
        }

        int mutas = air.ofType(AUnitType.Zerg_Mutalisk).count();
        if (existing * 4 < mutas) {
            return true;
        }

        return false;
    }

}
