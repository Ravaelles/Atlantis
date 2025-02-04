package atlantis.combat.micro.avoid.margin;

import atlantis.combat.micro.avoid.margin.special.SafetyMarginAgainstSpecial;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

public class SafetyMargin {

    protected AUnit defender;

    public SafetyMargin(AUnit defender) {
        this.defender = defender;
    }

    /**
     * Margin of defender safety against the attacker weapon range.
     * Negative value means attacker can shoot at the defender.
     * Positive value means some extra safety margin for the defender.
     * <p>
     * Example #1:
     * Defending Dragoon is 3 tiles away from attacking Marine (RANGED unit, range 4).
     * Margin =  3 - 4 = -1  tiles
     * <p>
     * Example #2:
     * Defending Dragoon is 5.5 tiles away from attacking Zealot (MELEE unit, range 1).
     * Margin =  5.5 - 1 = 4.5  tiles
     */
    public double marginAgainst(AUnit attacker) {
        if (attacker == null) {
            throw new RuntimeException("Attacker is null");
        }

        double base = BaseSafetyMargin.baseSafetyDistance(defender, attacker);
        double calculated;

        if ((calculated = (new SafetyMarginAgainstSpecial(defender)).handle(attacker)) != -1) {
            // Do nothing
        }
        else if (attacker.isMelee()) {
            calculated = (new SafetyMarginAgainstMelee(defender)).marginAgainst(attacker);
        }
        else {
            calculated = (new SafetyMarginAgainstRanged(defender)).marginAgainst(attacker);
        }

        double safetyMargin = base + defender.distTo(attacker) - calculated;

//        Color color = safetyMargin < 0 ? Color.Red : Color.Green;
//        defender.paintLine(attacker, color);
//        APainter.paintTextCentered(defender, A.digit(calculated), color, 0, 0.6);

        return safetyMargin;
    }

    // =========================================================

    protected double enemyWeaponRange(AUnit attacker) {
        return defender.enemyWeaponRangeAgainstThisUnit(attacker)
            + (attacker.isMelee() && attacker.groundWeaponRange() < 1.5 ? 1 : 0);
    }

    protected double enemyMovementBonus(AUnit attacker) {
        if (attacker == null || attacker.hp() <= 0) return 0;

//        System.err.println("TargetedBy= " + defender.isTargetedBy(attacker) + " / AttMoving= " + attacker.isMoving());
        if (attacker.isMoving() || attacker.isAttacking()) {
            return 0.6;
//            boolean doingWell = defender.woundPercent() < 33 && defender.lastUnderAttackMoreThanAgo((int) (30 * (5 + defender.woundPercent())));
//            return defender.isTargetedBy(attacker) ? 1.3 : -0.2;
//            return defender.isTargetedBy(attacker)
//                ? (doingWell
//                ? 0.8
//                : 1.7
//            )
//                : (doingWell
//                ? -1.4
//                : 0.5
//            );
        }
        else {
            return 0;

//            return defender.isTargetedBy(attacker) ? 1.2 : -1.0;

//            // TARGETED by enemy
//            if (defender.isTargetedBy(attacker)) {
//                return 0.5;
//            }
//
//            // NOT targeted by enemy
//            else {
////                if (defender.isDragoon() && defender.hp() >= 40) {
////                    return -2.0;
////                }
//                return -0.2;
//            }
        }

//        if (attacker.isMoving()) {
//            return defender.isTargettedBy(attacker) ? 1.0 : -0.9;
//        }
//        else {
//            return defender.isTargettedBy(attacker) ? 0.6 : -1.3;
//        }
    }

    protected double ourNotMovingPenalty(AUnit defender) {
        return defender.isMoving() ? (defender.isRunning() ? -0.1 : 0) : 1.2;
    }

    protected double ourUnitsNearBonus(AUnit defender) {
        return Select.ourRealUnits().inRadius(0.5, defender).count() / 1.5;
    }

    protected double woundedBonus(AUnit attacker) {
        if (defender.isTerranInfantry()) {
            if (Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(20).inRadius(2, defender).isNotEmpty()) {
                return 0;
            }

            if (defender.isWounded() && attacker.isDragoon()) {
                return defender.woundPercent() / 30;
            }
        }

        if (defender.isAir()) {
            return defender.woundPercent() / 10;
        }

        boolean applyExtraModifier = defender.isTank();
        return (defender.woundPercent() * (applyExtraModifier ? 2 : 1)) / 32.0;
    }

    protected double specialAirUnitBonus(AUnit defender) {
        return defender.type().isTransport() ? 4 : (defender.is(AUnitType.Terran_Science_Vessel) ? 3 : 0);
    }

    protected double quicknessBonus(AUnit attacker) {

        // If unit is much slower than enemy, don't run at all. It's better to shoot instead.
        double quicknessDifference = defender.maxSpeed() - attacker.maxSpeed();

        return -quicknessDifference / (quicknessDifference > 0 ? 2.5 : (attacker.isMelee() ? 0.6 : 1.5));
//        return Math.min(0, (quicknessDifference > 0 ? -quicknessDifference / 3 : quicknessDifference / 1.5));
    }

    protected double asWorkerBonus(AUnit attacker) {
        if (defender.isWorker()) {
            double base = 2.9 + defender.woundPercent() / 44.0;

            return base + (defender.isBuilder() ? 2.2 : 0) + (Enemy.zerg() ? 1.4 : 0);
        }

        return 0;
    }

}
