package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class EnsureBallAsTank extends Manager {
    private AUnit squadLeader;

    public EnsureBallAsTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTank()
            && unit.noCooldown()
            && unit.squad().size() >= 4
            &&
            (
                unit.friendsNear().groundUnits().inRadius(4, unit).count() <= 2
                    && unit.friendsNear().groundUnits().inRadius(7, unit).count() <= 4
            )
            && ((squadLeader = unit.squadLeader()) != null && squadLeader.distTo(unit) > 5.5);
    }

    protected Manager handle() {
        if (unit.move(squadLeader, Actions.MOVE_FORMATION, "Ball")) {
            return usedManager(this);
        }

//        if (unit.goToNearest(AUnitType.Terran_Siege_Tank_Tank_Mode, AUnitType.Terran_Siege_Tank_Siege_Mode)) {
//            return usedManager(this);
//        }

        return null;
    }
}
