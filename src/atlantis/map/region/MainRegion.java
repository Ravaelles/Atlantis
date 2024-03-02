package atlantis.map.region;

import atlantis.map.position.HasPosition;
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

    public static HasPosition center() {
        ARegion mainRegion = mainRegion();
        if (mainRegion == null) return null;

        return mainRegion.center();
    }
}
