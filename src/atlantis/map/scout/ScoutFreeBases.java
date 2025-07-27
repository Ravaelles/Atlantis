package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ScoutFreeBases extends Manager {
    private HasPosition nextPosition;

    public ScoutFreeBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isScout() && !unit.isAir()) return false;

//        if (
//            unit.enemiesNear()
//                .combatUnits()
//                .canAttack(unit, 1.5 + unit.woundPercent() / 40.0).notEmpty()
//        ) return false;

//        if (A.s >= 300) return true;

        return (unit.hasNotMovedInAWhile() || (unit.hasNotMovedInAWhile() && unit.looksIdle()))
            && (nextPosition = defineValidPosition()) != null;
    }

    @Override
    protected Manager handle() {
        if (unit.move(
            nextPosition, Actions.MOVE_SCOUT, "ScoutBases" + A.now(), true
        )) return usedManager(this);

        return null;
    }

    private HasPosition defineValidPosition() {
        if (
            nextPosition == null
                || nextPosition.isPositionVisible()
                || isNextPositionTooClose()
        ) {
            nextPosition = BaseLocations.nearestUnexploredBaseLocation(unit);

            if (nextPosition == null) {
                nextPosition = BaseLocations.randomInvisibleStartingLocation();
            }

            if (nextPosition == null) {
                nextPosition = BaseLocations.randomFree();
            }
        }

        return isNextPositionTooClose() ? null : nextPosition;
    }

    private boolean isNextPositionTooClose() {
        return nextPosition == null || nextPosition.distTo(unit) < 6;
    }
}
