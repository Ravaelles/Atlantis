package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossShouldStopRetreat extends Manager {
    public ProtossShouldStopRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isRetreating()) return false;
//        if (unit.lastStartedRunningLessThanAgo(20)) return false;

        return noEnemiesNear()
            || unit.eval() >= 1.3
            || (unit.cooldown() <= 7 && (unit.distToCannon() <= 1.9 || unit.distToBase() <= 5));
    }

    private boolean noEnemiesNear() {
        return unit.enemiesNear().combatUnits().canAttack(unit, 7).empty();
    }

    @Override
    public Manager handle() {
        unit.runningManager().stopRunning();
        if (unit.isMoving() && unit.isAction(Actions.RUN_RETREAT)) {
            unit.stop("StopRetreat");
        }

        return null;
    }
}
