package atlantis.terran.chokeblockers;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ChokeBlockersCommander extends Commander {
    private AChoke choke;
    private APosition point1;
    private APosition point2;
    private AUnit worker1 = null;
    private AUnit worker2 = null;

    @Override
    public boolean applies() {
        return Count.bunkers() <= 2 && Count.bases() <= 1;
    }

    @Override
    protected void handle() {
        choke = Chokes.mainChoke();

        if (choke == null) return;

        point1 = choke.firstPoint();
        point2 = choke.lastPoint();

        assignWorkersIfNeeded();

        if (worker1 != null || worker2 != null) {
            actWithWorker(worker1, worker2, point1);
            actWithWorker(worker2, worker1, point2);
        }
    }

    private void actWithWorker(AUnit unit, AUnit otherBlocker, APosition goTo) {
        if (unit != null && unit.isAlive()) {
            (new ChokeBlockerManager(unit, otherBlocker, goTo)).invoke();
        }
    }

    private void assignWorkersIfNeeded() {
        if (A.everyFrameExceptNthFrame(13)) return;

        if (Count.ourCombatUnits() >= 18) {
            worker1 = null;
            worker2 = null;
            return;
        }

        if (worker1 != null && worker1.isAlive() && worker2 != null && worker2.isAlive()) return;

        Selection workers = Select.ourWorkers().gatheringMinerals(true);

        if (worker1 == null || !worker1.isAlive()) {
            worker1 = workers.first();
        }
        if (worker2 == null || !worker2.isAlive()) {
            worker2 = workers.second();
        }
    }
}
