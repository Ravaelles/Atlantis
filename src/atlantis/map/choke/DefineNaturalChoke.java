package atlantis.map.choke;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.units.select.Select;
import bwapi.Color;

import java.util.List;

public class DefineNaturalChoke {
    public static AChoke define() {
        if (Chokes.mainChoke() == null) return null;

//                if (!ActiveMap.isMap("7th")) {
//                    AChoke naturalFromJbweb = AChoke.from(JBWEB.getNaturalChoke());
//                    System.err.println("@@@@ naturalFromJbweb = " + naturalFromJbweb);
//                    if (naturalFromJbweb != null) return naturalFromJbweb;
//                }
//
//                if (!ActiveMap.isMap("7th")) {
//                    AChoke choke = AChoke.from(JBWEB.getNaturalChoke());
//                    System.err.println("@@@@ CHOKE B = " + choke);
//                    if (fullfillsConditionsForNatural(choke, "NATURAL")) {
//                        //                        System.err.println("choke.position() = " + choke.position());
//                        //                        System.err.println("AMap.getMapHeightInTiles() = " + AMap.getMapHeightInTiles());
//                        return choke;
//                    }
//                }
//
        AChoke choke = defineByReturningChokeBelongingToRegionCloseToMain();
        if (choke != null) return choke;

        return defineByFindingNaturalBaseAndReturningClosestChoke();
    }

    public static AChoke defineByReturningChokeBelongingToRegionCloseToMain() {
        AChoke mainChoke = DefineMainChoke.define();
        if (mainChoke == null) return null;

//        AAdvancedPainter.paintChoke(mainChoke, Color.Red, "MAIN CHOKE");

        ARegion naturalRegion = DefineMainChoke.naturalRegion();
        if (naturalRegion == null) return null;

        for (AChoke choke : naturalRegion.chokes()) {
            if (choke.equals(mainChoke)) continue;

//            AAdvancedPainter.paintChoke(choke, Color.Yellow, "NATURAL CHOKE");
            return choke;
        }

        return null;
    }

    private static AChoke defineByFindingNaturalBaseAndReturningClosestChoke() {
        APosition naturalBase = DefineNaturalBase.natural();
//                System.err.println("@@@@ naturalBase = " + naturalBase);
//                System.err.println("@@@@ CHOKE C = " + nearestChoke(naturalBase, "MAIN"));

        return Chokes.nearestChoke(naturalBase, "MAIN");
    }
}
