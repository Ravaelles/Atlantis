package atlantis.units.workers.defence.fight;

import atlantis.map.position.HasPosition;

/**
 * The strayed-worker rule, one home (owner order, lost game vs early
 * aggression: workers that end up fighting far from base turn back
 * instead of donating). "Far" reads from the MAIN base, never the
 * nearest one: a natural Nexus must not legitimize a fight the army
 * cannot see (the old nearest-base read went blind the moment the
 * natural warped). The refusal reason stays TOO_FAR_FROM_BASE.
 */
public final class WorkerStrayedFromBase {

    private WorkerStrayedFromBase() {
    }

    /**
     * True when this position is further than the fight line from the
     * main base. Null fails closed (no fight), unreadable ground fails
     * open (the old shape, kept).
     */
    public static boolean tooFarFromHome(HasPosition where) {
        if (where == null) return true;

        try {
            return where.distToMain() > WorkerDefenceFightCombatUnits.MAX_FIGHT_DIST_TO_BASE_TILES;
        } catch (Exception | Error ignored) {
            return false;
        }
    }
}
