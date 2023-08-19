package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnitTooCloseToBunker extends Manager {
    private AUnit bunker;

    public UnitTooCloseToBunker(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMedic() && (bunker = unit.friendsNear().bunkers().nearestTo(unit)) != null;
    }

    @Override
    public Manager handle() {
        if (unit.distTo(bunker) < 2.5) {
            unit.moveAwayFrom(bunker, 2, Actions.MOVE_SPACE, "TooCloseToBunker");
            return usedManager(this);
        }

        return null;
    }
}
