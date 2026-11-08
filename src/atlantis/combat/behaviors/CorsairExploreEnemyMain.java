package atlantis.combat.behaviors;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Corsair scouting (owner 2026-11-09, port of the old
 * {@code CorsairExploreEnemyMain}): a few seconds per half minute the
 * Corsair confirms the enemy main instead of idling - an unseen main
 * too long means the army fights blind.
 */
public final class CorsairExploreEnemyMain {

    private static int mainLastSeenAt = -9999;

    /**
     * Test seam: the latch is per-game state, never cross-test.
     */
    public static void clearForTests() {
        mainLastSeenAt = -9999;
    }

    private CorsairExploreEnemyMain() {
    }

    public static boolean applies(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive() || !unit.isCorsair()) return false;

            APosition enemyMain = EnemyInfo.enemyMain();
            if (enemyMain == null || !enemyMain.hasPosition()) return false;

            if (enemyMain.isPositionVisible()) {
                mainLastSeenAt = A.now();
            }

            return A.s % 30 <= 9 && tooLongNotSeenMain();
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static boolean act(AUnit unit) {
        APosition enemyMain;
        try {
            if (!applies(unit)) return false;
            enemyMain = EnemyInfo.enemyMain();
            if (enemyMain == null) return false;
        } catch (Exception | Error ignored) {
            return false;
        }

        unit.setTooltipTactical("ExploreEnemyMain");

        return unit.move(enemyMain, Actions.MOVE_ADVANCE, "ExploreEnemyMain");
    }

    private static boolean tooLongNotSeenMain() {
        try {
            return A.secondsAgo(mainLastSeenAt) >= A.whenEnemyZerg(20, 30);
        } catch (Exception | Error ignored) {
            return false;
        }
    }
}
