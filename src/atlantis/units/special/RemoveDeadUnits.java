package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;

public class RemoveDeadUnits extends Manager {
    public RemoveDeadUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        if (!unit.isAlive()) {
            Squad squad = unit.squad();
//            A.errPrintln("Removing invalid unit: "
//                + unit + " / hp:" + unit.hp()
//                + " / alive:" + unit.isAlive());
            if (squad != null) squad.removeUnit(unit);

            return usedManager(this);
        }

        return null;
    }
}
