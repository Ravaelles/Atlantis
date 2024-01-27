package atlantis.terran.chokeblockers;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.select.Select;
import atlantis.util.Vector;
import atlantis.util.Vectors;

public class ChokeToBlock {
    public static AChoke get() {
        AChoke choke = Chokes.mainChoke();

        if (choke == null || choke.width() >= 4.1) return null;

        return choke;
    }

    public static Vector defineTranslationVector(AChoke choke) {
        if (choke == null) return null;

        APosition closerPoint = choke.getClosestPointABTo(Select.mainOrAnyBuilding());

//        System.err.println("choke.center() = " + choke.center().toStringPixels());
//        System.err.println("closerPoint = " + closerPoint.toStringPixels());
//        System.err.println("vector = " + Vectors.fromPositionsBetween(choke.center(), closerPoint).normalizeTo(2.0).toString());

        return Vectors.fromPositionsBetween(choke.center(), closerPoint).multiplyVector(0.8);
    }
}
