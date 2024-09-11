package atlantis.terran.chokeblockers;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Vector;
import atlantis.util.Vectors;

public class ChokeToBlock {
    public static final double BASE_DIST_FROM_CHOKE_CENTER = 1.2;

    public static AChoke get() {
        return Count.basesWithUnfinished() <= 1 ? forMainChoke() : forNaturalChoke();
    }

    private static AChoke forNaturalChoke() {
        return returnChokeIfValidToBlock(Chokes.natural());
    }

    private static AChoke forMainChoke() {
        return returnChokeIfValidToBlock(Chokes.mainChoke());
    }

    private static AChoke returnChokeIfValidToBlock(AChoke choke) {
        return choke != null && choke.width() < 4.1 ? choke : null;
    }

    public static Vector defineTranslationVector(AChoke choke) {
        if (choke == null) return null;

        APosition closerPoint = choke.getClosestPointABTo(Select.mainOrAnyBuilding());

//        System.err.println("choke.center() = " + choke.center().toStringPixels());
//        System.err.println("closerPoint = " + closerPoint.toStringPixels());
//        System.err.println("vector = " + Vectors.fromPositionsBetween(choke.center(), closerPoint).normalizeTo(2.0).toString());

        return Vectors.fromPositionsBetween(choke.center(), closerPoint).multiplyVector(vectorLength());
    }

    private static double vectorLength() {
        return BASE_DIST_FROM_CHOKE_CENTER + Alpha.count() / 4.0;
    }
}
