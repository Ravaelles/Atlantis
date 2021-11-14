package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.combat.micro.terran.TerranInfantry;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
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

        // Let workers pass
        double optimalDist = optimalDist(unit, focusPoint);

        if (unit.distTo(focusPoint) > optimalDist) {
            return unit.move(focusPoint, UnitActions.MOVE_TO_FOCUS, "MoveToDefend");
        }
        else if (unit.distTo(focusPoint) <= optimalDist - 0.5) {
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

    private double optimalDist(AUnit unit, APosition focusPoint) {
        int workerBonus = Select.enemy().inRadius(5, unit).isEmpty()
                && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                ? 3 : 0;
        int alliesNear = Select.our().inRadius(2, unit).count();
        return 0.1
                + (unit.isTank() ? 3 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isMarine() ? 2 : 0)
                + workerBonus
                + (unit.isRanged() ? 3 : 0)
                + (alliesNear / 20.0);
    }

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
//        if (unit.isRanged() || enemy.isRanged()) {
        if (unit.isRanged()) {
            return true;
        }

        if (unit.hasWeaponRange(enemy, 1)) {
            return true;
        }

        if (unit.distToLessThan(focusPoint(), 0.5)) {
            return true;
        }

//        if (Select.enemy().inRadius(18, Select.main()).atLeast(1)) {
        if (Select.enemy().inRadius(18, Select.main()).atLeast(1)) {
            return true;
        }

        if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
            return true;
        }


        return false;
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        return unit.hp() >= 18;
    }
}
