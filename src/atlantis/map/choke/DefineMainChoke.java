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

        // Measured GAME_9FC7B9A3 / GAME_02EC7DF2 (TauCross): the main
        // position can sit outside every BWEM area, so region() is null and
        // the call below threw ~5000 NullPointerExceptions per game, one per
        // frame, skipping the JBWEB/custom fallback this method's caller
        // already handles. A missing region is "no answer", not an error.
        ARegion region = main.region();
        if (region == null) {
            return null;
        }

        List<ARegion> reachableRegions = region.getReachableRegions();
        if (reachableRegions == null || reachableRegions.size() != 1) return null;

//        List<AChoke> mainChokes = region.chokes();

        naturalRegion = reachableRegions.get(0);
        return region.chokeBetween(naturalRegion);
    }

    protected static ARegion naturalRegion() {
        return naturalRegion;
    }
}
