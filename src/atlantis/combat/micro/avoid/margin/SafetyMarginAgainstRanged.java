package atlantis.combat.micro.avoid.margin;

import atlantis.combat.micro.avoid.margin.protoss.DragoonSafetyMarginAgainstRanged;
import atlantis.combat.micro.avoid.margin.terran.BonusForWraith;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;
import bwapi.Color;

public class SafetyMarginAgainstRanged extends SafetyMargin {
    public SafetyMarginAgainstRanged(AUnit defender) {
        super(defender);
    }

    public double marginAgainst(AUnit attacker) {
        double criticalDist;

        if (A.hasMinerals(1500) && defender.isGroundUnit()) {
            if (defender.hp() >= 20 && defender.maxHp() <= 60) return 0;
            if (defender.hp() >= 90 && defender.maxHp() > 60) return 0;
        }

        // GROUND unit
        if (defender.isGroundUnit()) {
            criticalDist = forGroundUnit(attacker);

            if (We.protoss()) {
                if (defender.isDragoon()) {
                    return (new DragoonSafetyMarginAgainstRanged(defender)).marginAgainst(attacker);
                }
            }
            else if (We.terran()) {
                if (defender.isGhost()) {
                    criticalDist += bonusForGhost(attacker);
                }
                else if (defender.isWraith()) {
                    criticalDist += BonusForWraith.bonusForWraith(attacker, defender);
                }
                else if (defender.isScienceVessel()) {
                    criticalDist += 1 + defender.woundPercent() / 25.0;
                }
            }
        }

        // AIR unit
        else {
            criticalDist = forAirUnit(attacker);
        }

        // === For all ==================================

        criticalDist += addBuildingBonus(attacker, criticalDist);
        criticalDist += shouldRetreatBonus(defender);
//            addBuildingBonus(attacker, criticalDist)
//        if (attacker.isCombatBuilding()) {

//        }

        // ==============================================

        return criticalDist;
    }

    private double bonusForGhost(AUnit attacker) {
        if (attacker.isCombatBuilding()) {
            return 7;
        }

        return defender.woundPercent() / 25.0;
    }

    private double shouldRetreatBonus(AUnit defender) {
//        if (ShouldRetreat.shouldRetreat(defender)) {
//            return 4.2;
//        }

        return 0;
    }

    private double forGroundUnit(AUnit attacker) {
        return enemyWeaponRange(attacker)
            + quicknessBonus(attacker)
            + lurkerBonus(attacker)
            + woundedBonus(attacker)
            + ourUnitsNearBonus(defender)
            + ourNotMovingPenalty(defender)
            + enemyMovementBonus(attacker)
            + scoutBonus(attacker)
//            + combatEvalBonus(attacker)
            + asWorkerBonus(attacker);
    }

    private double forAirUnit(AUnit attacker) {
        return 3
            + enemyWeaponRange(attacker)
            + woundedBonus(attacker)
            + specialAirUnitBonus(defender)
            + ourNotMovingPenalty(defender)
            + enemyMovementBonus(attacker);
//        return applyAirUnitTweaks(attacker);
    }

    // =========================================================

    protected double woundedBonus(AUnit attacker) {
//        if (defender.isDragoon() && defender.hpLessThan(25)) {
//            return 3.4;
//        }

        // Don't apply wound bonus against units with bigger or equal range
        if (attacker.groundWeaponRange() >= defender.groundWeaponRange()) {
            return 0;
        }

        return super.woundedBonus(attacker);
    }

    private double addBuildingBonus(AUnit attacker, double criticalDist) {
        if (!attacker.isCombatBuilding()) {
            return 0;
        }

        APainter.paintTextCentered(attacker, "DefBuilding", Color.Orange);
        APainter.paintCircle(attacker, 7 * 32, Color.Orange);

        return 2.2
            + defender.woundPercent() / 70.0
            + (defender.isMoving() ? 0.5 : 0)
            + (defender.isAir() ? -0.9 : 0)
            + (defender.lastUnderAttackMoreThanAgo(30 * 100) ? -1.5 : 0);
    }

//    private double extraMarginAgainstCombatBuilding(AUnit attacker) {
//        if (defender.isVulture()) {
//            return 6.4;
//        } else if (defender.is(AUnitType.Terran_Wraith)) {
//            return 7.3;
//        }
//
//        return (defender.isAir() ? 5.8 : 1.1);
//    }

    private double lurkerBonus(AUnit attacker) {
        if (attacker.is(AUnitType.Zerg_Lurker) && attacker.effUndetected()) {
            return 3.6;
        }

        return 0;
    }

    private double scoutBonus(AUnit attacker) {
        return defender.isScout() ? (5 + defender.woundPercent() / 31.0) : 0;
    }

    private double combatEvalBonus(AUnit attacker) {
//        if (!ACombatEvaluator.isSituationFavorable(defender)) {
//            return -3;
//        }

        return 0;
    }

}
