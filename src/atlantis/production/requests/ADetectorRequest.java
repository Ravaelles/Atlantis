package atlantis.production.requests;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionRequests;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ADetectorRequest {

    /**
     * Request quickest possible detector to be built (e.g. Comsat Station for Terran, not Science Vessel).
     * Cancel all other not started build orders, to make sure you have resources.
     */
    public static void requestDetectorImmediately(APosition where) {
        AUnitType detectorBuilding = null;
        if (AGame.isPlayingAsTerran()) {
            detectorBuilding = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        }
        else if (AGame.isPlayingAsProtoss()) {
            detectorBuilding = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND;
        }
        else if (AGame.isPlayingAsZerg()) {
            // Zerg has Overlords, they should be handling this situation
            return;
        }

        // =========================================================

        AConstructionRequests.removeAllNotStarted();
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
//            where = MissionDefend.getInstance().focusPoint().translatePercentTowards(AMap.getNaturalBaseLocation(), 32);
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
        int detectors = AConstructionRequests.countExistingAndPlannedConstructions(detectorBuilding);
        System.out.println("detectors = " + detectors);

        // === Ensure parent exists ========================================

        int requiredParents = AConstructionRequests.countExistingAndPlannedConstructions(detectorBuilding.getWhatIsRequired());
        if (requiredParents == 0) {
            System.out.println("Detector dependency requested: " + detectorBuilding.getWhatIsRequired().shortName());
            AConstructionRequests.requestConstructionOf(detectorBuilding.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================
//
        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfDetectorsNearBase = AConstructionRequests.countExistingAndPlannedConstructionsInRadius(
                    detectorBuilding, 15, base.position()
            );

            for (int i = 0; i <= 2 - numberOfDetectorsNearBase; i++) {
                System.out.println("Detector construction (" + detectorBuilding.shortName() + ") requested!");
                AConstructionRequests.requestConstructionOf(detectorBuilding, base.position());
            }
        }
    }

}
