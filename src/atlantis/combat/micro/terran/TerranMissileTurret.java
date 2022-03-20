package atlantis.combat.micro.terran;

import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.requests.AntiAirBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class TerranMissileTurret extends AntiAirBuildingManager {

    protected static final AUnitType turret = AUnitType.Terran_Missile_Turret;

    @Override
    public AUnitType type() {
        return AUnitType.Terran_Missile_Turret;
    }

    @Override
    public int expected() {
        return 0;
    }

    // =========================================================

    protected boolean handleReinforcePosition(HasPosition position, double inRadius) {
        if (!AGame.canAffordWithReserved(75, 0)) {
            return false;
        }

        if (!Have.existingOrPlannedOrInQueue(type(), position, inRadius)) {
            AddToQueue.withTopPriority(type(), position).setMaximumDistance(8);
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(position);
            return true;
        }

        return false;
    }

    // =========================================================

    public static TerranMissileTurret get() {
        if (instance == null) {
            return (TerranMissileTurret) (instance = new TerranMissileTurret());
        }

        return (TerranMissileTurret) instance;
    }
}
