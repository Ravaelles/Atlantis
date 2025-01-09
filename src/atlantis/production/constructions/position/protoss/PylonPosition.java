package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.Bases;
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

    private static HasPosition defineNearTo() {
        int pylons = Count.pylons();

        if (pylons >= 4 && Count.bases() >= 2) {
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

    public static HasPosition nearToForPylon(HasPosition nearTo) {
//        int supply = A.supplyTotal();
        int pylons = Count.pylons();

        // First pylon should be close to Nexus for shorter travel dist
        if (pylons <= 0) {
            nearTo = nearToPositionForFirstPylon();
//                AAdvancedPainter.paintPosition(nearTo, "PylonPosition");
        }

        // First pylon should be oriented towards the nearest choke
        else if (pylons <= 1) {
            nearTo = nearToPositionForSecondPylon();
        }

        // First pylon should be oriented towards the nearest choke
        else if (pylons == 5) {
            nearTo = Select.mainOrAnyBuilding().translatePercentTowards(Chokes.mainChoke(), 60);
        }

//        AUnit main = Select.main();
//        if (main != null) {
//            if (main.friendsNear().buildings().atMost(13)) nearTo = main;
//        }

//        if (A.chance(70)) {
//            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).nearestTo(EnemyUnits.nearestEnemyBuilding());
//        }
        if (A.supplyFree() <= 1 && A.hasMinerals(300)) {
//            if (nearTo == null && A.chance(70) && Count.bases() >= 3) nearTo = Select.ourBases().last();
            if (nearTo == null) nearTo = Select.mainOrAnyBuildingPosition();
        }
        if (nearTo == null) nearTo = Select.ourBasesWithUnfinished().exclude(Bases.natural()).last();
        if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
//        if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

//        System.err.println("pylons = " + pylons);

        return nearTo;
    }
}
