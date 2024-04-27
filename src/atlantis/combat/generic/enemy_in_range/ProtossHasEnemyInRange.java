package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.Enemy;
import atlantis.util.We;
import net.bytebuddy.description.NamedElement;

public class ProtossHasEnemyInRange extends Manager {
    private AUnit enemyInRange;

    public ProtossHasEnemyInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!We.protoss()) return false;
        if (!unit.isCombatUnit()) return false;
        if (unit.enemiesNear().empty()) return false;

        return unit.lastAttackFrameMoreThanAgo(30 * (unit.isMelee() ? 2 : 5))
            && (!Enemy.protoss() || fairlyHealthyOrSafeFromMelee())
            && unit.cooldown() <= 8
            && (unit.woundHp() <= 80 && notTooManyEnemiesNear())
//            && unit.combatEvalRelative() > 1
//            && !unit.hasTarget()
            && (enemyInRange = ProtossGetEnemyInRange.getEnemyInRange(unit)) != null
            && allowedToAttackThisEnemy();
    }

    private boolean fairlyHealthyOrSafeFromMelee() {
        return unit.woundHp() <= 14
            || unit.meleeEnemiesNearCount(1.5 + unit.woundPercent() / 38.0) == 0;
    }

    private boolean notTooManyEnemiesNear() {
        int maxEnemies = unit.isRanged() ? 0 : (2 + (unit.woundPercent() < 30 ? 1 : 0));

        return unit.enemiesNear().inRadius(1.4, unit).atMost(maxEnemies);
    }

    private boolean allowedToAttackThisEnemy() {
        int lastAttackFrameAgo = unit.lastAttackFrameAgo();
//        if (unit.lastUnderAttackAgo() < lastAttackFrameAgo / 2) return true;

        if (unit.isMelee()) {
            return allowedToAttackAsMelee(lastAttackFrameAgo);
        }
        else {
            if (true) return false;
            return allowedToAttackAsRanged();
        }
    }

    private boolean allowedToAttackAsMelee(int lastAttackFrameAgo) {
        if (lastAttackFrameAgo > 30 * 10) return true;
        if (lastAttackFrameAgo <= 30 && unit.cooldown() >= 8) return false;

        if (unit.isMissionDefendOrSparta()) return unit.hp() >= 18 && unit.friendsNear().inRadius(1.5, unit).atLeast(1);
        if (enemyInRange.hp() < unit.hp()) return true;

        return unit.hp() >= (unit.isMelee() ? 21 : 40);
    }

    private boolean allowedToAttackAsRanged() {
        if (unit.cooldown() >= 14) return false;
        if (unit.hp() < 41) return false;
        if (unit.lastUnderAttackLessThanAgo(30)) return false;
        if (unit.meleeEnemiesNearCount(1.7 + unit.woundPercent() / 39.0) > 0) return false;

        return unit.distTo(enemyInRange) >= 1.5 + unit.woundPercent() / 55.0;
    }

    @Override
    protected Manager handle() {
        if (unit.isRanged()) {
            A.printStackTrace("lol " + unit);
        }

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);

        return null;
    }
}
