package atlantis.terran.chokeblockers;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ChokeBlockers {
    private static ChokeBlockers instance = null;

    private AChoke choke;

    public AUnit worker1 = null;
    public AUnit worker2 = null;

    private APosition blockingPoint1;
    private APosition blockingPoint2;

    private ChokeBlockers(AChoke choke) {
        this.choke = choke;

        defineBlockingPoints();
    }

    public static ChokeBlockers get() {
        if (instance == null) instance = new ChokeBlockers(Chokes.mainChoke());

        return instance;
    }

    private void defineBlockingPoints() {
        blockingPoint1 = choke.firstPoint();
        blockingPoint2 = choke.lastPoint();

        blockingPoint1 = blockingPoint1.translatePercentTowards(blockingPoint2, 26);
        blockingPoint2 = blockingPoint2.translatePercentTowards(blockingPoint1, 26);
    }

    public void assignWorkersIfNeeded() {
        if (A.everyFrameExceptNthFrame(13)) return;
//
//        if (Count.ourCombatUnits() >= 18) {
//            worker1 = null;
//            worker2 = null;
//            return;
//        }

        if (worker1 != null && worker1.isAlive() && worker2 != null && worker2.isAlive()) return;

        Selection workers = candidates();

        if (worker1 == null || !worker1.isAlive()) {
            worker1 = workers.first();
            if (worker1 != null) worker1.setSpecialPosition(blockingPoint1);
        }
        if (worker2 == null || !worker2.isAlive()) {
            worker2 = workers.second();
            if (worker2 != null) worker2.setSpecialPosition(blockingPoint2);
        }
    }

    private static Selection candidates() {
        return Select.ourWorkers()
            .gatheringMinerals(true)
            .notConstructing();
    }

    public AUnit otherBlocker(AUnit unit) {
        if (unit.equals(worker1)) return worker2;
        else return worker1;
    }
}
