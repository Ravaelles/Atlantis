package atlantis.combat.missions.defend.sparta;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

public class DragoonSeparateFromZealots extends Manager {
    public DragoonSeparateFromZealots(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon()
            && unit.noCooldown()
            && unit.enemiesNear().inRadius(8, unit).empty();
    }

    @Override
    public Manager handle() {
        if (separateFromZealots()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean separateFromZealots() {
        APosition center = unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(0.8, unit).center();
        if (center == null) return false;

        if (unit.distTo(center) < 0.7) {
            unit.moveAwayFrom(center, 0.25, Actions.MOVE_SPACE, "SpaceForZealots");
            return true;
        }

        return false;
    }
}
