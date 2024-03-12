package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
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
        if (enemy.isCombatBuilding() && notAllowedToAttackCombatBuilding(enemy)) {
            return true;
        }

        if (enemy.isABuilding()) {
            Manager manager = (new DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(unit)).invoke(this);
            if (manager != null) return true;
        }

        return false;
    }

    private boolean notAllowedToAttackCombatBuilding(AUnit enemy) {
        if (unit.distTo(enemy) <= 4) return true;

        int minUnits = We.protoss() ? 5 : 9;
        return unit.friendsNear().inRadius(5, unit).count() >= minUnits;
    }
}
