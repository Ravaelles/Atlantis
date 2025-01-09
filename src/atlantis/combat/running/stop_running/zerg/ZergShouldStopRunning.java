package atlantis.combat.running.stop_running.zerg;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ZergShouldStopRunning extends Manager {
    public ZergShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.zerg();
    }

    @Override
    protected Manager handle() {
        if (handleSubmanagers() != null) {
            unit.runningManager().stopRunning();
            return usedManager(this);
        }

        return null;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ShouldStopRunningZergling.class,
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

    private boolean decisionStopRunning() {
//        System.out.println("@ " + A.now() + " - stop running, near enemy =  " + unit.nearestEnemyDist() + " / " + unit.tooltip());

        unit.runningManager().stopRunning();
//        unit.stop("StopRunning");
        return true;
    }
}
