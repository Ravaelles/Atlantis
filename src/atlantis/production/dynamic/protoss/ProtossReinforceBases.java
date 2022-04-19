package atlantis.production.dynamic.protoss;

import atlantis.map.ABaseLocation;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Protoss_Pylon;

public class ProtossReinforceBases {

    public static boolean handle() {
        for (AUnit base : Select.ourBases().list()) {
            int existingCannonsNearby = Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, base);

            System.err.println(base + " cannons = " + existingCannonsNearby);

            if (existingCannonsNearby < 1) {
                HasPosition nearTo = ABaseLocation.mineralsCenter(base);
                if (Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, nearTo) == 0) {
                    if (Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 8, nearTo) == 0) {
                        nearTo = nearTo.translateTilesTowards(3, base);
//                        System.err.println(
//                            "Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 8, nearTo) = "
//                                + Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 8, nearTo)
//                        );
                        AddToQueue.withHighPriority(Protoss_Pylon, nearTo);
                        return true;
                    }

                    AntiLandBuildingManager.get().requestOne(nearTo);
//                    System.err.println("Requested Cannon to protect base " + base);
                    return true;
                }
            }
        }

        return false;
    }

}
