package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.cancelling.CancelConstruction;
import atlantis.production.constructions.position.PositionsInBases;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProtossDefineNearTo {
    public static HasPosition forProtoss(AUnitType t, HasPosition nearTo) {
        if (!We.protoss()) return null;

        if (nearTo != null) {
            return nearTo;
        }

        if (nearTo == null && t.isPylon()) {
            nearTo = PylonPosition.nearToForPylon(nearTo);
        }

//        if (nearTo == null && A.supplyUsed() <= 11 && Count.ourOfTypeUnfinished(AUnitType.Protoss_Pylon) >= 1) {
//            nearTo = nearTheOnlyPylon(t, nearTo);
//        }

        if (nearTo == null && t.isCannon()) {
            nearTo = forCannon(t, nearTo);
        }

        if (nearTo == null && t.is(AUnitType.Protoss_Shield_Battery)) {
            nearTo = Chokes.mainChoke();
        }

        if (nearTo == null && Enemy.zerg() && A.supplyUsed() <= 40) {
            nearTo = Select.mainOrAnyBuilding();
        }

        if (nearTo == null && t.isGateway() && (Enemy.terran() || Count.gateways() == 2)) {
            nearTo = Chokes.mainChoke();
            if (t != null) nearTo.translateTilesTowards(5, Select.mainOrAnyBuilding());
        }

        if (nearTo == null && A.supplyUsed() <= 75) {
            nearTo = Select.mainOrAnyBuilding();
        }

        if (nearTo == null && t.isGateway()) {
            if (Count.gateways() <= 5) {
                nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).mostDistantTo(Select.mainOrAnyBuilding());
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

    public static HasPosition forceAtMain(AUnitType t, HasPosition nearTo) {
        if (CancelConstruction.countRecentCancellationsOf(t) == 0) {
            if (t.isRoboticsFacility()) {
                return PositionsInBases.mainHalfwayToMainChoke();
            }

            if (t.isRoboticsSupportBay() || t.isObservatory() || t.isFleetBeacon()) {
                return PositionsInBases.atTheBackOfMain();
            }
        }

        return null;
    }
}
