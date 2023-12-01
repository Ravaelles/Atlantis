package atlantis.terran.chokeblockers;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class ChokeBlockersCommander extends Commander {
    private AChoke choke;

    @Override
    public boolean applies() {
        return checkIfApplies();
    }

    private boolean checkIfApplies() {
        if (AGame.notNthGameFrame(3)) return false;

        choke = Chokes.mainChoke();
        if (choke == null) return false;

        if (AGame.killsLossesResourceBalance() >= 1800) return false;

        int bunkers = Count.bunkers();
        if (bunkers <= 0 || bunkers >= 3 || Count.bases() != 1) return false;

        if (Atlantis.KILLED <= 2) return true;

        return Count.ourCombatUnits() <= 18;
    }

    @Override
    protected void handle() {
        ChokeBlockers chokeBlockers = ChokeBlockers.get();

        chokeBlockers.assignWorkersIfNeeded();

        actWithWorker(chokeBlockers.worker1);
        actWithWorker(chokeBlockers.worker2);
    }

    private void actWithWorker(AUnit unit) {
        if (unit != null && unit.isAlive()) {
            (new ChokeBlockerManager(unit)).invoke();
        }
    }
}
