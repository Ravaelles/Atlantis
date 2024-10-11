package atlantis.production.constructing.position;

import atlantis.game.A;
import atlantis.game.race.MyRace;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.protoss.DefineCannonNearTo;
import atlantis.production.constructing.position.protoss.PylonPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class DefineNearTo {
    public static HasPosition defineNearTo(AUnitType building, HasPosition nearTo) {
        if (nearTo != null) return nearTo;

        if (We.protoss()) {
            nearTo = forProtoss(building, nearTo);
        }

        if (We.terran()) {
            nearTo = forTerran(building, nearTo);
        }

        if (nearTo == null) nearTo = Select.mainOrAnyBuilding();
        if (nearTo == null) {
            ErrorLog.printMaxOncePerMinute("Apply dirty hack as nearTo is still null for " + building);
            nearTo = APosition.create(50, 50);
        }

        return nearTo;
    }

    private static HasPosition forTerran(AUnitType building, HasPosition nearTo) {
        if (nearTo == null && building.isSupplyDepot() && A.chance(50)) {
            nearTo = Select.ourOfType(AUnitType.Terran_Supply_Depot).last();
        }

        if (nearTo == null && A.supplyUsed() <= 45) {
            nearTo = MainRegion.center();
        }

        if (nearTo == null && A.chance(50)) nearTo = Select.ourBuildings().last();
        return nearTo;
    }

    private static HasPosition protossOnePylonForBuilding(AUnitType building, HasPosition nearTo) {
        if (A.supplyUsed() >= 12) return null;
        if (building.isGasBuilding() || building.isBase()) return null;

        HasPosition pylon = Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).nearestTo(nearTo);
        if (pylon == null) {
            pylon = ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Pylon, nearTo, 15);
        }

        return pylon;
    }

    private static HasPosition forProtoss(AUnitType building, HasPosition nearTo) {
        if (!We.protoss()) return null;

        if (building.isPylon()) {
            nearTo = defineNearToForPylon(nearTo);
        }

        if (nearTo == null) {
            nearTo = protossOnePylonForBuilding(building, nearTo);
        }

        if (nearTo == null && A.supplyUsed() <= 19) {
            nearTo = Select.mainOrAnyBuilding();
        }

        if (building.isGateway()) {
            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

            if (nearTo == null) {
                if (A.chance(80)) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
            }

            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

            if (nearTo == null
                && A.supplyTotal() <= 45
                && building.isGateway()
                && Count.gateways() <= 3
            ) {
                nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).nearestTo(Select.mainOrAnyBuilding());
            }

            if (nearTo == null
                && (!A.hasFreeSupply(2) || A.supplyTotal() >= 130)
                && A.chance(60)) {
                nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();
            }
        }

        if (nearTo == null && building.isCannon()) {
            int bases = Count.bases();
            if (bases >= 3) return Select.ourBases().last();
            if (bases == 2) return Chokes.natural();
            return Chokes.mainChoke();
        }

        if (nearTo == null && building.isCannon()) {
            nearTo = DefineCannonNearTo.define();
        }

        return nearTo;
    }

    public static HasPosition defineNearTo(HasPosition nearTo) {
        if (nearTo == null) {
            if (MyRace.isPlayingAsZerg()) {
                nearTo = Select.main().position();
            }
            else {
                if (Count.bases() >= 3) {
                    nearTo = Select.ourBases().random();
                }
                else {
                    nearTo = Select.main().position();
                }
            }
        }

        if (nearTo == null) nearTo = Select.ourBuildings().first().position();

        // If all of our bases have been destroyed, build somewhere near our first unit alive
        if (nearTo == null) nearTo = Select.our().first().position();

        return nearTo;
    }

    private static HasPosition defineNearToForPylon(HasPosition nearTo) {
//        int supply = A.supplyTotal();
        int pylons = Count.pylons();

        // First pylon should be close to Nexus for shorter travel dist
        if (pylons <= 0) {
            nearTo = PylonPosition.nearToPositionForFirstPylon();
//                AAdvancedPainter.paintPosition(nearTo, "PylonPosition");
        }

        // First pylon should be oriented towards the nearest choke
        else if (pylons <= 1) {
            nearTo = PylonPosition.nearToPositionForSecondPylon();
        }

        AUnit main = Select.main();
        if (main != null) {
            if (main.friendsNear().buildings().atMost(13)) nearTo = main;
        }

        if (nearTo == null) nearTo = Select.ourBasesWithUnfinished().exclude(Bases.natural()).random();
        if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
//        if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

//        System.err.println("pylons = " + pylons);

        return nearTo;
    }
}
