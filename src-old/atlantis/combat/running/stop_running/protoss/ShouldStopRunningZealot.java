package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

public class ShouldStopRunningZealot extends Manager {
    public ShouldStopRunningZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot()
            && unit.cooldown() <= 4
            && (unit.distToCannon() <= 1.8 || unit.distToBase() <= 5)
            && !forbidStoppingDuringEarlyGameVsProtoss();
    }

    private boolean forbidStoppingDuringEarlyGameVsProtoss() {
        if (!Enemy.protoss()) return false;

        if (unit.hp() <= 22) return true;

//        if (A.supplyUsed() >= 70) return false;

        return unit.hp() <= 40 && unit.eval() <= 1.2;
    }

    @Override
    public Manager handle() {
        if (ProtossShouldStopRunning.decisionStopRunning(unit)) {
            return usedManager(this);
        }

        return null;
    }
}

