package atlantis.terran.chokeblockers;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;

import java.util.List;

public class ChokeBlockersAssignments {
    private static ChokeBlockersAssignments instance = null;

    private static AChoke choke;

    public AUnit unit1 = null;
    public AUnit unit2 = null;

    private static APosition blockingPoint1;
    private static APosition blockingPoint2;

    private ChokeBlockersAssignments(AChoke choke) {
        this.choke = choke;

        defineBlockingPoints();
    }

    public static ChokeBlockersAssignments get() {
        if (instance == null) instance = new ChokeBlockersAssignments(Chokes.mainChoke());

        return instance;
    }

    private static void defineBlockingPoints() {
        if (choke == null) return;

        blockingPoint1 = choke.firstPoint();
        blockingPoint2 = choke.lastPoint();

        blockingPoint1 = choke.firstPoint().translatePercentTowards(choke.lastPoint(), 20);
        blockingPoint2 = choke.lastPoint().translatePercentTowards(choke.firstPoint(), 20);
    }

    // =========================================================

    public void assignWorkersWhenNeeded() {
        if (!We.terran()) return;
        if (A.everyFrameExceptNthFrame(13)) return;

        boolean unit1IsOk = unit1IsOk();
        boolean unit2IsOk = unit2IsOk();
        if (unit1IsOk && unit2IsOk) return;

        Selection workers = FreeWorkers.get();

        if (!unit1IsOk) {
            unit1 = workers.first();
            if (unit1 != null) unit1.setSpecialPosition(blockingPoint1);
        }

        if (We.protoss()) return;

        if (!unit2IsOk) {
            unit2 = workers.second();
            if (unit2 != null) unit2.setSpecialPosition(blockingPoint2);
        }
    }

    private boolean unit1IsOk() {
        return unit1 != null && unit1.isAlive();
    }

    private boolean unit2IsOk() {
        return unit2 != null && unit2.isAlive() && (!We.protoss() || unit2.isZealot());
    }

    public void assignZealotsWhenNeeded() {
        if (!We.protoss()) return;
        boolean unit1IsOk = unit1IsOk();
        boolean unit2IsOk = unit2IsOk();
        if (unit1IsOk && unit2IsOk) return;

        List<AUnit> zealots = Select.ourOfType(AUnitType.Protoss_Zealot).exclude(unit1).exclude(unit2).list();
        for (AUnit zealot : zealots) {
            if (!unit2IsOk) {
                if (unit2 != null) unit2.setSpecialPosition(null);

                unit2 = zealot;
                unit2.setSpecialPosition(blockingPoint2);
                break;
            }
            if (!unit1IsOk) {
                unit1 = zealot;
                unit1.setSpecialPosition(blockingPoint1);
                break;
            }
        }
    }

    // =========================================================

    public AUnit otherBlocker(AUnit unit) {
        if (unit.equals(unit1)) return unit2;
        else return unit1;
    }

    public boolean noEnemiesVeryNear() {
        return unit1 != null && unit1.enemiesNear().inRadius(6, unit1).empty();
    }

    public static APosition chokeBlockPoint1() {
        if (blockingPoint1 == null) defineBlockingPoints();

        return blockingPoint1;
    }

    public static APosition chokeBlockPoint2() {
        if (blockingPoint1 == null) defineBlockingPoints();

        return blockingPoint1;
    }
}
