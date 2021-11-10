package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.production.requests.AAntiAirBuildingRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranMissileTurret {

    public static final AUnitType turret = AUnitType.Terran_Missile_Turret;

    // =========================================================

    public static boolean handleOffensiveMissileTurrets() {
        if (!Have.engBay()) {
            return false;
        }

        if (handleReinforceMissionContain()) {
            return true;
        }

        if (handleReinforceMissionAttack()) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean handleReinforceMissionAttack() {
        if (!Missions.isGlobalMissionAttack()) {
            return false;
        }

        APosition squadCenter = Squad.alphaCenter();
        if (squadCenter == null) {
            return false;
        }

        if (!Have.existingOrPlanned(turret, squadCenter, 13)) {
            AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(squadCenter);
            return true;
        }

        return false;
    }

    private static boolean handleReinforceMissionContain() {
        if (!Missions.isGlobalMissionContain()) {
            return false;
        }

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null) {
            return false;
        }

        if (!Have.existingOrPlanned(turret, focusPoint, 9)) {
            AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(focusPoint);
            return true;
        }

        return false;
    }

}
