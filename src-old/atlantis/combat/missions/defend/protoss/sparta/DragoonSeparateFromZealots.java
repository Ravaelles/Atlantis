package atlantis.combat.missions.defend.protoss.sparta;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

public class DragoonSeparateFromZealots extends Manager {

    private AUnit zealot;

    public DragoonSeparateFromZealots(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon()
            && unit.noCooldown()
            && unit.enemiesNear().inRadius(7, unit).empty()
            && needToSeparateFromZealots();
    }

    @Override
    public Manager handle() {
        if (!unit.moveAwayFrom(zealot, 0.2, Actions.MOVE_SPACE, "SpaceForZealotA")) {
            if (unit.moveAwayFrom(zealot, 2, Actions.MOVE_SPACE, "SpaceForZealotB")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean needToSeparateFromZealots() {
        zealot = unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(0.7, unit).first();
        if (zealot == null) return false;

        return unit.nearestChokeCenterDist() <= 2.7;
    }
}
