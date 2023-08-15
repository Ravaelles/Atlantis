package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.requests.AntiAirBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class TerranTurret extends AntiAirBuildingManager {

    protected final AUnitType turret = AUnitType.Terran_Missile_Turret;

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
        if (position == null) return false;

        if (!AGame.canAffordWithReserved(75, 0)) return false;

        if (Count.existingOrInProductionOrInQueue(type()) >= 3) return false;

        if (!Have.existingOrPlannedOrInQueue(type(), position, inRadius)) {
            AddToQueue.withTopPriority(type(), position).setMaximumDistance(8);
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(position);
            return true;
        }

        return false;
    }

    public boolean exceededExistingAndInProduction() {
        if (Count.inProductionOrInQueue(type()) >= 3) return false;

        int existing = Count.existingOrInProductionOrInQueue(type());
        if (existing >= 12) return true;

        if (existing >= 6) {
            if (!A.hasMinerals(250)) {
                return true;
            }
        }

        return false;
    }

    // =========================================================

    public static TerranTurret get() {
        if (instance == null) {
            return (TerranTurret) (instance = new TerranTurret());
        }

        return (TerranTurret) instance;
    }
}
