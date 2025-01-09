package atlantis.combat.squad.positioning.choking;

import atlantis.combat.squad.Squad;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

/**
 * Choking is a custom term for positioning behind a choke and waiting for the enemy to engage,
 * luring them into a bad position.
 */
public class ShouldDoChoking {
    public static final int MIN_DIST_TO_CHOKE = 6;
    private static AUnit leader;
    private static Selection enemies;
    private static AUnit nearestEnemy;
    private static double distToChoke;

    public static boolean check(AUnit unit) {
        nearestEnemy = null;

        leader = unit.squadLeader();
        if (leader == null) return false;

        enemies = leader.enemiesNear().combatUnits();
        if (enemies.atMost(1)) return false;

        nearestEnemy = enemies.nearestTo(leader);
        if (nearestEnemy == null) return false;

        Squad squad = leader.squad();
        if (squad != null) {
            if (squad.lastUnderAttackLessThanAgo(50)) return false;
        }

        distToChoke = leader.distToNearestChokeCenter();
        if (distToChoke >= MIN_DIST_TO_CHOKE + 2) return false;

        if (enemies.canBeAttackedBy(leader, 1).notEmpty()) return false;

        return leader.eval() <= 9;
    }

    public static HasPosition lastEnemyPosition() {
        return nearestEnemy != null ? nearestEnemy.position() : null;
    }
}
