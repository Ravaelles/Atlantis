package atlantis.combat.micro.stack;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class StackedUnitsManager extends Manager {

    private double minDist;
    private boolean onlyOfTheSameType;

    public StackedUnitsManager(AUnit unit, double minDist, boolean onlyOfTheSameType) {
        super(unit);
        this.minDist = minDist;
        this.onlyOfTheSameType = onlyOfTheSameType;
    }

    @Override
    public Manager handle() {
        AUnit nearest = (onlyOfTheSameType ? Select.ourOfType(unit.type()) : Select.ourRealUnits())
                .exclude(unit).inRadius(minDist, unit).nearestTo(unit);

        if (nearest != null) {
            if (unit.moveAwayFrom(nearest, minDist / 2, "Stacked", Actions.MOVE_FORMATION)) {
                return usedManager(this);
            }
        }

        return null;
    }

}
