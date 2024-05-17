package atlantis.combat.missions.defend;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.micro.attack.DontAttackAlone;
import atlantis.combat.micro.attack.DontAttackUnitScatteredOnMap;
import atlantis.combat.missions.generic.MissionAllowsToAttackEnemyUnit;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class MissionDefendAllowsToAttack extends MissionAllowsToAttackEnemyUnit {
    public MissionDefendAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (enemy == null) {
            throw new RuntimeException("aaa");
//            if (true) return true;
        }


        if (We.protoss()) {
            if (unit.hp() >= 50 && unit.isTargetInWeaponRangeAccordingToGame(enemy)) return true;
            if (EnemyWhoBreachedBase.notNull()) return true;

            if (!A.isUms()) {
                AFocusPoint focusPoint = unit.focusPoint();
                if (focusPoint != null && focusPoint.distTo(enemy) >= 10) return false;
            }

            if (Alpha.count() <= 4) {
                int rangeBonus = unit.isRanged() ? 2 : 1;
                if (!unit.canAttackTargetWithBonus(enemy, rangeBonus)) return true;
                return false;
            }

            if (unit.isRanged()) {
                if (EnemyInfo.noRanged()) {
                    if (unit.shieldDamageAtMost(9)) return true;
                    if (unit.hp() >= 40 && unit.lastAttackFrameLessThanAgo(30 * 4)) return true;
                }

                if (unit.distToLeader() >= 13 && unit.friendsInRadiusCount(4) <= 4) return false;

                if (
                    unit.shieldDamageAtLeast(20)
                        && unit.lastAttackFrameMoreThanAgo(unit.lastUnderAttackAgo() + 30)
                ) return true;

                if (EnemyInfo.noRanged() && unit.isSafeFromMelee()) return true;

                return unit.friendsInRadiusCount(3) >= 3
                    || unit.distToLeader() <= 6;
            }

            if (unit.isMelee()) {
                if (unit.hp() >= 40 && unit.lastAttackFrameLessThanAgo(30 * 4)) return true;
                if (unit.meleeEnemiesNearCount(1.2) >= 3) return false;

                return unit.friendsInRadiusCount(2) >= 4
                    || (unit.distToLeader() <= 3 && Count.ourCombatUnits() >= 3)
                    || unit.distToDragoon() <= 6;
            }
        }

//        if (unit.isRanged()) return true;

        if (!unit.isMissionSparta() && DontAttackAlone.isAlone(unit)) return false;
        if (unit.isMissionSparta() && unit.isMelee() && !enemy.hasCooldown()) {
            AFocusPoint focusPoint = unit.mission().focusPoint();
            if (focusPoint != null && unit.distTo(focusPoint) >= 2) return false;
        }

        AUnit leader = unit.squadLeader();
        if (leader != null) {
            if (leader.lastAttackFrameLessThanAgo(30 * 3)) return true;
            if (leader.distTo(unit) > 10) return false;
        }

        if (!enemy.hasPosition() || enemy.effUndetected()) {
            return false;
        }
        if (focusPoint == null) return true;

//        if (forbidAsTooFarFromFocusPoint(enemy)) return false;

        if (
            unit.isTargetInWeaponRangeAccordingToGame(enemy)
                || (unit.noCooldown() && enemy.canAttackTarget(unit) && (unit.isRanged() || focusPoint.regionsMatch(enemy)))
                || ourBuildingIsInDanger(unit, enemy)
        ) return true;

//        System.err.println("@ " + A.now() + " - not allowed to att " + unit.id() + " / " + enemy.type());

        return false;

//        if (focusPoint.regionsMatch(enemy)) {
//            return whenTargetInSameRegion(unit, enemy);
//        }
//        else {
//            return whenTargetInDifferentRegions(unit, enemy);
//        }
    }

    private boolean forbidAsTooFarFromFocusPoint(AUnit enemy) {
        if (unit.distToFocusPoint() < 15) return false;
        if (unit.distToBase() < 10) return false;

        return unit.hp() <= 20 || unit.lastAttackFrameMoreThanAgo(30 * 4);
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

    private boolean whenTargetInDifferentRegions(AUnit unit, AUnit enemy) {
        return false;
    }

    private boolean ourBuildingIsInDanger(AUnit unit, AUnit enemy) {
        Selection ourBuildings = Select.ourBuildings().inRadius(enemy.groundWeaponRange() + 0.5, enemy);
        if (unit.isAir()) {
            if (ourBuildings.atLeast(2) || ourBuildings.combatBuildingsAntiLand().notEmpty()) {
                return true;
            }
        }

        if (ourBuildings.combatBuildings(true).notEmpty()) return true;

        return false;
    }
}
