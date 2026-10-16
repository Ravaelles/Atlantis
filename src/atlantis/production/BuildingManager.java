package atlantis.production;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            // BATTLE CUTOVER: Terran building micro (deleted) - Protoss unaffected.
            LiftedBuildingManager::new,
            ShouldLiftBuildingManager::new,
//            ShieldBattery.class,
        };
    }
}

