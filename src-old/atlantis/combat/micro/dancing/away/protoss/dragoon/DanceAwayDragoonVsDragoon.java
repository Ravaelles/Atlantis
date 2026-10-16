package atlantis.combat.micro.dancing.away.protoss.dragoon;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class DanceAwayDragoonVsDragoon {
    protected static Decision check(AUnit unit, AUnit enemy) {
//        if (unit.hp() >= 62 && unit.lastAttackFrameMoreThanAgo(100)) return Decision.FALSE;

        Selection enemyGoons = unit.enemiesNear().dragoons();
        if (enemyGoons.empty()) return Decision.INDIFFERENT;

        if (unit.shieldWound() >= 23) {
            if (unit.cooldown() >= 12) return Decision.TRUE;
        }

//        if (unit.cooldown() <= 8) return Decision.FALSE;
//        if (unit.shieldWound() <= 23) return Decision.FALSE;
//        if (enemyGoons.canAttack(unit, 0).empty()) return Decision.FALSE;

        return Decision.FALSE;

//        if (unit.hp() >= 82 && unit.cooldown() <= 9) return Decision.FALSE;
//
//        return (unit.meleeEnemiesNearCount(DanceAwayDragoon.meleeEnemiesRadius(unit, enemy)) > 0)
//            ? Decision.TRUE : Decision.FALSE;

//        if (unit.shields() >= 40) return false;
//
//        if (unit.enemiesNearInRadius(enemiesRadius()) > 0) return true;
//
//        return false;
    }
}
