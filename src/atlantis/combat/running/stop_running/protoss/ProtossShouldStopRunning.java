package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossShouldStopRunning extends Manager {

    public ProtossShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isRunning();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossShouldStopRunningMelee.class,
            ShouldStopRunningDragoon.class,
            ShouldStopRunningProtossAir.class,
            ShouldStopRunningProbe.class,
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
            && unit.combatEvalRelative() >= 1.2;
    }

    private boolean checkAsZealot() {
        return unit.isZealot() && unit.combatEvalRelative() >= 1.2;
    }

    public static boolean decisionStopRunning(AUnit unit) {
//        System.out.println("@ " + A.now() + " - stop running, near enemy =  " + unit.nearestEnemyDist() + " / " + unit.tooltip());

        unit.runningManager().stopRunning();
//        unit.stop("StopRunning");
        return true;
    }
}
