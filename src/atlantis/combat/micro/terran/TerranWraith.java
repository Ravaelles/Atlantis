package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class TerranWraith extends Manager {

    public TerranWraith(AUnit unit) {
        super(unit);
    }

    public boolean update() {
        if (UnitBeingReparedManager.handleDontRunWhenBeingRepared()) return true;

        return false;
    }

}
