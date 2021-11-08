package atlantis.combat.eval;

import atlantis.position.PositionUtil;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.WeaponUtil;

import java.util.Collection;

public class Evaluate {

    /**
     * Fight only if our army is locally stronger X% than enemy army. 0.5 = 50%.
     */
//    private static final double SAFETY_MARGIN_ATTACK = 0.4;
//    private static final double SAFETY_MARGIN_RETREAT = -0.4;

    /**
     * Multiplier for hit points factor when evaluating unit's combat value.
     */
    private static final double EVAL_HIT_POINTS_FACTOR = 0.3;

    /**
     * Multiplier for damage factor when evaluating unit's combat value.
     */
    private static final double EVAL_DAMAGE_FACTOR = 1.0;

    // =========================================================

    /**
     * Calculate total strength value of given units set.
     * It's always evaluated from a perspective of particular unit, in this case <b>againstUnit</b>.
     * Also it makes sense to distguish before enemy evaluation (we will almost always understimate enemy
     * strength) or our own evaluation (we're likely to overestimate our strength).
     */
    protected static double evaluateUnitsAgainstUnit(Collection<AUnit> units, AUnit againstUnit, boolean isEnemyEval) {
        double strength = 0.0;
        boolean enemyDefensiveBuildingFound = false;
        boolean enemyDefensiveBuildingInRange = false;

        // =========================================================
        for (AUnit unit : units) {
            double unitStrengthEval = evaluateUnitHPandDamage(unit, againstUnit);

            // =========================================================
            // WORKER

            if (unit.isWorker()) {
                strength += 0.15 * unitStrengthEval;
            }

            // =========================================================
            // BUILDING
            else if (unit.type().isBuilding() && unit.isCompleted()) {
                boolean antiGround = (againstUnit == null || !againstUnit.isAirUnit());
                boolean antiAir = (againstUnit == null || againstUnit.isAirUnit());
                if (unit.type().isMilitaryBuilding(antiGround, antiAir)) {
                    enemyDefensiveBuildingFound = true;
                    if (unit.is(AUnitType.Terran_Bunker)) {
                        strength += 7 * evaluateUnitHPandDamage(AUnitType.Terran_Marine, againstUnit);
                    } else {
                        strength += 1.3 * unitStrengthEval;
                    }

                    if (PositionUtil.distanceTo(unit, againstUnit) <= 8.5) {
                        enemyDefensiveBuildingInRange = true;
                    }
                }
            } // =========================================================
            // Ordinary MILITARY UNIT
            else {
                strength += unitStrengthEval;
            }
        }

        // =========================================================
        // Extra bonus for DEFENSIVE BUILDING PRESENCE

        if (!isEnemyEval) {
            if (enemyDefensiveBuildingFound) {
                strength += 10;
            }
            if (enemyDefensiveBuildingInRange) {
                strength += 10;
            }
        }

        return strength;
    }

    // =========================================================

    private static double evaluateUnitHPandDamage(AUnitType evaluate, AUnit againstUnit) {
//        return evaluateUnitHPandDamage(evaluate.type(), evaluate.hp(), againstUnit);
        return evaluateUnitHPandDamage(evaluate, evaluate.getMaxHitPoints(), againstUnit);
    }

    private static double evaluateUnitHPandDamage(AUnit evaluate, AUnit againstUnit) {
        return evaluateUnitHPandDamage(evaluate.type(), evaluate.hp(), againstUnit);
    }

//    private static double evaluateUnitHPandDamage(AUnit evaluate, AUnit againstUnit) {
////        System.out.println(evaluate.getType() + " damage: " + evaluate.getType().getGroundWeapon().getDamageNormalized());
//        return evaluateUnitHPandDamage(evaluate, againstUnit);
//    }

    private static double evaluateUnitHPandDamage(AUnitType type, int hp, AUnit againstUnit) {
        if (type.isMine() || type.isNeutralType() || type.isInvincible()) {
            return 0.0;
        }

        // === Special types =======================================

        double customEval;
        if ((customEval = customEvaluation(type, againstUnit)) >= 0) {
            return customEval;
        }

        // =========================================================

        double hpValue = (double) hp / type.getMaxHitPoints();

        int damage = againstUnit.isAirUnit()
                ? WeaponUtil.damageNormalized(type.getAirWeapon())
                : WeaponUtil.damageNormalized(type.getGroundWeapon());

        double total = hpValue * EVAL_HIT_POINTS_FACTOR
                + (damage * WeaponUtil.damageModifier(type, againstUnit.type())) * EVAL_DAMAGE_FACTOR;

        // =========================================================
        // Diminish role of NON-SHOOTING units

        return total;
    }

    private static double customEvaluation(AUnitType type, AUnit againstUnit) {
        if (type.is(AUnitType.Terran_Medic)) {
            return 5.5;
        }

        return -1.0;
    }

}
