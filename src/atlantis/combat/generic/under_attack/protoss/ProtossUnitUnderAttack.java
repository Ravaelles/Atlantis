package atlantis.combat.generic.under_attack.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.generic.enemy_in_range.ProtossGetEnemyInRange;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.decions.Decision;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class ProtossUnitUnderAttack extends Manager {
    public ProtossUnitUnderAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!Enemy.zerg() && !unit.isMelee()) return false;

        if (!unit.isCombatUnit()) return false;
        if (unit.cooldown() >= 7) return false;
        if (unit.lastUnderAttackMoreThanAgo(30 * 2)) return false;

        Decision decision;

        if ((decision = appliesForDragoon()).notIndifferent()) return decision.toBoolean();

        if (preventForZealot()) return false;

        return unit.isCombatUnit()
            && unit.isGroundUnit()
//            && unit.noCooldown()
//            && unit.lastActionMoreThanAgo(30, Actions.MOVE_DANCE_AWAY)
            && unit.hp() >= 61
            && unit.hasAnyWeapon()
            && (
            unit.hp() >= 18
                || !unit.recentlyMoved(20)
                || shouldAttackBackBecauseOverstackedAndCantRun()
        )
            && unit.enemiesNear().canBeAttackedBy(unit, 0).notEmpty();
    }

    private boolean preventForZealot() {
        return unit.isZealot()
            && unit.lastAttackFrameLessThanAgo(30 * 3)
            && unit.combatEvalRelative() <= 0.7;
    }

    private Decision appliesForDragoon() {
        if (!unit.isDragoon()) return Decision.INDIFFERENT;

        if (unit.woundHp() <= (Enemy.protoss() ? 13 : 6)) return Decision.TRUE;
        if (unit.lastAttackFrameMoreThanAgo(30 * 5)) return Decision.TRUE;
        if (unit.meleeEnemiesNearCount(3.2) > 0) return Decision.FALSE;
        if (unit.hp() >= 17 && ProtossFlags.dragoonBeBrave()) return Decision.TRUE;

        return unit.lastStartedAttackLessThanAgo(30 * (unit.shields() >= 16 ? 1 : 3))
            && (
            unit.lastUnderAttackLessThanAgo(30 * 4)
                || unit.enemiesNear(4.5).ranged().notEmpty()
        ) ? Decision.TRUE : Decision.INDIFFERENT;
    }

    private boolean shouldAttackBackBecauseOverstackedAndCantRun() {
//        if (!unit.isRunning()) return false;
        if (unit.allUnitsNear().groundUnits().inRadius(0.7, unit).atMost(2)) return false;
        if (unit.friendsNear().groundUnits().combatUnits().inRadius(3, unit).atMost(4)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = ProtossGetEnemyInRange.enemyInRange(unit);
        if (enemyInRange == null) return null;

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) {
            return usedManager(this, "FightBack");
        }

//        if ((new AttackNearbyEnemies(unit)).invoked(this)) {
//            return usedManager(this, "FightBack");
//        }

        return null;
    }
}
