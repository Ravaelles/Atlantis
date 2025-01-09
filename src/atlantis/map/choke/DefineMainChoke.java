package atlantis.map.choke;

import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.units.select.Select;
import jbweb.JBWEB;

import java.util.List;

public class DefineMainChoke {
    private static ARegion naturalRegion = null;

    public static AChoke define() {
        AChoke mainChoke = defineFromMainBaseRegion();
        if (mainChoke != null) return mainChoke;
        
        return defineFromJbwebOrCustomSolution();
    }

    private static AChoke defineFromJbwebOrCustomSolution() {
        AChoke mainChoke = mainChokeFromJbweb();
        if (mainChoke != null) return mainChoke;

        return MainChokeCustom.get();
    }

    private static AChoke mainChokeFromJbweb() {
        return AChoke.from(JBWEB.getMainChoke());
    }

    private static AChoke defineFromMainBaseRegion() {
        APosition main = Select.mainOrAnyBuildingPosition();
        if (main == null) {
            return null;
        }

        ARegion region = main.region();
        List<ARegion> reachableRegions = region.getReachableRegions();
        if (reachableRegions.size() != 1) return null;

//        List<AChoke> mainChokes = region.chokes();

        naturalRegion = reachableRegions.get(0);
        return region.chokeBetween(naturalRegion);
    }

    protected static ARegion naturalRegion() {
        return naturalRegion;
    }
}
