package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossDefineNearTo {
    public static HasPosition forProtoss(AUnitType t, HasPosition nearTo) {
        if (!We.protoss()) return null;

        if (nearTo == null && t.isPylon()) {
            nearTo = defineNearToForPylon(nearTo);
        }

//        if (nearTo == null && A.supplyUsed() <= 11 && Count.ourOfTypeUnfinished(AUnitType.Protoss_Pylon) >= 1) {
//            nearTo = nearTheOnlyPylon(t, nearTo);
//        }

        if (nearTo == null && t.isCannon()) {
            nearTo = forCannon(t, nearTo);
        }

        if (nearTo == null && t.isGateway() && (Enemy.terran() || Count.gateways() == 2)) {
            nearTo = Chokes.mainChoke();
            if (t != null) nearTo.translateTilesTowards(5, Select.mainOrAnyBuilding());
        }

        if (nearTo == null && t.is(AUnitType.Protoss_Shield_Battery)) {
            nearTo = Chokes.mainChoke();
        }

        if (nearTo == null && A.supplyUsed() <= 75) {
            nearTo = Select.mainOrAnyBuilding();
        }

        if (nearTo == null && t.isGateway()) {
            if (Count.gateways() <= 5) {
                nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).nearestTo(Select.mainOrAnyBuilding());
//                nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
            }

            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

            if (nearTo == null) {
                if (A.chance(80)) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
            }

            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

            if (nearTo == null
                && (!A.hasFreeSupply(2) || A.supplyTotal() >= 130)
                && A.chance(60)) {
                nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();
            }
        }
        else {
            if (nearTo == null) nearTo = Select.mainOrAnyBuilding();
        }

        return nearTo;
    }

    private static HasPosition forCannon(AUnitType building, HasPosition nearTo) {
        if (nearTo == null) {
            nearTo = DefineCannonNearTo.define();
        }

        if (nearTo == null) {
            int bases = Count.bases();
            if (bases >= 3) return Select.ourBases().last();
            if (bases == 2) return Chokes.natural();
            return Chokes.mainChoke();
        }

        return nearTo;
    }

    private static HasPosition nearTheOnlyPylon(AUnitType building, HasPosition nearTo) {
        if (building.isGasBuilding() || building.isBase()) return null;

        if (nearTo == null) return Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).first();

        HasPosition pylon = Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Pylon).nearestTo(nearTo);
        if (pylon == null) {
            pylon = ConstructionRequests.nearestOfTypeTo(AUnitType.Protoss_Pylon, nearTo, 15);
        }

        return pylon;
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

    public static HasPosition forceAtMain(AUnitType t, HasPosition nearTo) {
        if (t.isRoboticsFacility() || t.isRoboticsSupportBay() || t.isObservatory() || t.isFleetBeacon()) {
            return Select.main();
        }

        return null;
    }
}
