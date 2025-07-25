package atlantis.combat.targeting.generic.protoss;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;

import static atlantis.combat.targeting.generic.ATargeting.debug;

public class ATargetingAsDragoon {
    public static AUnit target(AUnit unit, Selection enemyUnits) {
        if (!unit.isDragoon()) return null;

        AUnit target;
        double range = OurDragoonRange.range();

        if (Enemy.zerg() && unit.isDragoon()) {
            Selection rangedEnemies = enemyUnits.ranged();

            target = rangedEnemies
                .inRadius(range + 1, unit)
                .notHavingHp(25)
//                .notShowingBackToUs(unit)
                .nearestTo(unit);
            if (target != null) {
                debug("ClosestZergA = " + target.typeWithUnitId());
                return target;
            }

            target = rangedEnemies
                .inRadius(range, unit)
                .notShowingBackToUs(unit)
                .mostWounded();
            if (target != null && target.isWounded()) {
                debug("ClosestZergB = " + target.typeWithUnitId() + "(" + target.hp() + ") out of " + enemyUnits.size());
                return target;
            }

            target = rangedEnemies
                .inRadius(range, unit)
                .mostWoundedOrNearest(unit);
            if (target != null) {
                if (!unit.isOtherUnitShowingBackToUs(target) || unit.distTo(target) <= range - 0.4) {
                    debug("ClosestZergC = " + target.typeWithUnitId() + "(" + target.hp() + ") out of " + enemyUnits.size());
                    return target;
                }
            }

            target = rangedEnemies
                .inRadius(range, unit)
                .nearestTo(unit);
            if (target != null) {
                debug("ClosestZergD = " + target.typeWithUnitId());
                return target;
            }
        }

//        if (unit.shotSecondsAgo() >= 2) {
        AUnit leader = unit.squadLeader();
        if (leader == null) return null;

        AUnit enemyClosestToLeader = enemyUnits.inRadius(range + 2, leader).mostWoundedOrNearest(leader);
        if (enemyClosestToLeader != null) {
            if (unit.distTo(enemyClosestToLeader) <= range) {
//                System.out.println("--- LEADER CLOSEST " + enemyClosestToLeader);
                debug("GoonLeaderClosest = " + enemyClosestToLeader);
                return enemyClosestToLeader;
            }
        }
//        }

        if (Enemy.protoss()) {
            if (unit.enemiesNearCount(2.4) == 0) {
                target = enemyUnits
                    .dragoons()
                    .inRadius(range, unit)
                    .mostWoundedOrNearest(unit);

                if (target != null) {
                    //            System.out.println("--- REGULAR " + target);
                    debug("GoonGoonz = " + target);
                    return target;
                }
            }
        }

        if (enemyClosestToLeader == null) {
            enemyClosestToLeader = enemyUnits.mostWoundedOrNearest(leader);
            if (enemyClosestToLeader != null && unit.distTo(enemyClosestToLeader) <= 9) {
                debug("GoonEnemyClosestToLeader = " + enemyClosestToLeader);
                return enemyClosestToLeader;
            }
        }

//        System.out.println("@@@@@@@@@@@@@@@@ LEADER CLOSEST " + enemyClosestToLeader);

        return null;
    }
}
