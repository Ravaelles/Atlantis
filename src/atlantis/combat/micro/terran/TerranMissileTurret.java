package atlantis.combat.micro.terran;

import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class TerranMissileTurret {

    public static final AUnitType turret = AUnitType.Terran_Missile_Turret;

    // =========================================================

    protected static boolean handleReinforcePosition(HasPosition position, double inRadius) {
        if (!AGame.canAffordWithReserved(75, 0)) {
            return false;
        }

        if (!Have.existingOrPlannedOrInQueue(turret, position, inRadius)) {
            AddToQueue.withTopPriority(turret, position) .setMaximumDistance(8);
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(position);
            return true;
        }

        return false;
    }

}
