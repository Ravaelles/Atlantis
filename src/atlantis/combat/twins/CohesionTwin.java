package atlantis.combat.twins;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Names a recalled straggler: which unit, which target it was denied,
 * how far from the ball. Mirrors {@code ChaseVetoTwin} (transitions
 * always row, steady refusals heartbeat) - a calm army rows nothing.
 */
public final class CohesionTwin {

    private static final int HEARTBEAT_FRAMES = 150;
    private static final Map<Integer, Integer> lastRowed = new HashMap<>();
    private static final Map<Integer, Integer> lastTarget = new HashMap<>();

    private CohesionTwin() {
    }

    public static void recalled(AUnit unit, AUnit target, HasPosition median) {
        try {
            int now = A.now();
            Integer last = lastRowed.get(unit.id());
            // March recalls (no target) row under their own sentinel: a
            // targetless rejoin is its own state, not a repeat of the last
            // denied target - without this every march recall reads as a
            // transition and the heartbeat never throttles it.
            int foe = target == null ? -1 : target.id();
            Integer lastFoe = lastTarget.get(unit.id());
            boolean transition = lastFoe == null || foe != lastFoe;

            if (!transition && last != null && now - last < HEARTBEAT_FRAMES) return;

            lastRowed.put(unit.id(), now);
            lastTarget.put(unit.id(), foe);

            EventLog.key(LogKey.COHESION_RECALL)
                .param("unit", unit.id())
                .param("target", foe)
                .param("dist", (int) (unit.distTo(median) * 10))
                .param("transition", transition)
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }

    public static void clearForTests() {
        lastRowed.clear();
        lastTarget.clear();
    }
}
