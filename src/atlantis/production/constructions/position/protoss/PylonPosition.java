package atlantis.production.constructions.position.protoss;

import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.FindPosition;
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
        if (Count.pylons() >= 4 && Count.bases() >= 2) {
            return Select.ourBases().random();
        }

        return Select.mainOrAnyBuilding();
    }

    public static APosition nearToPositionForFirstPylon() {
        AUnit base = Select.main();
        if (base == null) {
            return fallback();
        }

        APosition position = base.position();
        HasPosition geyser = Select.geysers().inRadius(10, base).first();
        HasPosition mineralsCenter = ABaseLocation.mineralsCenter(base);

        if (geyser == null || mineralsCenter == null) return fallback();

        APosition centerOfResources = geyser.translateTilesTowards(mineralsCenter, 50);

        if (position == null) {
            System.err.println("Unable to position first Pylon... " + Select.minerals().inRadius(10, base).size());
            return fallback();
        }

        position = base.translateTilesTowards(-4, centerOfResources);
        position = position.makeBuildableFarFromBounds(5);

        return position;
    }

    public static APosition nearToPositionForSecondPylon() {
//        return initialNearTo;

        AUnit base = Select.main();
        AChoke mainChoke = Chokes.mainChoke();
        if (base == null || mainChoke == null) return fallback();

        return base.translateTilesTowards(mainChoke, 8);
    }

    // =========================================================

    private static APosition fallback() {
        AUnit first = Select.ourBuildings().first();
        return first != null ? first.position() : null;
    }

}
