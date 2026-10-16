package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossShouldStopRunning extends Manager {

    public ProtossShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && (unit.isRunning() || unit.isRetreating() || unit.isAction(Actions.MOVE_SAFETY))
//            && (unit.isRunning() || unit.isRetreating() || unit.isAction(Actions.MOVE_SAFETY))
            && !unit.isActionDance();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ShouldStopRunningDragoon::new,
            ShouldStopRunningZealot::new,
            ShouldStopRunningProtossAir::new,
            ShouldStopRunningProbe::new,
            ProtossShouldStopRetreat::new,
            ProtossShouldStopRunningMelee::new,
        };
    }

//    public boolean check() {
//        if (unit.combatEvalRelative() >= 2) return true;
//
//        if (asAirUnit()) {
//            unit.setTooltipTactical("SafeEnough");
//            unit.addLog("SafeEnough");
//            return decisionStopRunning();
//        }
//
//        return (new ShouldStopRunningDragoon(unit)).invoked(this);
//    }

    private boolean checkAsZergling() {
        return unit.isZergling()
            && unit.enemiesNear().melee().canAttack(unit, 2).empty()
            && unit.eval() >= 1.2;
    }

    private boolean checkAsZealot() {
        return unit.isZealot() && unit.eval() >= 1.2;
    }

    public static boolean decisionStopRunning(AUnit unit) {
//        System.out.println("@ " + A.now() + " - stop running, near enemy =  " + unit.nearestEnemyDist() + " / " + unit.tooltip());

        unit.runningManager().stopRunning();
//        unit.stop("StopRunning");
        return true;
    }
}
