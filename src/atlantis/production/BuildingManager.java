package atlantis.production;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.TerranCommandCenter;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.terran.LiftedBuildingManager;
import atlantis.terran.ShouldLiftBuildingManager;
import atlantis.units.AUnit;

public class BuildingManager extends Manager {
    public BuildingManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isABuilding();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranComsatStation.class,
            TerranCommandCenter.class,
            LiftedBuildingManager.class,
            ShouldLiftBuildingManager.class,
            ProtossShieldBattery.class,
        };
    }
}

