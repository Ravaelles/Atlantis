package atlantis.production.constructing.position.protoss;

import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class PylonPosition {

    public static APosition positionForFirstPylon() {
        AUnit base = Select.main();
        if (base == null) {
            return fallback();
        }

        APosition mineralsCenter = Select.minerals().inRadius(10, base).center();

        if (mineralsCenter == null) {
            return fallback();
        }

        return base.translateTilesTowards(mineralsCenter, -3);
    }

    public static APosition positionForSecondPylon() {
        AUnit base = Select.main();
        AChoke mainChoke = Chokes.mainChoke();
        if (base == null || mainChoke == null) {
            return fallback();
        }

        return mainChoke.translateTilesTowards(base, 8);
    }

    // =========================================================

    private static APosition fallback() {
        AUnit first = Select.our().first();
        return first != null ? first.position() : null;
    }

}
