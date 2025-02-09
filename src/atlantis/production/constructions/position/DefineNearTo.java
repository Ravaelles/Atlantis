package atlantis.production.constructions.position;

import atlantis.game.A;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.production.constructions.position.protoss.ProtossDefineNearTo;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class DefineNearTo {
    public static HasPosition defineNearTo(AUnitType building, HasPosition nearTo) {
        if (We.terran()) {
            nearTo = forTerran(building, nearTo);
        }

        else if (We.protoss()) {
            HasPosition forcedNearTo = ProtossDefineNearTo.forceAtMain(building, nearTo);
            if (forcedNearTo != null) return forcedNearTo;

            nearTo = ProtossDefineNearTo.forProtoss(building, nearTo);
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

        if (nearTo == null && building.isBarracks() && A.supplyUsed() >= 11) {
            nearTo = Chokes.mainChoke().translateTilesTowards(8, Select.mainOrAnyBuilding());
        }

        if (nearTo == null && A.supplyUsed() <= 40) {
            nearTo = Select.mainOrAnyBuilding();
        }

        if (nearTo == null && A.supplyUsed() <= 70) {
            nearTo = MainRegion.center();
        }

        if (nearTo == null && A.chance(50)) nearTo = Select.ourBuildings().last();
        return nearTo;
    }

    public static HasPosition defineNearTo(HasPosition nearTo) {
        if (nearTo == null) {
            if (We.zerg()) {
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
}
