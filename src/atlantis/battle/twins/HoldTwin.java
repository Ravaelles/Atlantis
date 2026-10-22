package atlantis.battle.twins;

import atlantis.battle.FormationHold;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Throttled HOLD twin (NEXT #182): which units hold formation slots and
 * why the others decline. Rows carry the deciding gate (verdict) plus
 * the nearest-enemy distance and foes-in-2, so the next "stood into
 * chews" report is decidable from logs instead of forensics.
 *
 * <p>Throttled: a row fires when the verdict flips for a unit, plus a
 * slow heartbeat for steady holds. The hold decision runs every frame
 * per unit; unthrottled it would drown the log it exists to clarify.</p>
 */
public final class HoldTwin {

    /** Heartbeat while the verdict does not change. */
    private static final int HEARTBEAT_FRAMES = 150;

    private static final Map<Integer, String> lastVerdict = new HashMap<>();
    private static final Map<Integer, Integer> lastLogFrame = new HashMap<>();

    private HoldTwin() {
    }

    public static void clearForTests() {
        lastVerdict.clear();
        lastLogFrame.clear();
    }

    public static void note(AUnit unit, AUnit target, FightOrFlight.Tactic tactic, FormationHold.HoldVerdict verdict) {
        try {
            if (unit == null || !unit.isAlive() || verdict == null) return;

            int now = A.now();
            String name = verdict.name();
            String prev = lastVerdict.get(unit.id());
            Integer last = lastLogFrame.get(unit.id());
            boolean changed = prev == null || !prev.equals(name);
            if (!changed && last != null && now - last < HEARTBEAT_FRAMES) return;

            lastVerdict.put(unit.id(), name);
            lastLogFrame.put(unit.id(), now);

            int foeDist = -1;
            int foes2 = 0;
            try {
                Selection foes = Select.enemy().combatUnits();
                if (foes != null && !foes.isEmpty()) {
                    AUnit nearest = foes.nearestTo(unit);
                    if (nearest != null && nearest.position() != null) {
                        foeDist = (int) (unit.distTo(nearest) * 10);
                    }
                    foes2 = foes.inRadius(2.0, unit).count();
                }
            } catch (Exception ignored) {
            }

            EventLog.key(LogKey.HOLD)
                .param("unit", unit.id())
                .param("tgt", target == null ? -1 : target.id())
                .param("tactic", tactic == null ? "-" : tactic.name())
                .param("verdict", name)
                .param("foe", foeDist)
                .param("foes2", foes2)
                .param("ux", unit.x() / 32)
                .param("uy", unit.y() / 32)
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }
}
