package atlantis.terran.chokeblockers;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.Enemy;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.List;

public class ChokeBlockersAssignments {
    private static ChokeBlockersAssignments instance = null;
    public List<AUnit> blockers = new ArrayList<>();
    protected AChoke choke;

    private ChokeBlockersAssignments(AChoke choke) {
        this.choke = choke;
    }

    public static ChokeBlockersAssignments get() {
        if (instance == null) instance = new ChokeBlockersAssignments(Chokes.mainChoke());

        return instance;
    }

    // =========================================================

    public void assignWorkersWhenNeeded() {
        if (We.protoss() && Count.ourCombatUnits() >= 4) return;
//        if (!We.terran()) return;
        if (A.everyFrameExceptNthFrame(13)) return;

        int blockersNeeded = blockersNeeded();

        for (int i = 1; i <= blockersNeeded; i++) {
            AUnit worker = FreeWorkers.get().first();
            if (worker != null) {
                blockers.add(worker);
                worker.setSpecialPosition(ChokeBlockerPosition.positionForBlocker(worker));
            }
        }
    }

    private int blockersNeeded() {
        int available = A.inRange(3, blockers.size() + Count.zealots(), 4);
        return workersNeeded() - available;
    }

    private int workersNeeded() {
        if (We.protoss()) {
            if (Enemy.zerg()) return Math.max(0, 2 - Count.zealots());
            return A.seconds() >= 60 ? 1 : 0;
        }

        if (We.terran()) return 2;

        return 0;
    }

    public void assignZealotsWhenNeeded() {
        if (!We.protoss()) return;

        Selection freeZealots = Select.ourOfType(AUnitType.Protoss_Zealot).exclude(blockers);
        if (freeZealots.isEmpty()) return;

        for (AUnit worker : blockers) {
            if (worker.isWorker()) {
                worker.setSpecialPosition(null);
                break;
            }
        }

        if (blockers.size() >= 3) return;

        AUnit zealot = freeZealots.nearestTo(choke);
        if (zealot != null) {
            addNewBlocker(zealot);
        }

        assignWorkersWhenNeeded();
    }

    private void addNewBlocker(AUnit unit) {
        blockers.add(unit);

        for (AUnit blocker : blockers) {
            blocker.setSpecialPosition(ChokeBlockerPosition.positionForBlocker(blocker));
        }
    }

    // =========================================================

//    public AUnit otherBlocker(AUnit unit) {
//        if (unit.equals(unit1)) return unit2;
//        else return unit1;
//    }
//
//    public boolean noEnemiesVeryNear() {
//        return unit1 != null && unit1.enemiesNear().inRadius(6, unit1).empty();
//    }
//
//    public static APosition chokeBlockPoint1() {
//        if (blockingPoint1 == null) defineBlockingPoints();
//
//        return blockingPoint1;
//    }
//
//    public static APosition chokeBlockPoint2() {
//        if (blockingPoint1 == null) defineBlockingPoints();
//
//        return blockingPoint1;
//    }

    public void removeDeadUnits() {
        while (true) {
            for (AUnit unit : blockers) {
                if (unit.isDead()) {
                    blockers.remove(unit);
                    break;
                }
            }
            break;
        }
    }
}
