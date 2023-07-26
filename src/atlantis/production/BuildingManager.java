package atlantis.production;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.TerranCommandCenter;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.terran.TerranLiftedBuildingManager;
import atlantis.terran.TerranShouldLiftBuildingManager;
import atlantis.units.AUnit;

public class BuildingManager extends Manager {
    public BuildingManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            TerranComsatStation.class,
            TerranCommandCenter.class,
            TerranLiftedBuildingManager.class,
            TerranShouldLiftBuildingManager.class,
            ProtossShieldBattery.class,
        };
    }
}

