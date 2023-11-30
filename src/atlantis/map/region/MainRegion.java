package atlantis.map.region;

import atlantis.units.select.Select;

public class MainRegion {
    private static ARegion mainRegion = null;

    public static boolean isMainRegion(ARegion region) {
        return mainRegion().equals(region);
    }

    public static ARegion mainRegion() {
        if (mainRegion == null) {
            mainRegion = Select.mainOrAnyBuilding().position().region();
        }

        return mainRegion;
    }
}
