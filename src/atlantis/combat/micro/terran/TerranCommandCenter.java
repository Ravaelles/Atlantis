package atlantis.combat.micro.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.lifted.RebaseToNewMineralPatches;
import atlantis.combat.micro.terran.lifted.LandBuildingThatLifted;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class TerranCommandCenter extends Manager {
    public TerranCommandCenter(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Terran_Command_Center);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            RebaseToNewMineralPatches.class,
            LandBuildingThatLifted.class,
        };
    }
}
