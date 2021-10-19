package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class SafetyMargin {

    /**
     * Margin of defender safety against the attacker weapon range.
     * Negative value means attacker can shoot at the defender.
     * Positive value means some extra safety margin for the defender.
     *
     * Example #1:
     * Defending Dragoon is 3 tiles away from attacking Marine (RANGED unit, range 4).
     * Margin =  3 - 4 = -1  tiles
     *
     * Example #2:
     * Defending Dragoon is 5.5 tiles away from attacking Zealot (MELEE unit, range 1).
     * Margin =  5.5 - 1 = 4.5  tiles
     */
    public static double calculate(AUnit attacker, AUnit defender) {
        if (attacker == null) {
            return Double.POSITIVE_INFINITY;
        }

        if (attacker.isMeleeUnit()) {
            return SafetyMarginAgainstMelee.calculate(attacker, defender);
        }
        else {
            return SafetyMarginAgainstRanged.calculate(attacker, defender);
        }
    }

    // =========================================================

    protected static double enemyWeaponRangeBonus(AUnit defender, AUnit attacker) {
        return attacker.getWeaponRangeAgainst(defender);
    }

    protected static double enemyMovementBonus(AUnit attacker, AUnit defender) {
        return defender.isOtherUnitFacingThisUnit(attacker) ? (attacker.isMoving() ? 1.8 : 0.9) : 0;
    }

    protected static double ourMovementBonus(AUnit defender) {
        return defender.isMoving() ? (defender.isRunning() ? 0.8 : 0) : 1.3;
    }

    protected static double ourUnitsNearbyBonus(AUnit defender) {
        return Select.ourRealUnits().inRadius(0.6, defender).count() / 2.0;
    }

    protected static double woundedBonus(AUnit defender) {
        return defender.getWoundPercent() / 34.0;
    }

    protected static double quicknessBonus(AUnit attacker, AUnit defender) {

        // If unit is much slower than enemy, don't run at all. It's better to shoot instead.
        double quicknessDifference = defender.getSpeed() - attacker.getSpeed();

        return Math.min(0, (quicknessDifference > 0 ? -quicknessDifference / 3 : quicknessDifference / 1.5));
    }

}
