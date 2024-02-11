package atlantis.combat.missions.defend.sparta;

import atlantis.architecture.Manager;
import atlantis.game.A;
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
            && unit.enemiesNear().inRadius(6, unit).empty();
    }

    @Override
    public Manager handle() {
        if (separateFromZealots()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean separateFromZealots() {
        AUnit zealot = unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(0.8, unit).first();
        if (zealot == null) return false;

        if (unit.distToNearestChokeCenter() <= 2.7) {
            unit.moveToMain(Actions.MOVE_SPACE, "SpaceForZeal!");
            return true;
        }

        if (unit.distTo(zealot) < 1) {
//            System.out.println("@ " + A.now() + " - SEPARATE " + unit.id() + " / " + unit.distTo(zealot));
//            unit.moveToMain(Actions.MOVE_SPACE, "SpaceForZealots");
            unit.moveAwayFrom(zealot, 0.5, Actions.MOVE_SPACE, "SpaceForZealots");
            return true;
        }

        return false;
    }
}
