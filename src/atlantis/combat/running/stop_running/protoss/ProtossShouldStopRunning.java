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
    protected Manager handle() {
        if (check()) {
            unit.runningManager().stopRunning();
            return usedManager(this);
        }

        return null;
    }

    public boolean check() {
        if (unit.combatEvalRelative() >= 2) return true;

        if (asAirUnit()) {
            unit.setTooltipTactical("SafeEnough");
            unit.addLog("SafeEnough");
            return decisionStopRunning();
        }

        return ShouldStopRunningDragoon.shouldStopRunning(unit);
    }

    private boolean asAirUnit() {
        return unit.isAir()
            && unit.enemiesNear().groundUnits().combatUnits().countInRadius(8.5 + unit.woundPercent() / 30.0, unit) == 0;
    }

    private boolean dontStopRunningAsWorker() {
        return unit.isWorker()
            && unit.enemiesNear().inRadius(4, unit).havingAntiGroundWeapon().notEmpty();
    }

    private boolean checkAsMelee() {
        return checkAsZergling() || checkAsZealot();
    }

    private boolean checkAsZergling() {
        return unit.isZergling()
            && unit.enemiesNear().melee().canAttack(unit, 2).empty()
            && unit.combatEvalRelative() >= 1.2;
    }

    private boolean checkAsZealot() {
        return unit.isZealot() && unit.combatEvalRelative() >= 1.2;
    }

    private boolean decisionStopRunning() {
//        System.out.println("@ " + A.now() + " - stop running, near enemy =  " + unit.nearestEnemyDist() + " / " + unit.tooltip());

        unit.runningManager().stopRunning();
//        unit.stop("StopRunning");
        return true;
    }
}
