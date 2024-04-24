package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossUnitHasEnemyInRange extends Manager {
    private AUnit enemyInRange;

    public ProtossUnitHasEnemyInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
//            && unit.squad().targeting().lastTargetIfAlive() == null
            && unit.isCombatUnit()
            && unit.enemiesNear().notEmpty()
//            && unit.lastAttackFrameMoreThanAgo(45)
            && unit.cooldown() <= 8
            && (unit.woundHp() <= 50 || unit.lastAttackFrameMoreThanAgo(90) || notTooManyEnemiesNear())
//            && unit.combatEvalRelative() > 1
//            && !unit.hasTarget()
            && (enemyInRange = ProtossGetEnemyInRange.getEnemyInRange(unit)) != null
            && allowedToAttackThisEnemy();
    }

    private boolean notTooManyEnemiesNear() {
        int maxEnemies = 2 + (unit.woundPercent() < 30 ? 1 : 0);

        return unit.enemiesNear().inRadius(1.4, unit).atMost(maxEnemies);
    }

    private boolean allowedToAttackThisEnemy() {
        int lastAttackFrameAgo = unit.lastAttackFrameAgo();
        if (unit.lastUnderAttackAgo() < lastAttackFrameAgo / 2) return true;

        if (unit.isMelee()) {
            return asMelee(lastAttackFrameAgo);
        }
        else {
            return asRanged();
        }
    }

    private boolean asMelee(int lastAttackFrameAgo) {
        if (lastAttackFrameAgo > 30 * 10) return true;
        if (lastAttackFrameAgo <= 30 && unit.cooldown() >= 8) return false;

        if (unit.isMissionDefendOrSparta()) return unit.hp() >= 18 && unit.friendsNear().inRadius(1.5, unit).atLeast(1);
        if (enemyInRange.hp() < unit.hp()) return true;

        return unit.hp() >= (unit.isMelee() ? 21 : 40);
    }

    private boolean asRanged() {
        if (unit.hp() < (unit.isMelee() ? 21 : 40)) return false;
        if (unit.meleeEnemiesNearCount(1.7 + unit.woundPercent() / 43.0) > 0) return false;

        return unit.distTo(enemyInRange) >= 1.5 + unit.woundPercent() / 55.0;
    }

    @Override
    protected Manager handle() {
        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);

        return null;
    }
}
