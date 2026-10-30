package atlantis.combat.targeting;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Names a refused chase: which zealot, which target, how far. The veto
 * itself stays in {@link ChaseVeto}; this only records what happened.
 * Per-unit throttled (a standing refusal would otherwise row every
 * frame) - transitions always row, steady refusals heartbeat.
 */
public final class ChaseVetoTwin {

    private static final int HEARTBEAT_FRAMES = 150;
    private static final Map<Integer, Integer> lastRowed = new HashMap<>();
    private static final Map<Integer, Integer> lastTarget = new HashMap<>();

    private ChaseVetoTwin() {
    }

    public static void vetoed(AUnit unit, AUnit target) {
        try {
            int now = A.now();
            Integer last = lastRowed.get(unit.id());
            Integer lastFoe = lastTarget.get(unit.id());
            boolean transition = lastFoe == null || target.id() != lastFoe;

            if (!transition && last != null && now - last < HEARTBEAT_FRAMES) return;

            lastRowed.put(unit.id(), now);
            lastTarget.put(unit.id(), target.id());

            EventLog.key(LogKey.CHASE_VETO)
                .param("unit", unit.id())
                .param("target", target.id())
                .param("dist", (int) (unit.distTo(target) * 10))
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
