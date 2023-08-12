package atlantis.combat.micro.avoid.margin;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class TerranSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {

    public TerranSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    protected double handleTerranInfantry(AUnit attacker) {
        if (canIgnoreThisEnemyForNow(attacker)) {
            return 2;
        }

        // =========================================================

        double criticalDist = enemyFacingThisUnitBonus(attacker);

//        if (true) return 3;

        // === MEDIC in range ===========================================

        boolean medicInRange = defender.hasMedicInRange();
        if (medicInRange) {
            if (defender.isHealthy() && defender.enemiesNearInRadius(3) <= 1) {
                defender.setTooltipTactical("Healthy");
                return enemyUnitsNearBonus(defender);
            }

            criticalDist += SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC
//                    + ourMovementBonus(defender)
//                    + enemyMovementBonus(attacker)
                + woundedAgainstMeleeBonus(attacker);

            defender.setTooltipTactical("HasMedic");
        }

        // === No medic Near ===========================================

        else {
            criticalDist += SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC
                + ourMovementBonus(defender)
                + (1.2 * enemyMovementBonus(attacker))
//                    + SafetyMargin.enemyMovementBonus(attacker)
//                    + (defender.hasCooldown() ? enemyMovementBonus(attacker) : 0)
                + workerBonus(attacker)
                + Math.min(1.2, woundedAgainstMeleeBonus(attacker));

//            criticalDist = Math.min(criticalDist, defender.isWounded() ? 2.9 : 2.8);

            String log = "NoMedic" + A.digit(criticalDist);
            defender.setTooltipTactical(log);
//            defender.addLog(log);
        }

        criticalDist += enemyUnitsNearBonus(defender);
        criticalDist += cooldownBonus(defender);

        if (medicInRange) {
            criticalDist = Math.min(criticalDist, 2.1);
        }
//        else {
//            criticalDist = Math.min(criticalDist, 3.0);
//        }

//        defender.addTooltip(A.digit(criticalDist));

        if (defender.hp() <= (Enemy.zerg() ? 9 : 17)) {
            criticalDist += 1.5;
        }

        if (attacker.groundWeaponRange() <= 1) {
            criticalDist = Math.min(3.6, criticalDist);
        }

        return criticalDist;
    }

    private double cooldownBonus(AUnit defender) {
        int cooldown = defender.cooldown();
        if (cooldown >= 4) {
            return cooldown / 6.0;
        }

        return 0;
    }

    protected double ourMovementBonus(AUnit defender) {
        return defender.isMoving() ? -0.2 : +0.3;
    }

    private boolean canIgnoreThisEnemyForNow(AUnit attacker) {
        if (attacker.isRanged()) return false;

        double distTo = defender.distTo(attacker);
        if (distTo >= 4) return true;

        if (distTo >= 2 && !defender.isOtherUnitFacingThisUnit(attacker)) return true;

        return false;
    }
}
