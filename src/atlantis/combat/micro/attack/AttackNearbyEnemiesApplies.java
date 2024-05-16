package atlantis.combat.micro.attack;

import atlantis.combat.micro.avoid.terran.fight.MarineCanAttackNearEnemy;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class AttackNearbyEnemiesApplies extends HasUnit {
    public AttackNearbyEnemiesApplies(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
//        if (unit.isAttacking() && unit.lastActionLessThanAgo(6, Actions.ATTACK_UNIT) && unit.hasTarget()) return false;
        if (unit.cooldown() >= 7) return false;
        if (unit.enemiesNear().empty()) return false;
        if (unit.isSpecialMission() && unit.isMelee()) return false;
        if (!unit.hasAnyWeapon()) return false;
        if (unit.lastStoppedRunningLessThanAgo(1)) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - SRun:" + unit.lastStoppedRunningAgo());
            return true;
        }
        if (unit.distToLeader() >= 15) return false;
        if (
            unit.squadSize() >= 4
                && (unit.friendsInRadiusCount(2) == 0 && unit.friendsInRadiusCount(4) <= 1)
        ) return false;
//        if (ShouldRetreat.shouldRetreat(unit)) return false;

        if (dontAttackAlone()) return false;

        if (unit.isDragoon()) {
            if (unit.shieldDamageAtLeast(14)) {
//                if (unit.meleeEnemiesNearCount(2.7) >= 1) return false;
//                if (unit.isHealthy() || unit.lastAttackFrameMoreThanAgo(30 * 4)) return true;
                return unit.cooldown() <= 10;
            }
            if (unit.meleeEnemiesNearCount(2.5) >= 2) return false;
            if (unit.meleeEnemiesNearCount(2.6 + unit.woundPercent() / 140.0) >= 1) return false;
            return true;
        }

        if (!CanAttackAsMelee.canAttackAsMelee(unit)) return false;

//        if (unit.isDragoon() && !unit.isAttacking() && unit.lastActionLessThanAgo(1)) return false;

//        if (unit.manager().equals(this) && unit.looksIdle() && unit.enemiesNear().empty()) return false;
//        if (unit.lastStartedRunningLessThanAgo(8)) return false;
//        if (unit.isDragoon() && unit.lastActionLessThanAgo(1)) return false;

        if (unit.isMarine()) return MarineCanAttackNearEnemy.allowedForThisUnit(unit);

        return true;
    }

    private boolean dontAttackAlone() {
        if (unit.canBeLonelyUnit()) return false;

        if (
            unit.isRanged()
//                && unit.woundPercent() <= 70
                && unit.enemiesNear().onlyMelee()
        ) return false;

        if (
            unit.isCombatUnit()
                && unit.distToLeader() >= 7
//                && unit.combatEvalRelative() <= 2.6
        ) return true;

        return unit.squadSize() >= 6
            && unit.friendsInRadiusCount(6) == 0
            && unit.enemiesNear().ranged().notEmpty();
    }
}
