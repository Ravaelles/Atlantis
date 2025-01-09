package atlantis.combat.missions.attack;

import atlantis.combat.advance.contain.ContainEnemy;
import atlantis.combat.micro.attack.DontAttackUnitScatteredOnMap;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.game.player.Enemy;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class MissionAttackAllowsToAttack extends HasUnit {
    public MissionAttackAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
//        if (A.supplyUsed() <= 40) {
//            // Zealots vs Zealot fix
//            if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
//                return true;
//            }
//        }

//        if (true) return true;

        if (!enemy.isAlive() || enemy.isDead()) return forbidden("EnemyDead");
        if (!enemy.hasPosition()) return forbidden("EnemyWithoutPosition");

        if (
            unit.isRanged()
                && unit.hp() >= 25
                && unit.eval() >= 1.2
                && unit.isTargetInWeaponRangeAccordingToGame(enemy)
        ) return true;

        if (A.minerals() < 1000 && A.supplyUsed() <= 110) {
            HasPosition squadCenter = unit.squadCenter();
            if (squadCenter != null && enemy.distToSquadCenter() >= 20 && unit.eval() < 2.0) return forbidden(
                "TooFarFromCenter");
        }

        if (unit.canAttackTargetWithBonus(enemy, 0)) return true;
        if (Enemy.zerg() && unit.isMelee() && enemy.isMelee() && unit.distToNearestChokeLessThan(1)) return true;

//        if (DontAttackAlone.isAlone(unit)) return false;
        if (DontAttackUnitScatteredOnMap.isEnemyScatteredOnMap(unit, enemy)) return forbidden("ScatteredEnemy");

        if (forbiddenToAttackCombatBuilding(enemy)) return forbidden("ForbiddenCB");
        if (dontAttackAsSquadScout(enemy)) return forbidden("SScout");

        if (leaderJustAttacked()) return true;

        if (dontAttackDuringContain(enemy)) return forbidden("Containing");
        if (forbiddenToAttackWithinChoke(enemy)) return forbidden("WithinChoke");

        return true;
    }

    protected boolean forbidden(String reason) {
//        ErrorLog.debug(reason);
        return false;
    }

    protected boolean preventProtossFromChasingScatteredLings(AUnit enemy) {
        if (!We.protoss()) return false;
        if (!Enemy.zerg()) return false;

        return enemy.isZergling()
            && EnemyInfo.hasRanged()
            && !unit.isTargetInWeaponRangeAccordingToGame(enemy)
            && enemy.friendsNear().groundUnits().countInRadius(3, enemy) <= 0;
    }

    private boolean leaderJustAttacked() {
        AUnit leader = unit.squadLeader();
        return leader != null
            && leader.distTo(unit) <= 8
            && leader.lastAttackFrameLessThanAgo(30 * 3);
    }

    private boolean dontAttackDuringContain(AUnit enemy) {
        if (!unit.isActiveManager(ContainEnemy.class)) return false;

        if (enemy.isABuilding() && unit.groundWeaponRange() <= 7 && enemy.distToNearestChoke() <= 9) return true;

        AUnit squadLeader = unit.squadLeader();
        if (squadLeader == null) return false;

        return squadLeader.isActiveManager(ContainEnemy.class);
    }

    private boolean dontAttackAsSquadScout(AUnit enemy) {
        if (!unit.isSquadScout()) return false;
        if (enemy.isWorker()) return false;
        if (unit.friendsNear().inRadius(1.5, unit).count() > 0) return false;

        return unit.hasCooldown() || unit.woundPercent() >= 20;
    }

    private boolean forbiddenToAttackWithinChoke(AUnit enemy) {
        if (unit.isAir() || unit.isMelee()) return false;
        if (enemy.isABuilding()) return false;
        if (A.supplyUsed() >= 190 || A.hasMinerals(2500)) return false;

        return (unit.lastUnderAttackLessThanAgo(30 * 9) || unit.hasCooldown())
            && unit.isWithinChoke();
    }

    private boolean forbiddenToAttackCombatBuilding(AUnit enemy) {
        return false;

//        if (!enemy.isCombatBuilding()) return false;
//
//        if (unit.distTo(enemy) <= 4) return false;
//
//        int minUnits = We.protoss() ? 4 : 9;
//        return unit.friendsNear().inRadius(5, unit).count() <= minUnits;
    }

    private boolean notAllowedToAttackCombatBuilding(AUnit enemy) {
        if (unit.distTo(enemy) <= 4) return false;

        int minUnits = We.protoss() ? 4 : 9;
        return unit.friendsNear().inRadius(5, unit).count() >= minUnits;
    }
}
