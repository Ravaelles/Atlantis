package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ChokeBlockerMoveAway extends Manager {
    private final APosition chokeBlockPoint;

    public ChokeBlockerMoveAway(AUnit unit) {
        super(unit);
        this.chokeBlockPoint = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        if (A.seconds() <= 300) return false;

        if (chokeBlockPoint.distTo(unit) > 8) return false;

        return unit.enemiesNear().empty()
            && unit.friendsNear().workers().notProtector().inRadius(6, unit).size() >= 2;
    }

    @Override
    protected Manager handle() {
        unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_SPACE, "Spacing...");
        unit.setAction(Actions.MOVE_SPECIAL);

        return usedManager(this);
    }
}
