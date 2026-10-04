package atlantis.production.dynamic.expansion.decision;

import atlantis.game.A;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.expansion.terran.TerranShouldExpand;
import atlantis.production.dynamic.expansion.zerg.ZergShouldExpand;
import atlantis.units.select.Have;
import atlantis.util.We;

public class ShouldExpand {
    /**
     * Why the last call said no (or yes). Read in the queue log - a Nexus line ends with
     * "/ LimitedBases", which is how B-22's expansion chain was traced - so every way out
     * of the three race doctrines has to leave a reason here, including the two that used
     * to return without touching it and left the previous frame's string standing.
     */
    public static String reason = "_NO_EXPAND_REASON_";

    public static boolean shouldExpand() {
        // Set directly rather than through the race doctrines' no(reason), which also
        // cancels not-started bases to free minerals: "we are saving for a base" must not
        // cancel the base we are saving for.
        if (A.isUms() && !Have.base()) {
            reason = "UmsNoBase";
            return false;
        }

        if (We.terran()) return TerranShouldExpand.shouldExpand();
        if (We.protoss()) return ProtossShouldExpand.shouldExpand();
        if (We.zerg()) return ZergShouldExpand.shouldExpand();

        reason = "UnknownRace";
        return false;
    }
}
