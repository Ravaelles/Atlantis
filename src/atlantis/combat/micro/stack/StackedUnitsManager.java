package atlantis.combat.micro.stack;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class StackedUnitsManager extends Manager {
    private double minDist;
    private boolean onlyOfTheSameType;

    public StackedUnitsManager(AUnit unit) {
        super(unit);

        boolean overlord = unit.isOverlord();
        this.minDist = overlord ? 1.5 : 0.5;
        this.onlyOfTheSameType = overlord;
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit();
    }

    @Override
    protected Manager handle() {
        AUnit nearest = (onlyOfTheSameType ? Select.ourOfType(unit.type()) : Select.ourRealUnits())
            .exclude(unit).inRadius(minDist, unit).nearestTo(unit);

        if (nearest != null) {
            if (unit.moveAwayFrom(nearest, minDist / 2, Actions.MOVE_FORMATION, "Stacked")) {
                return usedManager(this);
            }
        }

        return null;
    }

}
