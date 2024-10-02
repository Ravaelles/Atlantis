package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class SpreadOutDetectors extends Manager {

    private Selection separateFrom;
    private int minDistBetween;

    public SpreadOutDetectors(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        minDistBetween = minDistanceBetweenUnits();
        separateFrom = Select.ourOfType(unit.type()).inRadius(minDistBetween, unit).exclude(unit);

        return separateFrom.count() > 0;
    }

    public Manager handle() {
        AUnit otherUnit = separateFrom.nearestTo(unit);

        if (unit.id() < otherUnit.id()) return null; // Older units don't separate, only newer ones do it

        unit.moveAwayFrom(otherUnit.position(), minDistBetween, Actions.MOVE_FORMATION, "SpreadDetectors");
        return usedManager(this);
    }

    private int minDistanceBetweenUnits() {
        return We.protoss() ? 11 : 5;
    }
}
