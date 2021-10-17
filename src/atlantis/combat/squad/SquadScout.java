package atlantis.combat.squad;

import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class SquadScout {

    public static boolean handle(AUnit unit) {
        if (unit.equals(unit.squad().getSquadScout())) {
            return handleSquadScout(unit);
        }

        return false;
    }

    // =========================================================

    private static boolean handleSquadScout(AUnit unit) {
        if (!unit.canAnyCloseEnemyAttackThisUnit(2)) {
            APosition enemyBase = AEnemyUnits.getEnemyBase();
            if (enemyBase == null) {
                return false;
            }

            if (enemyBase.distanceTo(unit) > 4) {
                unit.move(enemyBase.getPosition(), UnitActions.SCOUT, "Scout!");
                return true;
            }
            else if (unit.isMoving()) {
                unit.holdPosition("Scouted!");
                return true;
            }
        }

        return false;
    }

}
