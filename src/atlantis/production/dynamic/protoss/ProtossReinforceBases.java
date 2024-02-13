package atlantis.production.dynamic.protoss;

import atlantis.map.base.ABaseLocation;
import atlantis.map.position.HasPosition;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProtossReinforceBases {
    public static boolean invoke() {
        for (AUnit base : Select.ourBases().list()) {
            HasPosition nearTo = ABaseLocation.mineralsCenter(base);
            int cannonsNearby = Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, nearTo);

//            System.err.println(base + " cannons = " + cannonsNearby);

            if (cannonsNearby == 0) {

//                // @Pylon @ToDo
//                if (Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 8, nearTo) == 0) {
////                    System.err.println(
////                        "Pylons = "
////                            + Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 8, nearTo)
////                    );
//                    nearTo = nearTo.translateTilesTowards(3, base);
//                    AddToQueue.withHighPriority(Protoss_Pylon, nearTo);
//                    return true;
//                }

                AntiLandBuildingCommander.get().requestOne(nearTo);
//                    AddToQueue.withHighPriority(Protoss_Photon_Cannon, nearTo);
//                    System.err.println("Requested Cannon to protect base " + base);
                return true;
            }
        }

        return false;
    }

}
