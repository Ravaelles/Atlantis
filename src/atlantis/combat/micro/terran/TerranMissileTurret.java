package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
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

        if (handleReinforcePosition(turretForNatural(), 9)) {
            System.out.println("Requested TURRET for NATURAL");
            return true;
        }

        if (Missions.isGlobalMissionContain() && handleReinforceMissionContain()) {
            System.out.println("Requested TURRET for contain");
            return true;
        }

//        if (Missions.isGlobalMissionAttack() && handleReinforceMissionAttack()) {
//            System.out.println("Requested TURRET for attack");
//            return true;
//        }

        return false;
    }

    private static HasPosition turretForNatural() {
        APosition natural = Bases.natural();
        if (natural == null) {
            return null;
        }

        return natural.translateTilesTowards(Chokes.nearestChoke(natural), 10);
    }

    // =========================================================

    private static boolean handleReinforceMissionAttack() {
        APosition squadCenter = Squad.alphaCenter();
        if (squadCenter == null) {
            return false;
        }

        return handleReinforcePosition(squadCenter, 14);
    }

    private static boolean handleReinforceMissionContain() {
        APosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null || !Have.main()) {
            return false;
        }

        return handleReinforcePosition(containReinforcePoint(focusPoint), 9);
    }

    private static HasPosition containReinforcePoint(APosition focusPoint) {
        APosition point = focusPoint;
        AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();

        if (enemyBuilding != null) {
//            point = point.translateTilesTowards(-6, enemyBuilding);
            AChoke choke = Chokes.nearestChoke(enemyBuilding);
            if (choke != null) {
                point = point.translateTilesTowards(-10, enemyBuilding);
            }
        }

        return point;
    }

    private static boolean handleReinforcePosition(HasPosition position, double inRadius) {
        if (!AGame.canAffordWithReserved(75, 0)) {
            return false;
        }

        if (!Have.existingOrPlannedOrInQueue(turret, position, inRadius)) {
            AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(position);
            return true;
        }

        return false;
    }

}
