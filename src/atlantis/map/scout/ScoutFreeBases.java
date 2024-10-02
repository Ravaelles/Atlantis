package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

import static atlantis.map.scout.ScoutState.nextPositionToScout;

public class ScoutFreeBases extends Manager {

    public ScoutFreeBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isScout()) return false;

//        if (
//            unit.enemiesNear()
//                .combatUnits()
//                .canAttack(unit, 1.5 + unit.woundPercent() / 40.0).notEmpty()
//        ) return false;

//        if (A.s >= 300) return true;

        return (unit.hasNotMovedInAWhile() || (unit.hasNotMovedInAWhile() && unit.looksIdle()))
            && (nextPositionToScout = defineValidPosition()) != null;
    }

    @Override
    protected Manager handle() {
        if (unit.move(
            nextPositionToScout, Actions.MOVE_SCOUT, "ScoutBases" + A.now(), true
        )) return usedManager(this);

        return null;
    }

    private HasPosition defineValidPosition() {
        if (
            nextPositionToScout == null
                || nextPositionToScout.isPositionVisible()
                || isNextPositionTooClose()
        ) {
            nextPositionToScout = BaseLocations.nearestUnexploredBaseLocation(unit);

            if (nextPositionToScout == null) {
                nextPositionToScout = BaseLocations.randomInvisibleStartingLocation();
            }
        }

        return isNextPositionTooClose() ? null : nextPositionToScout;
    }

    private boolean isNextPositionTooClose() {
        return nextPositionToScout == null || nextPositionToScout.distTo(unit) < 6;
    }
}
