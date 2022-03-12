package atlantis.production.constructing.position.protoss;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class PylonPosition {

    public static APosition positionForFirstPylon() {
        AUnit base = Select.main();
        if (base == null) {
            return fallback();
        }

        APosition position = base.position();
        HasPosition geyser = Select.geysers().inRadius(10, base).first();
        HasPosition mineralsCenter = ABaseLocation.mineralsCenter(base);

//        AAdvancedPainter.paintPosition(position, "geyser");
//        AAdvancedPainter.paintPosition(mineralsCenter, "mineralsCenter");

        if (position == null) {
            System.err.println("Unable to position first Pylon... " + Select.minerals().inRadius(10, base).size());
            return fallback();
        }

        if (mineralsCenter != null) {
            position = position.translateTilesTowards(mineralsCenter, -3);
//            AAdvancedPainter.paintPosition(position, "AwayFromMinerals");
        }
        if (geyser != null) {
            position = position.translateTilesTowards(geyser, -2);
//            AAdvancedPainter.paintPosition(position, "AwayFromGeyserAndMinerals");
        }

        return position;
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
