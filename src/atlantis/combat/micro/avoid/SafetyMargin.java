package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

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
    public static double calculate(AUnit defender, AUnit attacker) {
        if (attacker == null) {
            throw new RuntimeException("Attacker is null");
        }

        double base = baseMargin(defender, attacker);

        if (attacker.isMelee()) {
            return base + defender.distTo(attacker) - SafetyMarginAgainstMelee.calculate(defender, attacker);
        }
        else {
            return base + defender.distTo(attacker) - SafetyMarginAgainstRanged.calculate(defender, attacker);
        }
    }

    // =========================================================

    private static double baseMargin(AUnit defender, AUnit attacker) {
        return (defender.isSquadScout() ? -2.7 : 0)
                + (defender.lastRetreatedAgo() <= 40 ? -3.3 : 0);
    }

    protected static double enemyWeaponRange(AUnit defender, AUnit attacker) {
//        return attacker.getWeaponRangeAgainst(defender) - (attacker.isMelee() && attacker.groundWeaponRange() < 1.5 ? 1 : 0);
        return attacker.weaponRangeAgainst(defender) + (attacker.isMelee() && attacker.groundWeaponRange() < 1.5 ? 1 : 0);
    }

    protected static double enemyMovementBonus(AUnit defender, AUnit attacker) {
//         || defender.isOtherUnitFacingThisUnit(attacker)

        if (attacker.isMoving()) {
            boolean doingWell = defender.woundPercent() < 33 && defender.lastUnderAttackMoreThanAgo((int) (30 * (5 + defender.woundPercent())));
            return defender.isTargetedBy(attacker)
                    ? (doingWell
                        ? 0.8
                        : 1.7
                    )
                    : (doingWell
                        ? -1.4
                        : 0.5
                    );
        }
        else {
//            return defender.isTargetedBy(attacker) ? 1.2 : -1.0;

            // TARGETED by enemy
            if (defender.isTargetedBy(attacker)) {
                return 1.2;
            }

            // NOT targeted by enemy
            else {
//                if (defender.isDragoon() && defender.hp() >= 40) {
//                    return -2.0;
//                }
                return -1.0;
            }
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

    protected static double ourUnitsNearBonus(AUnit defender) {
        return Select.ourRealUnits().inRadius(0.5, defender).count() / 1.5;
    }

    protected static double woundedBonus(AUnit defender, AUnit attacker) {
        if (defender.isTerranInfantry()) {
            if (Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(20).inRadius(2, defender).isNotEmpty()) {
                return 0;
            }
        }

        if (defender.isAir()) {
            return defender.woundPercent() / 10;
        }

        boolean applyExtraModifier = defender.isTank();
        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

    protected static double specialAirUnitBonus(AUnit defender) {
        return defender.type().isTransport() ? 4 : (defender.is(AUnitType.Terran_Science_Vessel) ? 3 : 0);
    }

    protected static double quicknessBonus(AUnit defender, AUnit attacker) {

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
