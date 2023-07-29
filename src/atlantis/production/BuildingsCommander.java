package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.terran.TerranFlyingBuildingScoutCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class BuildingsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            TerranFlyingBuildingScoutCommander.class,
        };
    }

    @Override
    public void handle() {
        super.handle();

        for (AUnit unit : Select.ourBuildings().list()) {
            (new BuildingManager(unit)).handle();
        }
    }

}
