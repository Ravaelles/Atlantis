package atlantis.battle.formation;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Names every corridor cleared and every pursuer covered (NEXT #162):
 * without rows a stepped-aside helper reads as "wandered off" and a
 * covering unit reads as "targeting noise". The decisions stay in
 * {@link CoverWithdraw}; this only records what happened, at the
 * call site that consumes the decision - the same split as
 * KiteTwin (pure rule, twin at the order).
 */
public final class FormationTwin {

    private FormationTwin() {
    }

    public static void madeWay(AUnit helper, AUnit escaper, APosition dest) {
        EventLog.key(LogKey.MAKE_WAY)
            .param("unit", helper.id())
            .param("escaper", escaper.id())
            .param("ux", helper.x() / 32)
            .param("uy", helper.y() / 32)
            .param("dx", dest.x() / 32)
            .param("dy", dest.y() / 32)
            .log(A.now(), A.minSec());
    }

    public static void covering(AUnit healthy, AUnit pursuer, AUnit escaper) {
        EventLog.key(LogKey.COVER)
            .param("unit", healthy.id())
            .param("pursuer", pursuer.id())
            .param("escaper", escaper.id())
            .param("ux", healthy.x() / 32)
            .param("uy", healthy.y() / 32)
            .param("px", pursuer.x() / 32)
            .param("py", pursuer.y() / 32)
            .log(A.now(), A.minSec());
    }
}
