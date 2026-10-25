package atlantis.map.scout;

import atlantis.game.A;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * The scout watch, witnessed (NEXT #187): no SCOUT-family key existed
 * in the twin vocabulary, so even a perfect game left zero rows. This
 * samples every scout's tile next to the known enemy main on the
 * cluster cadence - a trail clustered at the main is watching, a trail
 * at far expos is wandering. Representation only, never a decision.
 */
public final class ScoutWatch {

    private ScoutWatch() {
    }

    /** Row per live scout on the sample frames, nothing in between. */
    public static void sample() {
        if (!A.everyNthGameFrame(31)) return;

        for (AUnit scout : ScoutState.scouts) {
            if (scout == null || !scout.isAlive()) continue;

            APosition main = null;
            try {
                main = EnemyMainBase.get();
            } catch (RuntimeException ignored) {
            }

            EventLog.key(LogKey.SCOUT_WATCH)
                .param("unit", scout.id())
                .param("ux", scout.tx())
                .param("uy", scout.ty())
                .param("ex", main == null ? -1 : main.tx())
                .param("ey", main == null ? -1 : main.ty())
                .log(A.now(), A.minSec());
        }
    }
}
