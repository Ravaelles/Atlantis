package atlantis.terran.repair;

import atlantis.config.AtlantisRaceConfig;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class RepairableUnits {
    public static Selection get() {
        return Select.our()
            .repairable(true)
            .wounded()
            .excludeTypes(AtlantisRaceConfig.WORKER)
            .notScout();
    }
}
