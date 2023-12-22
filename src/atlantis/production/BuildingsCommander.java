package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.terran.FlyingBuildingScoutCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class BuildingsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            FlyingBuildingScoutCommander.class,
        };
    }

    @Override
    protected void handle() {
        for (AUnit unit : Select.ourBuildings().list()) {
            (new BuildingManager(unit)).invoke(this);
        }
    }

}
