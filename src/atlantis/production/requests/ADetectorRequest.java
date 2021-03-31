package atlantis.production.requests;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.combat.missions.MissionDefend;
import atlantis.constructing.AConstructionManager;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ADetectorRequest {

    /**
     * Request quickest possible detector to be built (e.g. Comsat Station for Terran, not Science Vessel).
     */
    public static void requestDetectorQuick(APosition where) {
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

        int detectors = AConstructionManager.countExistingAndPlannedConstructions(detectorBuilding);
        System.out.println("detectors = " + detectors);

        // === Ensure parent exists ========================================

        int requiredParents = AConstructionManager.countExistingAndPlannedConstructions(detectorBuilding.getWhatIsRequired());
        if (requiredParents == 0) {
            System.out.println("Request: " + detectorBuilding.getWhatIsRequired().getShortName());
            AConstructionManager.requestConstructionOf(detectorBuilding.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================
//
        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfDetectorsNearBase = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
                    detectorBuilding, 15, base.getPosition()
            );

            for (int i = 0; i < 2 - numberOfDetectorsNearBase; i++) {
                AConstructionManager.requestConstructionOf(detectorBuilding, base.getPosition());
            }
        }

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

}
