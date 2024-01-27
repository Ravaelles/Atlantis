package atlantis.terran.chokeblockers;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.Vector;
import atlantis.util.We;

public class ChokeBlockerPosition {
    public static APosition positionForBlocker(AUnit unit) {
        ChokeBlockersAssignments chokeBlockersAssignments = ChokeBlockersAssignments.get();
        AChoke choke = chokeBlockersAssignments.choke;
        int indexOf = indexOfUnit(unit, chokeBlockersAssignments);
        int totalBlockers = chokeBlockersAssignments.blockers.size();

        APosition first = choke.firstPoint();
        APosition last = choke.lastPoint();

        APosition position = first.translatePercentTowards(last, offset(indexOf, totalBlockers));

//        if (!We.terran() && unit.isWorker()) position = position.translateTilesTowards(choke.center(), -0.8);
//        double translate = We.protoss() ? -1.5 : -0.8;
//        double translate = -0.4;

        Vector translationVector = NeedChokeBlockers.translationVectorInRelationToChoke;
        if (translationVector != null) {
//            System.err.println("APPLY VECTOR before = " + position.toStringPixels());
            position = position.translateByVector(translationVector);
//            System.err.println("APPLY VECTOR after = " + position.toStringPixels());
        }

        return position;
    }

    private static int indexOfUnit(AUnit unit, ChokeBlockersAssignments assignments) {
        if (assignments.blockers.contains(unit)) {
            return assignments.blockers.indexOf(unit);
        }

        return unit.getUnitIndexInBwapi() % 3;
    }

    private static int offset(int indexOf, int totalBlockers) {
        if (totalBlockers == 1) return 50;

        if (totalBlockers == 2) {
            int offset = 17;
            if (indexOf == 0) return offset;
            return 100 - offset;
        }

        if (totalBlockers == 3) {
            int offset = 2;
            if (indexOf == 0) return offset;
            if (indexOf == 1) return 50;
            if (indexOf == 2) return 100 - offset;
        }

        indexOf = (totalBlockers + indexOf) % indexOf;
        int offset = 25;
        if (indexOf == 0) return 1;
        if (indexOf == 1) return offset;
        if (indexOf == 2) return 100 - offset;
        if (indexOf == 3) return 99;
        return 50;
    }
}
