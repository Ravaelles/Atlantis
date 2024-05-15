package atlantis.production.constructing.position;

import atlantis.game.A;
import atlantis.game.race.MyRace;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.MainRegion;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class DefineNearTo {
    public static HasPosition defineNearTo(AUnitType building, HasPosition nearTo) {
        if (nearTo != null) return nearTo;

        if (A.supplyUsed() <= 19) {
            nearTo = Select.mainOrAnyBuilding();
        }

        if (nearTo == null && A.supplyUsed() <= 45) {
            nearTo = MainRegion.center();
        }

        if (We.protoss()) {
            if (nearTo == null && building.isCannon()) {
                int bases = Count.bases();
                if (bases >= 3) return Select.ourBases().last();
                if (bases == 2) return Chokes.natural();
                if (bases <= 1) return Chokes.mainChoke();
            }

            if (nearTo == null && building.isGateway()) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

            if (nearTo == null) {
                if (A.chance(70)) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
            }

            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();
        }

        if (We.terran()) {
            if (nearTo == null && building.isSupplyDepot() && A.chance(50)) {
                nearTo = Select.ourOfType(AUnitType.Terran_Supply_Depot).last();
            }

            if (nearTo == null && A.chance(50)) nearTo = Select.ourBuildings().last();
        }

        if (nearTo == null) nearTo = Select.mainOrAnyBuilding();
        if (nearTo == null) {
            ErrorLog.printMaxOncePerMinute("Apply dirty hack as nearTo is still null for " + building);
            nearTo = APosition.create(50, 50);
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
}
