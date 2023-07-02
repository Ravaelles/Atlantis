package atlantis.combat.running;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ShouldStopRunning {
    public static boolean shouldStopRunning(AUnit unit) {
//        System.out.println(unit.id() + " // " + unit.isRunning()
//                + " // " + AAvoidUnits.shouldNotAvoidAnyUnit(unit));
//        System.out.println(unit.isRunning() + " // " + unit.runningManager().isRunning() + " // " + unit.action().isRunning());
        if (!unit.isRunning()) {
            return decisionStopRunning(unit);
        }

        if (unit.isFlying() && unit.enemiesNearInRadius(8.2) == 0) {
            unit.setTooltipTactical("SafeEnough");
            return decisionStopRunning(unit);
        }

        if (
            unit.isAction(Actions.RUN_IN_ANY_DIRECTION)
                && unit.lastActionLessThanAgo(20)
        ) {
            unit.setTooltipTactical("InAnyDir");
            return false;
        }

        if (
            unit.hp() > 30
                && unit.lastStartedRunningMoreThanAgo(150)
                && unit.nearestEnemyDist() >= 3.5
        ) {
            unit.setTooltipTactical("RanTooLong");
            return decisionStopRunning(unit);
        }

        if (We.terran() && unit.isHealthy() && unit.lastUnderAttackLessThanAgo(30)) {
            unit.setTooltipTactical("HealthyNow");
            return decisionStopRunning(unit);
        }

        if (
            unit.noCooldown()
                && unit.lastStartedRunningMoreThanAgo(15)
                && !AvoidEnemies.shouldNotAvoidAnyUnit(unit)) {
            unit.setTooltip("StopMan", false);
            return decisionStopRunning(unit);
        }

//        if (unit.isWounded() && unit.nearestEnemyDist() >= 3) {
//            return false;
//        }

        if (
            unit.lastStoppedRunningMoreThanAgo(ARunningManager.STOP_RUNNING_IF_STOPPED_MORE_THAN_AGO)
                && unit.lastStartedRunningMoreThanAgo(ARunningManager.STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO)
                && !unit.isUnderAttack(unit.isFlying() ? 250 : 5)
                //                && AAvoidUnits.shouldNotAvoidAnyUnit(unit)
                || AvoidEnemies.shouldNotAvoidAnyUnit(unit)
        ) {
            unit.setTooltip("StopRun");
            return decisionStopRunning(unit);
        }

        return false;
    }

    private static boolean decisionStopRunning(AUnit unit) {
        if (unit.hp() <= 20 && unit.isTerranInfantry()) {
            AUnit nearestMedic = Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(30).nearestTo(unit);
            if (nearestMedic != null) {
                unit.move(nearestMedic, Actions.MOVE_HEAL, "Lazaret");
                return true;
            }
        }

        unit.runningManager().stopRunning();
        return false;
    }
}