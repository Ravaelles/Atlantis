package atlantis.combat.generic.under_attack.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.generic.enemy_in_range.ProtossGetEnemyInRange;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossUnitUnderAttack extends Manager {
    public ProtossUnitUnderAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isCombatUnit()) return false;
        if (unit.cooldown() <= 4) return false;
        if (unit.lastUnderAttackMoreThanAgo(30 * 2)) return false;
        if (
            unit.lastAttackFrameLessThanAgo(30 * 3)
                && unit.isZealot()
                && unit.combatEvalRelative() <= 0.7
        ) return false;

        if (preventForDragoon()) return false;

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

    private boolean preventForDragoon() {
        if (!unit.isDragoon()) return false;

        return unit.lastStartedAttackLessThanAgo(30 * (unit.shields() >= 16 ? 1 : 3))
            && !ProtossFlags.dragoonBeBrave()
            && (
            unit.lastUnderAttackLessThanAgo(30 * 4)
                || unit.enemiesNear(4.5).ranged().notEmpty()
        );
    }

    private boolean shouldAttackBackBecauseOverstackedAndCantRun() {
//        if (!unit.isRunning()) return false;
        if (unit.allUnitsNear().groundUnits().inRadius(0.7, unit).atMost(2)) return false;
        if (unit.friendsNear().groundUnits().combatUnits().inRadius(3, unit).atMost(4)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = ProtossGetEnemyInRange.getEnemyInRange(unit);

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) {
            return usedManager(this, "FightBack");
        }

//        if ((new AttackNearbyEnemies(unit)).invoked(this)) {
//            return usedManager(this, "FightBack");
//        }

        return null;
    }
}
