package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.position.APosition;
import atlantis.production.requests.AAntiAirBuildingRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranBunker {

    public static final AUnitType bunker = AUnitType.Terran_Bunker;

    public static boolean handleOffensiveBunkers() {
        if (!Have.barracks()) {
            return false;
        }

        if (handleMissionContain()) {
            return true;
        }

//        if (handleReinforceMissionAttack()) {
//            return true;
//        }

        return false;
    }

    // =========================================================

//    private static boolean handleReinforceMissionAttack() {
//        if (!Missions.isGlobalMissionAttack()) {
//            return false;
//        }
//
//        APosition squadCenter = Squad.alphaCenter();
//        if (squadCenter == null) {
//            return false;
//        }
//
//        boolean hasTurretNearby = Select.ourOfTypeIncludingUnfinished(AUnitType.Terran_Bunker)
//                .inRadius(13, squadCenter).atLeast(1);
//        if (!hasTurretNearby) {
//            AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(squadCenter);
//        }
//    }

    private static boolean handleMissionContain() {
        if (!Missions.isGlobalMissionContain()) {
            return false;
        }

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null) {
            return false;
        }

        if (!Have.existingOrPlanned(bunker, focusPoint, 9)) {
            AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(focusPoint);
            return true;
        }

        return false;
    }

}
