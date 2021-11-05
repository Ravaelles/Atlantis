package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.combat.micro.terran.TerranInfantry;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;

public class MissionDefend extends Mission {

    protected MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        // =========================================================

        APosition focusPoint = focusPoint();
        if (focusPoint == null) {
            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        return moveToDefendFocusPoint(unit, focusPoint);
    }

    // =========================================================

    private boolean moveToDefendFocusPoint(AUnit unit, APosition focusPoint) {

        // === Load infantry into bunkers ==========================

        if (AGame.isPlayingAsTerran() && TerranInfantry.tryLoadingInfantryIntoBunkerIfPossible(unit)) {
            return true;
        }

        // =========================================================

        // Let workers pass
        int workerBonus = Select.enemy().inRadius(5, unit).isEmpty()
                && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                ? 3 : 0;
        int alliesNear = Select.our().inRadius(2, unit).count();
        double expectedDist = 0.1
                + workerBonus
                + (unit.isRanged() ? 3 : 0)
                + (alliesNear / 20.0);

        if (unit.distTo(focusPoint) > expectedDist) {
            return unit.move(focusPoint, UnitActions.MOVE_TO_FOCUS, "MoveToDefend");
        }
        else if (unit.distTo(focusPoint) <= expectedDist - 0.5) {
            return unit.moveAwayFrom(focusPoint, 0.2, "TooClose");
        }
        else {
            if (unit.isMoving()) {
                unit.holdPosition("DefendHere");
                return true;
            }
//            return true;
            return false;
        }
    }

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (unit.isRanged() || enemy.isRanged()) {
            return true;
        }

        if (unit.hasWeaponRange(enemy, 1.2)) {
            return true;
        }

        if (unit.distToLessThan(focusPoint(), 1.5)) {
            return true;
        }

        if (Select.enemy().inRadius(14, Select.mainBase()).atLeast(2)) {
            return true;
        }

//        if (unit.isInWeaponRangeByGame(enemy)) {

//        if () {
//            return true;
//        }

        return false;
    }

}
