package atlantis.map.region;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MainRegion {
    private static ARegion mainRegion = null;

    public static boolean isMainRegion(ARegion region) {
        return mainRegion().equals(region);
    }

    public static ARegion mainRegion() {
        if (mainRegion == null) {
            AUnit any = Select.mainOrAnyBuilding();
            if (any == null) return null;

            mainRegion = any.position().region();
        }

        return mainRegion;
    }

    /** Test seam: worlds come and go, the cached answer must not outlive one. */
    public static void clearForTests() {
        mainRegion = null;
    }

    public static HasPosition center() {
        ARegion mainRegion = mainRegion();
        if (mainRegion == null) return null;

        return mainRegion.center();
    }
}
