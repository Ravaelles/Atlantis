package atlantis.production.dynamic.expansion;

import atlantis.config.AtlantisRaceConfig;
import atlantis.units.select.Count;

public class ExpansionStatus {
    public static boolean isBaseUnderConstruction() {
        return Count.inProductionOrInQueue(AtlantisRaceConfig.BASE) > 0;
    }
}
