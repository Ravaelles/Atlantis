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
//        if (Select.enemy().inRadius(12.5, this).canShootAt(this, safetyMargin).isEmpty()) {
        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase == null) {
            return false;
        }

        if (enemyBase.distTo(unit) > 4) {
            unit.move(enemyBase.getPosition(), UnitActions.SCOUT, "Pioneer");
            return true;
        }
        else if (unit.isMoving()) {
            unit.holdPosition("Scouted!");
            return true;
        }
//        }

        return false;
    }

}
