package atlantis.combat.micro.attack;

import atlantis.combat.micro.avoid.terran.fight.MarineCanAttackNearEnemy;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;

public class AttackNearbyEnemiesApplies extends HasUnit {
    public AttackNearbyEnemiesApplies(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
//        if (unit.isAttacking() && unit.lastActionLessThanAgo(6, Actions.ATTACK_UNIT) && unit.hasTarget()) return false;
        if (unit.cooldown() >= 7) return false;
        if (unit.enemiesNear().empty()) return false;
        if (unit.isSpecialMission() && unit.isMelee()) return false;
//        if (ShouldRetreat.shouldRetreat(unit)) return false;
        if (dontAttackAlone()) return false;

        if (unit.isDragoon() && unit.lastActionLessThanAgo(1)) return false;

        if (unit.manager().equals(this) && unit.looksIdle() && unit.enemiesNear().empty()) return false;
        if (unit.lastStartedRunningLessThanAgo(8)) return false;
        if (!unit.hasAnyWeapon()) return false;
        if (!CanAttackAsMelee.canAttackAsMelee(unit)) return false;
        if (unit.isDragoon() && unit.lastActionLessThanAgo(1)) return false;

        if (unit.isMarine()) return MarineCanAttackNearEnemy.allowedForThisUnit(unit);

        return true;
    }

    private boolean dontAttackAlone() {
        if (unit.canBeLonelyUnit()) return false;

        if (
            unit.isRanged()
                && unit.woundPercent() <= 10
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
