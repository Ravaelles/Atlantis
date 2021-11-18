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
import atlantis.production.orders.AddToQueue;
import atlantis.production.requests.AAntiAirBuildingRequests;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranMissileTurret {

    public static final AUnitType turret = AUnitType.Terran_Missile_Turret;

    // =========================================================

    protected static boolean handleReinforcePosition(HasPosition position, double inRadius) {
        if (!AGame.canAffordWithReserved(75, 0)) {
            return false;
        }

        if (!Have.existingOrPlannedOrInQueue(turret, position, inRadius)) {
            AddToQueue.withTopPriority(turret, position) .setMaximumDistance(8);
//            AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(position);
            return true;
        }

        return false;
    }

}
