package atlantis.production.requests;

import atlantis.config.AtlantisRaceConfig;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
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
            detectorBuilding = AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        }
        else if (We.protoss()) {
            detectorBuilding = AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND;
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
//            AUnit nearestBunker = Select.ourOfTypeWithUnfinished(AUnitType.Terran_Bunker)
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
//        int numberOfDetectors = ConstructionsCommander.countExistingAndPlannedConstructionsInRadius(
//                detectorBuilding, 8, where
//        );
//
//        for (int i = 0; i < 2 - numberOfDetectors; i++) {
//            ConstructionsCommander.requestConstructionOf(detectorBuilding, where);
//        }
    }

    private static void requestDetectorConstruction(AUnitType detectorBuilding) {
        int detectors = ConstructionRequests.countExistingAndNotFinished(detectorBuilding);


        // === Ensure parent exists ========================================

        int requiredParents = ConstructionRequests.countExistingAndNotFinished(detectorBuilding.whatIsRequired());
        if (requiredParents == 0) {

            AddToQueue.withTopPriority(detectorBuilding.whatIsRequired());
            return;
        }

        // === Protect every base ==========================================
//
        for (AUnit base : Select.ourBases().list()) {
            int numberOfDetectorsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
                detectorBuilding, 15, base.position()
            );

            for (int i = 0; i <= 2 - numberOfDetectorsNearBase; i++) {

                AddToQueue.withTopPriority(detectorBuilding, base.position());
            }
        }
    }

}
