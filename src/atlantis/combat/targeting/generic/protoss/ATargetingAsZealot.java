package atlantis.combat.targeting.generic.protoss;

import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;

public class ATargetingAsZealot {
    public static AUnit target(AUnit unit, Selection enemyUnits) {
        if (!unit.isZealot()) return null;

        AUnit target;
        double baseRange = 1.0;

        AUnit leader = unit.squadLeader();
        if (leader == null) return null;

        target = enemyUnits
            .inRadius(baseRange, unit)
            .mostWoundedOrNearest(unit);

        if (target != null) {
//            System.out.println("--- REGULAR " + target);
            return target;
        }

        target = enemyUnits
            .inRadius(2, unit)
            .mostWoundedOrNearest(unit);

        if (target != null) {
//            System.out.println("--- REGULAR " + target);
            return target;
        }

        AUnit enemyClosestToLeader = enemyUnits.inRadius(baseRange + 3, leader).mostWoundedOrNearest(leader);
        if (enemyClosestToLeader != null) {
            if (unit.distTo(enemyClosestToLeader) <= baseRange) {
//                System.out.println("--- LEADER CLOSEST " + enemyClosestToLeader);
                return enemyClosestToLeader;
            }
        }
        if (enemyClosestToLeader == null) {
            enemyClosestToLeader = enemyUnits.mostWoundedOrNearest(leader);
        }

//        System.out.println("@@@@@@@@@@@@@@@@ LEADER CLOSEST " + enemyClosestToLeader);
        return enemyClosestToLeader;
    }
}
