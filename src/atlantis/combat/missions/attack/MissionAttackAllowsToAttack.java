package atlantis.combat.missions.attack;

import atlantis.combat.advance.contain.ContainEnemy;
import atlantis.combat.micro.attack.DontAttackUnitScatteredOnMap;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.util.Enemy;
import atlantis.util.We;

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

        if (!enemy.isAlive() || enemy.isDead() || !enemy.hasPosition()) return false;

        if (unit.canAttackTargetWithBonus(enemy, 0)) return true;
        if (Enemy.zerg() && unit.isMelee() && enemy.isMelee() && unit.distToNearestChokeLessThan(1)) return true;

        HasPosition squadCenter = unit.squadCenter();
        if (squadCenter != null && enemy.distToSquadCenter() >= 20) return false;

//        if (DontAttackAlone.isAlone(unit)) return false;
        if (DontAttackUnitScatteredOnMap.isEnemyScatteredOnMap(unit, enemy)) return false;

        if (forbiddenToAttackCombatBuilding(enemy)) return false;
        if (dontAttackAsSquadScout(enemy)) return false;

        if (leaderJustAttacked()) return true;

        if (dontAttackDuringContain(enemy)) return false;
        if (forbiddenToAttackWithinChoke(enemy)) return false;

        return true;
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
