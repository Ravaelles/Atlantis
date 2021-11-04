package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

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
            throw new RuntimeException("Attacker is null");
        }

        double base = defender.isOur() && defender.isSquadScout() ? 2 : 0;

        if (attacker.isMelee()) {
            return base + SafetyMarginAgainstMelee.calculate(attacker, defender);
        }
        else {
            return base + SafetyMarginAgainstRanged.calculate(attacker, defender);
        }
    }

    // =========================================================

    protected static double enemyWeaponRangeBonus(AUnit defender, AUnit attacker) {
        return attacker.getWeaponRangeAgainst(defender) - (attacker.isMelee() && attacker.groundWeaponRange() < 1.5 ? 1 : 0);
    }

    protected static double enemyMovementBonus(AUnit attacker, AUnit defender) {
//         || defender.isOtherUnitFacingThisUnit(attacker)

        if (attacker.isMoving()) {
            boolean doingWell = defender.woundPercent() < 33 && defender.lastUnderAttackMoreThanAgo((int) (30 * (5 + defender.woundPercent())));
            return defender.isTargettedBy(attacker) ? (doingWell ? 0.5 : 1.0) : (doingWell ? -1.4 : -0.9);
        }
        else {
            return defender.isTargettedBy(attacker) ? 0.6 : -1.3;
        }

//        if (attacker.isMoving()) {
//            return defender.isTargettedBy(attacker) ? 1.0 : -0.9;
//        }
//        else {
//            return defender.isTargettedBy(attacker) ? 0.6 : -1.3;
//        }
    }

    protected static double ourMovementBonus(AUnit defender) {
        return defender.isMoving() ? (defender.isRunning() ? -1.8 : 0) : 0.8;
    }

    protected static double ourUnitsNearbyBonus(AUnit defender) {
        return Select.ourRealUnits().inRadius(0.5, defender).count() / 1.5;
    }

    protected static double woundedBonus(AUnit defender) {
        return defender.woundPercent() / 32.0;
    }

    protected static double transportBonus(AUnit defender) {
        return defender.type().isTransport() ? 4 : 0;
    }

    protected static double quicknessBonus(AUnit attacker, AUnit defender) {

        // If unit is much slower than enemy, don't run at all. It's better to shoot instead.
        double quicknessDifference = defender.maxSpeed() - attacker.maxSpeed();

        return -quicknessDifference / (quicknessDifference > 0 ? 2.5 : (attacker.isMelee() ? 0.6 : 1.5));
//        return Math.min(0, (quicknessDifference > 0 ? -quicknessDifference / 3 : quicknessDifference / 1.5));
    }

    protected static double workerBonus(AUnit defender, AUnit attacker) {
        if (defender.isWorker()) {
            return 1.5;
        }

        return 0;
    }

}
