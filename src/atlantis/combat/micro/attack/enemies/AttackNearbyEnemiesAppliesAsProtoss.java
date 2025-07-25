package atlantis.combat.micro.attack.enemies;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;

public class AttackNearbyEnemiesAppliesAsProtoss {
    public static Decision decision(AUnit unit) {
        if (dontAttackNearEnemyBaseIfFocusPointIsExpansion(unit)) return Decision.FORBIDDEN;

        if (unit.isReaver()) return Decision.FORBIDDEN;

        if (unit.enemiesNear().buildings().nearestToDistMore(unit, 20)) {
            return Decision.FORBIDDEN;
        }

        if (unit.isDragoon()) {
            if (unit.shieldDamageAtLeast(14)) {
                return unit.cooldown() <= 12 ? Decision.ALLOWED : Decision.FORBIDDEN;
            }

            if (unit.hp() <= 21) return Decision.FORBIDDEN;

            if (unit.hp() >= 61 && unit.shotSecondsAgo() >= 7) return Decision.ALLOWED;

//            if (unit.meleeEnemiesNearCount(2.5) >= 2) return Decision.FORBIDDEN;
            if (unit.shotSecondsAgo() >= 4) return Decision.ALLOWED;
//            if (unit.meleeEnemiesNearCount(2.6 + unit.woundPercent() / 140.0) >= 1) return Decision.FORBIDDEN;

            return Decision.ALLOWED;
        }

        if (Alpha.count() <= 5) {
            if (unit.isMelee() && unit.shieldDamageAtLeast(11)) {
                if (
                    unit.meleeEnemiesNearCount(1.3) >= 3
                        && unit.friendsNear().melee().countInRadius(1.2, unit) == 0
                ) return Decision.FORBIDDEN;
            }
        }

        if (unit.lastStoppedRunningLessThanAgo(1) && unit.hp() >= 40) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - SRun:" + unit.lastStoppedRunningAgo());
            return Decision.ALLOWED;
        }

        if (EnemyInfo.hasRanged() && unit.distToLeader() >= 15) return Decision.FORBIDDEN;

        if (
            unit.squadSize() >= 4
                && (unit.friendsInRadiusCount(2) == 0 && unit.friendsInRadiusCount(4) <= 1)
        ) return Decision.FORBIDDEN;

        return Decision.INDIFFERENT;
    }

    // Broken!!!
    private static boolean dontAttackNearEnemyBaseIfFocusPointIsExpansion(AUnit unit) {
        AFocusPoint focus = unit.focusPoint();
        if (focus == null) return false;

        if (A.supplyUsed() <= 60) return false;
        if (unit.eval() >= 4) return false;
        if (unit.cooldown() == 0 && unit.enemiesNear().inRadius(6, unit).atLeast(1)) return false;

        if (focus.groundDistanceTo(unit) >= 15 && focus.nameContains("Third", "Expansion")) {
//            if (
//                unit.cooldown() <= 5
////                    && unit.combatEvalRelative() >= 1.5
//                    && unit.enemiesNear().canBeAttackedBy(unit, -0.1).notEmpty()
//            ) return false;

//            if (unit.isMelee()) return true;
//            return unit.cooldown() <= 2 && unit.canA
            unit.setTooltip("PreferExpansionMoveThere");
            return true;
        }

        return false;
    }
}
