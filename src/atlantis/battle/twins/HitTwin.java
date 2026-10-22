package atlantis.battle.twins;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Names every hit a fighting unit takes: how much HP+shields went
 * missing since the previous frame, with the threat distance, weapon
 * cooldown and movement state at that moment (owner 2026-10-19, Wine
 * 1Drag_v_1Zeal: the Dragoon took hits standing still at the end and
 * no row said why). Sampled, not pushed: the caller reports total
 * health every frame it acts, the twin speaks only when some went
 * missing (hits are rare, so they always land, unthrottled).
 */
public class HitTwin {

    private static final java.util.Map<Integer, Integer> lastTotalByUnit = new java.util.HashMap<>();

    private HitTwin() {
    }

    public static void sample(AUnit unit, AUnit enemy) {
        int total = unit.hp() + unit.shields();
        Integer last = lastTotalByUnit.put(unit.id(), total);
        if (last == null || total >= last) return;

        EventLog.key(LogKey.HIT)
            .param("unit", unit.id())
            .param("lost", last - total)
            .param("dist", (int) (unit.distTo(enemy) * 10))
            .param("cooldown", unit.cooldown())
            .param("moving", unit.isMoving())
            .param("ux", unit.x() / 32)
            .param("uy", unit.y() / 32)
            .param("tx", enemy.x() / 32)
            .param("ty", enemy.y() / 32)
            .param("result", "hit")
            .log(A.now(), A.minSec());
    }

    public static void clearForTests() {
        lastTotalByUnit.clear();
    }
}
