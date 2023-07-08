package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.information.tech.ATech;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranWraith extends Manager {

    public TerranWraith(AUnit unit) {
        super(unit);
    }

    public boolean update() {
        if (UnitBeingReparedManager.handleDontRunWhenBeingRepared()) return true;

        return false;
    }

}
