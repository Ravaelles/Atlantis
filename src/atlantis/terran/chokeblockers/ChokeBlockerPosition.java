package atlantis.terran.chokeblockers;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
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

        if (!We.terran() && unit.isWorker()) position = position.translateTilesTowards(choke.center(), -0.8);

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
            if (indexOf == 0) return 20;
            return 80;
        }

        if (totalBlockers == 3) {
            if (indexOf == 0) return 17;
            if (indexOf == 1) return 50;
            if (indexOf == 2) return 83;
        }

        return 50;
    }
}
