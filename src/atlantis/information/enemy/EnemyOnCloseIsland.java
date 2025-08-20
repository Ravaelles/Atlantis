package atlantis.information.enemy;

import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;

public class EnemyOnCloseIsland {
    private static AUnit enemyOnIsland = null;
    private static HasPosition enemyOnIslandPosition = null;
//    private static int lastSeenAt = -9992;

    public static HasPosition get() {
        if (shouldNullify()) {
            enemyOnIsland = null;
            enemyOnIslandPosition = null;
        }

        return enemyOnIslandPosition;
    }

    private static boolean shouldNullify() {
        if (enemyOnIsland != null && (enemyOnIsland.isDead() || enemyOnIsland.lastSeenAgo() >= 30 * 15)) return true;

        return false;

//        if (
//            enemyOnIsland != null && (
//                enemyOnIsland.isDead() || enemyOnIsland.lastSeenAgo() >= 30 * 15
////                !enemyOnIsland.hasPosition() || enemyOnIsland.isDead() || enemyOnIsland.lastSeenAgo() >= 30 * 15
//            )
//        ) {
////            System.err.println("@@@@@@@@@@@@@@@@@@ NULLIFY enemyOnIsland = " + enemyOnIsland
////                + " / hasPosition:" + enemyOnIsland.hasPosition()
////                + " / visible:" + enemyOnIsland.isVisibleUnitOnMap()
////                + " / dead:" + enemyOnIsland.isDead()
////                + " / exists:" + enemyOnIsland.exists()
////            );
//            return enemyOnIsland = null;
//        }
    }

    public static boolean potentialEnemyOnIsland(AUnit enemy) {
        if (!enemy.isCombatUnit()) return false;
        if (!enemy.isRanged() && !enemy.isDangerousLandCaster()) return false;

        if (enemyIsOnIsland(enemy) && BaseLocations.mainNaturalAndExistingBases().countInRadius(25, enemy) > 0) {
            enemyOnIsland = enemy;
            enemyOnIslandPosition = enemy.position();
            return true;
        }

        return false;
    }

    private static boolean enemyIsOnIsland(AUnit enemy) {
        if (enemy.isAir()) return false;

        ARegion region = enemy.position().region();
        if (region == null) return false;

        return region.isIsland();
    }
}
