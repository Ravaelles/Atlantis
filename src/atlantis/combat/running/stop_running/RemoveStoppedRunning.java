package atlantis.combat.running.stop_running;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class RemoveStoppedRunning extends Manager {
    public RemoveStoppedRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isMoving()
            && unit.lastStartedRunningLessThanAgo(1);
    }

    @Override
    public Manager handle() {
        unit.runningManager().stopRunning();
        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - STOP RUNNING -");
        return usedManager(this);
    }
}

