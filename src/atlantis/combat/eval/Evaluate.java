package atlantis.combat.eval;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.util.WeaponUtil;

import java.util.Iterator;

public class Evaluate {

    /**
     * Fight only if our army is locally stronger X% than enemy army. 0.5 = 50%.
     */
//    private static final double SAFETY_MARGIN_ATTACK = 0.4;
//    private static final double SAFETY_MARGIN_RETREAT = -0.4;

    /**
     * Multiplier for hit points factor when evaluating unit's combat value.
     */
    private static final double EVAL_HIT_POINTS_FACTOR = 0.2;

    /**
     * Multiplier for damage factor when evaluating unit's combat value.
     */
    private static final double EVAL_DAMAGE_FACTOR = 0.8;

    // =========================================================

    /**
     * Calculate total strength value of given units set.
     * It's always evaluated from a perspective of particular unit, in this case <b>againstUnit</b>.
     * Also it makes sense to distguish before enemy evaluation (we will almost always understimate enemy
     * strength) or our own evaluation (we're likely to overestimate our strength).
     */
    protected static double evaluateUnitsAgainstUnit(Units theseUnits, Units againstUnits, boolean isEnemyEval) {
        double totalStrength = 0;
        boolean enemyCombatBuildingFound = false;
        AUnit againstUnit = againstUnits.first();

//        theseUnits.print("THESE");
//        againstUnits.print("AGAINST");

        if (againstUnit == null || !againstUnit.isAlive()) {
            return 0.0;
        }

        // =========================================================

        for (Iterator<AUnit> iterator = theseUnits.iterator(); iterator.hasNext(); ) {
            AUnit unit = iterator.next();
            double unitStrengthEval = evaluateUnitHPandDamage(unit, againstUnit);

            // =========================================================
            // WORKER

            if (unit.isWorker()) {
                totalStrength += 0.5 * unitStrengthEval;
            }

            // =========================================================
            // BUILDING

            else if (unit.isCombatBuilding() && unit.canAttackTarget(againstUnit)) {
                totalStrength += evaluateBuilding(unit, againstUnit, unitStrengthEval);
                enemyCombatBuildingFound = true;
//                enemyCombatBuildingInRange = unit.hasWeaponRangeToAttack(againstUnit, 2.5);
            }

            // === Infantry ============================================

            else if (unit.isTerranInfantry()) {
                totalStrength += terranInfantryTweak(unit, unitStrengthEval);
            }

            // =========================================================
            // Ordinary MILITARY UNIT

            else {
                totalStrength += unitStrengthEval;
            }

//            System.err.println(unit + " // eval = " + unitStrengthEval);
        }

        // =========================================================
        // Extra bonus for DEFENSIVE BUILDING PRESENCE

        if (enemyCombatBuildingFound) {
            totalStrength += theseUnits.onlyAir() ? 80 : 40;
        }

//        System.err.println("totalStrength = " + totalStrength + " (against " + againstUnit.type() + ")");

        return totalStrength;
    }

    // =========================================================

    private static double evaluateBuilding(AUnit unit, AUnit againstUnit, double unitStrengthEval) {
        double eval = 0;
        boolean antiGround = (againstUnit == null || !againstUnit.isAir());
        boolean antiAir = (againstUnit == null || againstUnit.isAir());
        if (unit.type().isMilitaryBuilding(antiGround, antiAir)) {

            if (unit.is(AUnitType.Terran_Bunker)) {
                eval += 7 * unitStrengthEval;
            } else {
                eval += 1.3 * unitStrengthEval;
            }
        }

        return eval;
    }

//    private static double evaluateUnitHPandDamage(AUnitType evaluate, AUnit againstUnit) {
////        return evaluateUnitHPandDamage(evaluate.type(), evaluate.hp(), againstUnit);
//        return evaluateUnitHPandDamage(evaluate, evaluate.getMaxHitPoints(), againstUnit);
//    }

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
        if ((customEval = customEvaluation(type, hp, againstUnit)) >= 0) {
            return customEval;
        }

        // =========================================================

        double hpFactor = hpFactor(type, hp);
        double damageFactor = damageFactor(type, againstUnit);
        double total = hpFactor + damageFactor;

//        System.out.println(type + " against " + againstUnit.name() + " // "
//                + total + " // "
//                + damageFactor + " // "
//                + hpFactor * EVAL_HIT_POINTS_FACTOR + " // "
//                + (damageFactor * WeaponUtil.damageModifier(type, againstUnit.type())) * EVAL_DAMAGE_FACTOR);

        // =========================================================
        // Diminish role of NON-SHOOTING units

        return total;
    }

    private static double damageFactor(AUnitType type, AUnit againstUnit) {
        int damageNormalized = againstUnit.isAir()
                ? WeaponUtil.damageNormalized(type.airWeapon())
                : WeaponUtil.damageNormalized(type.groundWeapon());
        return (damageNormalized * WeaponUtil.damageModifier(type, againstUnit.type())) * EVAL_DAMAGE_FACTOR;
    }

    private static double hpFactor(AUnitType type, int hp) {
        return hp * EVAL_HIT_POINTS_FACTOR;
    }

    private static double customEvaluation(AUnitType type, int hp, AUnit againstUnit) {
        if (type.is(AUnitType.Terran_Medic)) {
            return hpFactor(type, hp) * 2;
        }

        return -1.0;
    }

    private static double terranInfantryTweak(AUnit unit, double strength) {
        int medics = unit.friendsNear().ofType(AUnitType.Terran_Medic).havingEnergy(10).inRadius(3, unit).count();

        if (unit.isWounded() && medics == 0) {
            strength = strength * unit.hpPercent() / 100;
        } else {
            strength = strength * (1 + medics / 2.0);
        }

        return strength;
    }

}
