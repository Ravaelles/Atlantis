package atlantis.production.dynamic.expansion.decision;

import atlantis.game.A;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.expansion.terran.TerranShouldExpand;
import atlantis.production.dynamic.expansion.zerg.ZergShouldExpand;
import atlantis.units.select.Have;
import atlantis.util.We;

public class ShouldExpand {
    public static String reason = "---";

    public static boolean shouldExpand() {
        if (A.isUms() && !Have.base()) return false;

        if (We.terran()) return TerranShouldExpand.shouldExpand();
        if (We.protoss()) return ProtossShouldExpand.shouldExpand();
        if (We.zerg()) return ZergShouldExpand.shouldExpand();

        return false;
    }
}
