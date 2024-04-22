package atlantis.combat.running.stop_running;

import atlantis.architecture.Manager;
import atlantis.combat.running.stop_running.protoss.ProtossShouldStopRunning;
import atlantis.combat.running.stop_running.protoss.TerranShouldStopRunning;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ShouldStopRunning extends Manager {
    public ShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRunning();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShouldStopRunning.class,
            TerranShouldStopRunning.class,
        };
    }

//    @Override
//    protected Manager handle() {
//        if (check()) {
//            unit.runningManager().stopRunning();
//            return usedManager(this);
//        }
//
//        return null;
//    }

//    public boolean check() {
////        if (unit.isActiveManager(HoldToShoot.class)) return false;
//
//        if (asProtoss()) return true;
//
////        if (dontStopRunningAsWorker()) return false;
//
////        if (!unit.isRunning()) {
////            return decisionStopRunning();
////        }
//
//        return false;
//
////        if (checkAsMelee()) return false;
////        if (ShouldStopRunningMarine.shouldNotStop(unit)) return false;
////
////        // @Replaced
////        if (unit.avoidEnemiesManager().shouldAvoidAnyUnit()) {
////            return false;
////        }
////
////        if (unit.isFlying() && unit.enemiesNearInRadius(8.5) == 0) {
////            unit.setTooltipTactical("SafeEnough");
////            unit.addLog("SafeEnough");
////            return decisionStopRunning();
////        }
////
////        if (
////            unit.isAction(Actions.RUN_IN_ANY_DIRECTION)
////                && unit.lastActionLessThanAgo(20)
////        ) {
////            unit.addLog("InAnyDir");
////            unit.setTooltipTactical("InAnyDir");
////            return false;
////        }
////
////        if (
////            unit.hp() > 30
////                && unit.lastStartedRunningMoreThanAgo(150)
////                && unit.nearestEnemyDist() >= 3.5
////        ) {
////            unit.setTooltipTactical("RanTooLong");
////            unit.addLog("RanTooLong");
////            return decisionStopRunning();
////        }
////
////        if (We.terran() && unit.isHealthy() && unit.lastUnderAttackLessThanAgo(30)) {
////            unit.setTooltipTactical("HealthyNow");
////            unit.addLog("HealthyNow");
////            return decisionStopRunning();
////        }
////
////        return false;
//    }

    private boolean dontStopRunningAsWorker() {
        return unit.isWorker()
            && unit.enemiesNear().inRadius(4, unit).havingAntiGroundWeapon().notEmpty();
    }

//    private boolean checkAsMelee() {
//        return checkAsZergling() || checkAsZealot();
//    }

    private boolean checkAsZergling() {
        return unit.isZergling()
            && unit.enemiesNear().melee().canAttack(unit, 2).empty()
            && unit.combatEvalRelative() >= 1.2;
    }

    private boolean decisionStopRunning() {
//        if (unit.hp() <= 20 && unit.isTerranInfantry() && !unit.isMedic()) {
//            AUnit nearestMedic = Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(30).nearestTo(unit);
//            if (nearestMedic != null) {
//                unit.move(nearestMedic, Actions.MOVE_HEAL, "Lazaret");
//                return true;
//            }
//        }

        System.out.println("@ " + A.now() + " - stop running, near enemy =  " + unit.nearestEnemyDist() + " / " + unit.tooltip());

        unit.runningManager().stopRunning();
        unit.stop("StopRunning");
        return false;
    }
}
