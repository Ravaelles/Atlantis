package atlantis.battle.kite;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Names every kiting step and every step that had nowhere to go (owner
 * 2026-10-19, Wine 1Drag_v_1Zeal follow-up: kite-move-kite, then a stop,
 * then trading at point blank - without rows the stop is guesswork).
 * The decision itself stays in {@link atlantis.battle.kite.Kite#shouldKite}; this
 * only records what happened. Every step rows: throttling would hide
 * exactly the timing a hit attribution needs (which step preceded the
 * swing), and even a brawl's worth of rows is nothing to the twin
 * stream. Stuck steps always rowed.
 */
public class KiteTwin {

    private KiteTwin() {
    }

    public static void steppedAside(AUnit dragoon, AUnit threat) {
        row(dragoon, threat, "stepped-aside");
    }

    public static void nowhereToStep(AUnit dragoon, AUnit threat) {
        row(dragoon, threat, "stuck-no-step");
    }

    public static void frozenReset(AUnit dragoon, AUnit threat) {
        row(dragoon, threat, "frozen-reset");
    }

    /**
     * The long deflect fired (NEXT #173): the straight ray was eaten by
     * a wall and the escape bent at full length instead of shuffling
     * short into the pocket. Unthrottled like every other step - rarity
     * is the throttle, and games must prove the branch engages at all.
     */
    public static void longEscape(AUnit dragoon, AUnit threat) {
        row(dragoon, threat, "long-deflect");
    }

    private static void row(AUnit dragoon, AUnit threat, String result) {
        EventLog.key(LogKey.KITE)
            .param("unit", dragoon.id())
            .param("dist", (int) (dragoon.distTo(threat) * 10))
            .param("cooldown", dragoon.cooldown())
            .param("ux", dragoon.x() / 32)
            .param("uy", dragoon.y() / 32)
            .param("tx", threat.x() / 32)
            .param("ty", threat.y() / 32)
            .param("result", result)
            .log(A.now(), A.minSec());
    }

    public static void clearForTests() {
        // No state left to clear (steps row unthrottled since the hit
        // attribution needed exact timing); kept so call sites stay stable.
    }
}
