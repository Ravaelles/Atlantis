package atlantis.production.dynamic.expansion;

import atlantis.config.AtlantisConfig;
import atlantis.units.select.Count;

public class ExpansionStatus {
    public static boolean isBaseUnderConstruction() {
        return Count.inProductionOrInQueue(AtlantisConfig.BASE) > 0;
    }
}
