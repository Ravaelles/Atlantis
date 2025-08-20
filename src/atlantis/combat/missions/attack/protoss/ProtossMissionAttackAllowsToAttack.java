package atlantis.combat.missions.attack.protoss;

import atlantis.combat.missions.attack.MissionAttackAllowsToAttack;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.game.player.Enemy;
import atlantis.units.select.Count;

public class ProtossMissionAttackAllowsToAttack extends MissionAttackAllowsToAttack {
    public ProtossMissionAttackAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (unit.isAir()) return true;

        if (unit.leaderIsRetreating()) return forbidden("LeaderRetreating");
        if (closeToEnemyCombatBuilding(enemy)) return forbidden("CloseToEnemyCB");

        Decision decision;
        if ((decision = asDT(enemy)).notIndifferent())return decision.toBoolean();

        if (Enemy.protoss()) {
            if (Count.ourCombatUnits() >= 10) return true;
            if (unit.isDragoon() && enemy.isZealot()) return true;
            if (unit.isDragoon() && unit.eval() >= 1.5 && unit.isTargetInWeaponRangeAccordingToGame(enemy)) return true;
        }

        if (Enemy.zerg()) {
            if (preventProtossFromChasingScatteredLings(enemy)) return forbidden("ChasingLings");
            if (preventTooLonelyMelee(enemy)) return forbidden("TooLonelyMelee");
        }

        if (Enemy.zerg() || Enemy.terran()) {
            double distToEnemy = unit.distTo(enemy);

            if (unit.isMelee() && distToEnemy <= 1.4) {
                if (unit.shotSecondsAgo() >= 6) return true;
            }
        }

        return super.allowsToAttackEnemyUnit(enemy);
    }

    private Decision asDT(AUnit enemy) {
        if (!unit.isDarkTemplar()) return Decision.INDIFFERENT;

        if (
            (enemy.isSunken() || enemy.isCannon()) && enemy.enemiesNear().countInRadius(3, unit) == 0
        ) return Decision.FORBIDDEN;

        return Decision.INDIFFERENT;
    }

    private boolean closeToEnemyCombatBuilding(AUnit enemy) {
        if (unit.enemiesNear().combatBuildingsAnti(unit).countInRadius(14, unit) == 0) return false;

        if ((unit.eval() <= 6 && Count.ourCombatUnits() <= 25)) {
            return forbidden("ForbidNearCb");
        }

        return false;
    }

    private boolean preventTooLonelyMelee(AUnit enemy) {
        if (!unit.isMelee()) return false;
        if (unit.enemiesNear().empty()) return false;
        if (unit.eval() >= 2) return false;
        if (unit.squadSize() <= 2) return false;
        if (unit.lastPositionChangedAgo() >= 2) return false;

        return unit.eval() <= 2
            && (unit.eval() <= 1.4 || unit.squad().lastAttackedMoreThanAgo(10))
            && unit.friendsInRadius(1.2).count() <= 0;
    }
}
