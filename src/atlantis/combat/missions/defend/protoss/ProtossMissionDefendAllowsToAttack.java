package atlantis.combat.missions.defend.protoss;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.avoid.dont.protoss.DontAvoidWhenCannonsNear;
import atlantis.combat.missions.generic.MissionAllowsToAttackEnemyUnit;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossMissionDefendAllowsToAttack extends MissionAllowsToAttackEnemyUnit {
    private AUnit enemy;

    public ProtossMissionDefendAllowsToAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        this.enemy = enemy;

        if (enemy == null || !enemy.hasPosition() || enemy.hp() <= 0) return false;

        if (DontAvoidWhenCannonsNear.check(unit) && (unit.hp() >= 42 || unit.cooldown() <= 6)) {
            return true;
        }

        if (protossDontAttackWhenAlmostDead()) return false;

        if (
            ProtossStickCombatToMainBaseEarly.should()
                && Select.ourBuildingsWithUnfinished().countInRadius(5, unit) == 0
                && (unit.hasCooldown() || !unit.isTargetInWeaponRangeAccordingToGame(enemy))
        ) {
            return false;
        }

        if (A.s >= 60 * 9) return true;

        boolean lowCooldown = unit.cooldown() <= 9;
        double distToEnemy = unit.distTo(enemy);

        if (Enemy.zerg()) {
            if (lowCooldown && enemyVeryCloseToBuilding()) return true;

            if (unit.isMelee() && distToEnemy <= 1.4) {
                if (unit.shotSecondsAgo() >= 8 && distToEnemy <= 1) return true;
                if (lowCooldown) return true;
                if (unit.cooldown() >= 12 && unit.shieldWound() >= 10) return false;
            }
        }
        else {
            if (enemyVeryCloseToBuilding()) return true;
        }

        if (forbidden_earlyGameVsStrongZergStickToMainBase()) return false;

        if (Enemy.zerg()) {
            if (unit.isMelee() && distToEnemy <= 1.1 && lowCooldown) return true;

            if (closeToBuildingsRelativelyAliveAndHaventAttackedRecently()) return true;
            if (earlyGameVsZergDontGoTooFar()) return false;
        }

        if (whenTooFarFromSquadCenter()) return false;

        if (asRangedAttackTargetsInRange()) return true;

        return true;
    }

    private boolean enemyVeryCloseToBuilding() {
//        if (Count.cannons() == 0) {
        return enemy.enemiesNear().buildings().inRadius(enemy.isMelee() ? 3 : 5.2, unit).notEmpty();
//        }
    }

    private boolean closeToBuildingsRelativelyAliveAndHaventAttackedRecently() {
        if (unit.hp() <= 30) return false;
        if (unit.combatEvalRelative() <= 0.8) return false;
        if (unit.friendsNear().buildings().inRadius(2.6, unit).empty()) return false;

        return unit.lastAttackFrameMoreThanAgo(30 * 3);
    }

    private boolean asRangedAttackTargetsInRange() {
        return (unit.isRanged() || unit.combatEvalRelative() >= 1.2)
            && unit.isTargetInWeaponRangeAccordingToGame(enemy);
    }

    private boolean whenTooFarFromSquadCenter() {
        int maxDist = 6;

        return unit.distToSquadCenter() >= maxDist
            && unit.distToFocusPoint() >= (unit.isMelee() ? 3 : 8)
            && unit.woundPercentMin(10)
            && (unit.groundDistToMain() + maxDist) < enemy.groundDistToMain();
    }


    private boolean protossDontAttackWhenAlmostDead() {
        if (unit.combatEvalRelative() >= 2 && unit.didntShootRecently(4)) return false;

        if (unit.isMelee()) {
            if (unit.hp() <= 25) return true; // Dont, avoid instead
        }
        else {
            if (unit.hp() <= 30 && unit.enemiesThatCanAttackMe(3.3).count() > 0) return true;
            if (unit.hp() <= 20) return true;
        }

        return unit.hp() <= 34
            && unit.shotSecondsAgo(5)
            && unit.enemiesThatCanAttackMe(3 + unit.woundPercent() / 100.0).notEmpty();
    }

    private boolean dontAttackOnYourOwn() {
        return unit.squadSize() >= 3
            && unit.friendsNear().inRadius(2.8, unit).empty()
            && unit.enemiesNear().ranged().canAttack(unit, 5).empty()
            && unit.enemiesThatCanAttackMe(2.5 + unit.woundPercent() / 50.0).empty()
            && unit.distToBase() >= 10;
    }

    private boolean forbidden_earlyGameVsStrongZergStickToMainBase() {
        if (!ProtossStickCombatToMainBaseEarly.should()) return false;
        if (unit.isRanged() && EnemyInfo.noRanged() && unit.hp() >= 35 && unit.noCooldown()) return false;
        if (unit.combatEvalRelative() >= 1.5) return false;

        if (OurBuildingUnderAttack.notNull()) return false;
        if (unit.friendsNear().workers().inRadius(2.4, unit).notEmpty()) return false;

        if (unit.hp() <= 15) return true;
        if (unit.cooldown() >= 4) return true;

        AFocusPoint focusPoint = unit.focusPoint();
        if (focusPoint == null) return false;

        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return false;

        if (unit.isMelee()) {
            if (Count.dragoons() >= 1) return false;
            if (unit.friendsNear().workers().inRadius(2, unit).notEmpty()) return false;

            if (enemy.distToMain() >= 7) return true;

            if (enemy.distToMain() <= 3) return false;
            if (unit.distTo(enemy) <= 2) return false;
        }

        return enemy.ourNearestBuildingWithUnfinishedDist() >= 1.5
            && unit.ourNearestBuildingWithUnfinishedDist() >= 1.5;

//        int enemyCombatUnits = EnemyUnits.combatUnits();
//
//        return A.s <= 60 * 7
////            && ProtossShouldPunishZergEarly.shouldPunishZergEarly().isTrue()
//            && unit.distToBase() >= 8
//            && (
//            unit.combatEvalRelative() <= 1.8
//                || (Enemy.zerg() && Count.ourCombatUnits() <= 5)
//                || (Enemy.zerg() && Count.ourCombatUnits() <= 2.3 * EnemyUnits.combatUnits())
//        )
//            && OurBuildingUnderAttack.get() == null
//            && (Count.dragoons() <= 1 && (enemyCombatUnits >= 12 && enemyCombatUnits >= 2.8 * Count.ourCombatUnits()));
    }

    private boolean forbidAsTooFarFromFocusPoint(AUnit enemy) {
        if (unit.distToFocusPoint() < 15) return false;
        if (unit.distToBase() < 10) return false;

        return unit.hp() <= 20 || unit.lastAttackFrameMoreThanAgo(30 * 4);
    }


    private boolean earlyGameVsZergDontGoTooFar() {
        if (!Enemy.zerg()) return false;

        if (
            Count.ourCombatUnits() <= 8
                && (Count.dragoons() <= 2 && OurArmy.strength() <= 170)
//                && unit.distToBase() >= (16 + unit.hpPercent() / 7.0)
                && unit.distToFocusPoint() >= 7
        ) return true;

        return false;
    }

    private Decision asRanged(AUnit enemy) {
        if (!unit.isRanged()) return Decision.INDIFFERENT;

        if (unit.isMissionDefend()) {
            if (!EnemyInfo.hasRanged()) {
                return unit.hp() >= 25
                    && unit.distTo(enemy) >= 3
                    && unit.isTargetInWeaponRangeAccordingToGame(enemy)
                    ? Decision.TRUE : Decision.INDIFFERENT;
            }
        }

        return Decision.INDIFFERENT;
    }

    private boolean whenTargetInSameRegion(AUnit unit, AUnit enemy) {
        Selection sunkens = Select.ourOfType(AUnitType.Zerg_Sunken_Colony);

        if (
            unit.isMelee()
                && sunkens.inRadius(15, enemy).notEmpty()
                && sunkens.inRadius(7, enemy).empty()
        ) return false;

        // =========================================================

        int friends = unit.friendsInRadiusCount(4);
        if (
            (
                enemy.isMelee()
                    || (unit.squadSize() >= 4 && friends <= 1)
            )
                && !unit.enemiesNear().inRadius(9, unit).onlyMelee()
        ) {
            unit.setTooltip("TooScarce");
            return false;
        }

        // =========================================================

        return (friends >= 2 && unit.combatEvalRelative() >= 2.5)
            || (friends >= 5 && unit.woundPercentMax(15));
    }
}
