package atlantis.combat.squad;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.micro.avoid.Avoid;
import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import atlantis.util.We;

public class SquadScout {

    public static boolean handle(AUnit unit) {
//        if (We.terran()) {
//            return false;
//        }

        if (unit.equals(unit.squad().getSquadScout())) {
            return handleSquadScout(unit);
        }

        return false;
    }

    // =========================================================

    private static boolean handleSquadScout(AUnit unit) {
        if (AAvoidUnits.avoidEnemiesIfNeeded(unit)) {
            return true;
        }

//        if (Select.enemy().inRadius(12.5, this).canShootAt(this, safetyMargin).isEmpty()) {
        APosition enemyBase = AEnemyUnits.enemyBase();
        if (enemyBase == null) {
            return false;
        }

//        if (A.everyNthGameFrame(20)) {
            if (enemyBase.distTo(unit) > 2) {
                return unit.move(enemyBase.position(), UnitActions.SCOUT, "Pioneer");
            }
            else if (unit.isMoving()) {
                unit.holdPosition("Scouted!");
                return true;
            }
//        }
//        }

        return false;
    }

}
