package atlantis.production.constructing.position.protoss;

import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.FindPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

public class PylonPosition {
    public static HasPosition nextPosition() {
//        return APositionFinder.findStandardPosition(
        return FindPosition.findForBuilding(
            FreeWorkers.get().first(),
            AUnitType.Protoss_Pylon,
            null,
            defineNearTo(),
            37
        );
    }

    private static AUnit defineNearTo() {
        if (Count.bases() >= 2) {
            return Select.ourBases().random();
        }

        return Select.mainOrAnyBuilding();
    }

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
            position = position.translateTilesTowards(mineralsCenter, -2);
//            AAdvancedPainter.paintPosition(position, "AwayFromMinerals");
        }
        if (geyser != null) {
            position = position.translateTilesTowards(geyser, -1);
//            AAdvancedPainter.paintPosition(position, "AwayFromGeyserAndMinerals");
        }
        if (position != null) position = position.translatePercentTowards(base, 90);
        if (position != null) position = position.translateTilesTowards(base, -1);

        return position;
    }

    public static APosition positionForSecondPylon(APosition initialNearTo) {
//        return initialNearTo;

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
