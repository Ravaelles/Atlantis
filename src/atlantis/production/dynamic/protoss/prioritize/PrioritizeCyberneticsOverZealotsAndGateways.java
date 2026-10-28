package atlantis.production.dynamic.protoss.prioritize;

import atlantis.game.A;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class PrioritizeCyberneticsOverZealotsAndGateways {
    public static boolean prioritizeCybernetics() {
        return Count.gatewaysWithUnfinished() >= 1
            && (!Have.cyberneticsCoreWithUnfinished() || Count.dragoonsWithUnfinished() == 0)
            && !A.hasMinerals(292);
    }
}
