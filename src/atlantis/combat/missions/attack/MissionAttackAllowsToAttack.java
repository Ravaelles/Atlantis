package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

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

        if (forbiddenToAttackCombatBuilding(enemy)) return false;
        if (forbiddenToAttackWithinChoke(enemy)) return false;
        if (dontAttackAsSquadScout(enemy)) return false;
        if (dontAttackDuringContain(enemy)) return false;

        return true;
    }

    private boolean dontAttackDuringContain(AUnit enemy) {
        if (!unit.isActiveManager(DontAdvanceButHoldAndContainWhenEnemyBuildingsClose.class)) return false;

        if (enemy.isABuilding() && unit.groundWeaponRange() <= 7 && enemy.distToNearestChoke() <= 9) return true;

        AUnit squadLeader = unit.squadLeader();
        if (squadLeader == null) return false;

        return squadLeader.isActiveManager(DontAdvanceButHoldAndContainWhenEnemyBuildingsClose.class);
    }

    private boolean dontAttackAsSquadScout(AUnit enemy) {
        if (!unit.isSquadScout()) return false;
        if (enemy.isWorker()) return false;

        return unit.hasCooldown() || unit.woundPercent() >= 20;
    }

    private boolean forbiddenToAttackWithinChoke(AUnit enemy) {
        if (unit.isAir() || unit.isMelee()) return false;
        if (enemy.isABuilding()) return false;

        if (A.supplyUsed() >= 190 || A.hasMinerals(2500)) return false;

        return unit.isWithinChoke();
    }

    private boolean forbiddenToAttackCombatBuilding(AUnit enemy) {
        if (enemy.isABuilding()) {
            Manager manager = (new DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(unit)).invoke(this);
            if (manager != null) return true;
        }

        return false;
    }
}
