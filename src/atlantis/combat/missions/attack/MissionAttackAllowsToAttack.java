package atlantis.combat.missions.attack;

import atlantis.combat.advance.contain.TerranContainEnemyWrapper;
import atlantis.combat.micro.attack.DontAttackUnitScatteredOnMap;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingClose;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class MissionAttackAllowsToAttack extends HasUnit {
    private AUnit enemy;

    public MissionAttackAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        this.enemy = enemy;

//        if (A.supplyUsed() <= 40) {
//            // Zealots vs Zealot fix
//            if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
//                return true;
//            }
//        }

//        if (true) return true;

        if (!enemy.isAlive() || enemy.isDead()) return forbidden("EnemyDead");
        if (!enemy.hasPosition()) return forbidden("EnemyWithoutPosition");

        if (forbidByType(enemy)) return forbidden("ForbiddenByType");

        if (
            unit.isActiveManager(AvoidCombatBuildingClose.class) && enemy.isCombatBuilding()
        ) return forbidden("ForbidCB");

        if (unit.squad() != null && unit.squad().hasMostlyOffensiveRole()) return true;
//        if (unit.isRanged() && unit.eval() >= 1.1 && unit.isTargetInWeaponRangeAccordingToGame(enemy)) return true;

        if (
            unit.isRanged()
                && unit.hp() >= 25
                && unit.eval() >= 1.2
                && unit.isTargetInWeaponRangeAccordingToGame(enemy)
        ) return true;

        if (Army.strength() >= 800 && enemy.isABuilding() && unit.eval() >= 2) return true;

        if (A.minerals() < 1000 && A.supplyUsed() <= 110) {
            HasPosition squadCenter = unit.squadCenter();
            if (
                squadCenter != null && enemy.distToSquadCenter() >= 20 && unit.eval() < 2.0
            ) return forbidden("TooFarFromCenter");
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

    private boolean forbidByType(AUnit enemy) {
        return unit.isDarkTemplar() && enemy.isDetector();
    }

    protected boolean forbidden(String reason) {
//        ErrorLog.debug(reason);
//        PauseAndCenter.on(unit, true, Color.Purple);
        if (enemy != null) {
            System.out.println(A.now + ": MAA forbids attack " + (enemy != null ? enemy.type() : enemy) + ": " + reason);
        }

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
        if (!unit.isActiveManager(TerranContainEnemyWrapper.class)) return false;

        if (enemy.isABuilding() && unit.groundWeaponRange() <= 7 && enemy.nearestChokeDist() <= 9) return true;

        AUnit squadLeader = unit.squadLeader();
        if (squadLeader == null) return false;

        return squadLeader.isActiveManager(TerranContainEnemyWrapper.class);
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
