package atlantis.production.requests;

import atlantis.AtlantisConfig;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.position.APosition;
import atlantis.production.orders.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ADetectorRequest {

    /**
     * Request quickest possible detector to be built (e.g. Comsat Station for Terran, not Science Vessel).
     * Cancel all other not started build orders, to make sure you have resources.
     */
    public static void requestDetectorImmediately(APosition where) {
        AUnitType detectorBuilding = null;
        if (We.terran()) {
            detectorBuilding = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        }
        else if (We.protoss()) {
            detectorBuilding = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND;
        }
        else {
            // Zerg has Overlords, they should be handling this situation
            return;
        }

        // =========================================================

        ConstructionRequests.removeAllNotStarted();
        requestDetectorConstruction(detectorBuilding);

        // === Protect choke point =========================================

//        if (where == null) {
//            AUnit nearestBunker = Select.ourOfTypeIncludingUnfinished(AUnitType.Terran_Bunker)
//                    .nearestTo(MissionDefend.getInstance().focusPoint());
//            if (nearestBunker != null) {
//                where = nearestBunker.getPosition();
//            }
//        }
//
//        if (where == null) {
//            where = MissionDefend.getInstance().focusPoint().translatePercentTowards(AMap.getNaturalLocation(), 32);
//        }
//
//        int numberOfDetectors = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
//                detectorBuilding, 8, where
//        );
//
//        for (int i = 0; i < 2 - numberOfDetectors; i++) {
//            AConstructionManager.requestConstructionOf(detectorBuilding, where);
//        }
    }

    private static void requestDetectorConstruction(AUnitType detectorBuilding) {
        int detectors = ConstructionRequests.countExistingAndNotFinished(detectorBuilding);
//        System.out.println("detectors = " + detectors);

        // === Ensure parent exists ========================================

        int requiredParents = ConstructionRequests.countExistingAndNotFinished(detectorBuilding.getWhatIsRequired());
        if (requiredParents == 0) {
//            System.out.println("Detector dependency requested: " + detectorBuilding.getWhatIsRequired().name());
            AddToQueue.withTopPriority(detectorBuilding.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================
//
        for (AUnit base : Select.ourBases().list()) {
            int numberOfDetectorsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
                    detectorBuilding, 15, base.position()
            );

            for (int i = 0; i <= 2 - numberOfDetectorsNearBase; i++) {
//                System.out.println("Detector construction (" + detectorBuilding.name() + ") requested!");
                AddToQueue.withTopPriority(detectorBuilding, base.position());
            }
        }
    }

}
