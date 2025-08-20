package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public abstract class SpreadOutAirUnits extends Manager {
    protected Selection separateFrom;

    public SpreadOutAirUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return otherUnitsOfSameType().count() > 0;
    }

    protected abstract double minDistBetweenUnits();
    protected abstract Selection unitsSelector();

    public Manager handle() {
        if (separateFrom == null) return null;
        
        AUnit otherUnit = separateFrom.nearestTo(unit);
        if (otherUnit == null) return null;
        if (unit.id() < otherUnit.id()) return null; // Older units don't separate, only newer ones do it

        if (separate(otherUnit)) return usedManager(this);

        return null;
    }

    private boolean separate(AUnit otherUnit) {
        return unit.moveAwayFrom(
            otherUnit.position(), minDistBetweenUnits(), Actions.MOVE_FORMATION, "SpreadAir"
        );
    }

    protected Selection otherUnitsOfSameType() {
        return unitsSelector().inRadius(minDistBetweenUnits(), unit);
    }
}
