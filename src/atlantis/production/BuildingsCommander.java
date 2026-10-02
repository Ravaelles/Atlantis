package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.terran.FlyingBuildingScoutCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public class BuildingsCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            FlyingBuildingScoutCommander::new,
        };
    }

    @Override
    protected boolean handle() {
        for (AUnit unit : Select.ourBuildings().list()) {
            if (We.protoss() && !unit.isPowered()) continue;

            (new BuildingManager(unit)).invokeFrom(this);
        }
        return false;
    }

}
