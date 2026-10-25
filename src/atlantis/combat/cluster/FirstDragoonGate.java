package atlantis.combat.cluster;

import atlantis.game.player.Enemy;
import atlantis.units.select.Count;

/**
 * First-Dragoon gate (owner order 2026-10-26, vs Zerg on a
 * Dragoon-based build): the line defending home does not leave the main
 * base before the first Dragoon is finished - it stays inside
 * worker-help range exactly as it does while we still have only a few
 * Zealots ({@code ClusterRally}'s early-Zerg hold). A dragoon build's
 * Zealot phase is precisely when the march used to step out to the
 * halfway standoff: the ranged unit the line was waiting for did not
 * exist yet, and the base was uncovered while the miners were exposed.
 *
 * <p>The build's intent is read ONCE, from the active build order, by
 * the game root at game start ({@code OnGameStarted}) and handed here
 * as a flag: combat never reads production code (arch boundary). A
 * build that never plans a Dragoon is never gated - a Zealot build must
 * still stage out - and one finished Dragoon opens the gate for good.</p>
 */
public final class FirstDragoonGate {

    /** Whether the active build order trains Dragoons (game root's answer). */
    private static boolean buildPlansDragoons = false;

    private FirstDragoonGate() {
    }

    /** Game root, at game start: the active build order's intent. */
    public static void setBuildPlansDragoons(boolean value) {
        buildPlansDragoons = value;
    }

    /** Per-game reset next to the other statics (BattleCommander). */
    public static void clearForTests() {
        buildPlansDragoons = false;
    }

    /** The build intent the game root wrote - read as a seam, no orders. */
    public static boolean buildPlansDragoons() {
        return buildPlansDragoons;
    }

    /**
     * Live answer: hold the line at the main this frame. Null-safe on
     * the world - an unreadable game answers "march as before".
     */
    public static boolean holdsAtMain() {
        try {
            return holds(Enemy.zerg(), Count.dragoons(), buildPlansDragoons);
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * The owner's condition on booleans alone: gate only vs Zerg, only
     * while no Dragoon is finished, and only for a build that trains
     * Dragoons at all.
     */
    public static boolean holds(boolean zergEnemy, int finishedDragoons, boolean dragoonBuild) {
        return zergEnemy && finishedDragoons == 0 && dragoonBuild;
    }
}
