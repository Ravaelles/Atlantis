package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Count;

public class TerranFightInsteadAvoid extends HasUnit {

    public TerranFightInsteadAvoid(AUnit unit) {
        super(unit);
    }

    public boolean fightForTerran() {
        if (!unit.isTerran()) {
            return false;
        }

        return fightForTerranInfantry() || fightForAirUnit();
    }

    private boolean fightForAirUnit() {
        if (!unit.isAir()) return false;

        if (unit.isMissionDefend() && unit.hp() >= 26 && unit.cooldown() <= 4) {
            unit.setTooltip("ImportantAA");
            return true;
        }

        return false;
    }

    protected boolean fightForTerranInfantry() {
        if (!unit.isMarine() && !unit.isGhost() && !unit.isFirebat()) {
            return false;
        }

        if (unit.isAttacking() && Count.tanks() >= 2) {
            double tankDist = unit.nearestFriendlyTankDist();

            if (tankDist <= 2) return true;
            if (tankDist >= 4) return false;
        }

        double combatEval = unit.combatEvalRelative();

        if (unit.friendsNear().combatUnits().count() < unit.enemiesNear().hydralisks().count()) {
            unit.setTooltipTactical("TooManyHydras");
            return false;
        }

        if (
            unit.isMissionDefend()
                && unit.lastStartedAttackLessThanAgo(40)
                && unit.meleeEnemiesNearCount(2.4) > 0
        ) {
            return false;
        }

        if (
            unit.hp() >= 38
                && unit.cooldownRemaining() <= 2
                && (unit.enemiesNearInRadius(2.4) <= 1 || unit.lastAttackFrameMoreThanAgo(30 * 3))
                && (
                unit.noCooldown()
//                || (unit.friendsInRadiusCount(1.5) >= 5 || unit.enemiesNearInRadius(2) == 0)
                    || unit.friendsInRadiusCount(1.5) >= 5
            )
                && unit.friendsInRadius(1.6).medics().free().isNotEmpty()
                && (unit.isStimmed() || unit.friendsInRadius(3).atLeast(5))

//                unit.hp() >= 40
//                && unit.cooldownRemaining() <= 2
//                && (unit.enemiesNearInRadius(2) == 0 || unit.lastAttackFrameMoreThanAgo(30 * 3))
//                && (unit.isHealthy() || unit.enemiesNearInRadius(2.4) <= 1)
//                && unit.friendsInRadius(2.5).medics().free().isNotEmpty()
//                && (unit.isStimmed() || unit.friendsInRadius(3).atLeast(5))
        ) {
            unit.setTooltipTactical("SafeWithMedics");
            return true;
        }

        if (
            unit.hp() >= 20
                && unit.cooldown() <= 3
                && unit.friendsInRadius(2).medics().isNotEmpty()
                && unit.enemiesNear().melee().inRadius(3.8, unit).isEmpty()
        ) {
            unit.setTooltipTactical("BeBrave");
            return true;
        }

        if (unit.hp() >= 12 && combatEval > 0.5 && unit.enemiesNear().mutalisks().notEmpty()) {
            unit.setTooltipTactical("Mutas");
            return true;
        }

        if (combatEval < 0.7) {
            return false;
        }

        return false;
    }
}
