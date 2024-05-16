package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossZealotTooFarFromDragoon extends Manager {
    private AUnit dragoon;

    public ProtossZealotTooFarFromDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot()
            && (dragoon = nearestDragoon()) != null
            && dragoon.distTo(unit) > 2;
    }

    private AUnit nearestDragoon() {
        Selection dragoons = unit.friendsNear().dragoons();
        if (dragoons.empty()) dragoons = Select.ourOfType(AUnitType.Protoss_Dragoon);

        return dragoons.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.move(dragoon, Actions.MOVE_FORMATION, "ClosaToDragoon")) return usedManager(this);

        return null;
    }
}
