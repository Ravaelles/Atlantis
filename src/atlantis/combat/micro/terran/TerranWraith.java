package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.information.tech.ATech;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranWraith extends Microable {
    private AUnit unit;

    // =========================================================

    public TerranWraith(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public boolean update() {
        if (UnitBeingReparedManager.handleDontRunWhenBeingRepared(unit)) return true;

        return false;
    }

}
