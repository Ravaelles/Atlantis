package atlantis.combat.targeting.generic.protoss;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;

import static atlantis.combat.targeting.generic.ATargeting.debug;

public class ATargetingAsDragoon {
    public static AUnit target(AUnit unit, Selection enemyUnits) {
        if (!unit.isDragoon()) return null;

        if (Enemy.zerg() && unit.isDragoon()) {
            AUnit target = enemyUnits
                .inRadius(3.75, unit)
                .mostWoundedOrNearest(unit);
            if (target != null) {
                debug("ClosestZergA = " + target);
                return target;
            }

            target = enemyUnits
                .inRadius(OurDragoonRange.range(), unit)
                .nearestTo(unit);
            if (target != null) {
                debug("ClosestZergB = " + target);
                return target;
            }
        }

        AUnit target;
        double baseRange = OurDragoonRange.range();

//        if (unit.shotSecondsAgo() >= 2) {
        AUnit leader = unit.squadLeader();
        if (leader == null) return null;

        AUnit enemyClosestToLeader = enemyUnits.inRadius(baseRange + 2, leader).mostWoundedOrNearest(leader);
        if (enemyClosestToLeader != null) {
            if (unit.distTo(enemyClosestToLeader) <= baseRange) {
//                System.out.println("--- LEADER CLOSEST " + enemyClosestToLeader);
                return enemyClosestToLeader;
            }
        }
//        }

        if (unit.meleeEnemiesNearCount(2.1) == 0) {
            target = enemyUnits
                .dragoons()
                .inRadius(baseRange, unit)
                .mostWoundedOrNearest(unit);

            if (target != null) {
                //            System.out.println("--- REGULAR " + target);
                return target;
            }
        }

        if (enemyClosestToLeader == null) {
            enemyClosestToLeader = enemyUnits.mostWoundedOrNearest(leader);
        }

//        System.out.println("@@@@@@@@@@@@@@@@ LEADER CLOSEST " + enemyClosestToLeader);
        return enemyClosestToLeader;
    }
}
