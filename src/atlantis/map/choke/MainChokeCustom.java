package atlantis.map.choke;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.MainRegion;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import static atlantis.map.base.BaseLocations.natural;

public class MainChokeCustom {
    public static AChoke get() {
        AUnit main = Select.main();
        if (main == null) {
            return null;
        }

        AChoke choke = (AChoke) Positions.nearestToFrom(main, Chokes.chokes());

        return choke;

//        // Define region where our main base is
//        ARegion mainRegion = MainRegion.mainRegion();
//        if (mainRegion == null) {
//            return null;
//        }

//        return oldCondition(mainRegion);
    }

    private static AChoke oldCondition(ARegion mainRegion) {
        // Define localization of the second base to expand
        APosition naturalBase = natural();
        if (naturalBase == null) {
            return null;
        }

        // Define region of the second base
        ARegion naturalRegion = naturalBase.region();
        if (naturalRegion == null) {
            return null;
        }

        // Try to match choke points between the two regions
        for (AChoke mainRegionChoke : mainRegion.chokes()) {
            // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
            if (naturalRegion.equals(mainRegionChoke.firstRegion())
                || naturalRegion.equals(mainRegionChoke.secondRegion())) {
                return mainRegionChoke;
            }
        }

//                    if (cached_mainBaseChoke == null) {
        return mainRegion.chokes().iterator().next();
//                    }
    }
}
