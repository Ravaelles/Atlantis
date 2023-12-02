package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.AntiAirBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class TerranMissileTurret extends AntiAirBuildingCommander {

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
            ProductionOrder order = AddToQueue.withTopPriority(type(), position);
            if (order != null) order.setMaximumDistance(8);
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(position);
//            System.err.println("@ " + A.now() + " - MISSILE TURRET ENQUEUED = "
//                + Have.existingOrPlannedOrInQueue(type(), position, inRadius));
            return order != null;
        }

        return false;
    }

    public boolean exceededExistingAndInProduction() {
        if (Count.withPlanned(type()) >= 2) return true;

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

    public static TerranMissileTurret get() {
        if (instance == null) {
            return (TerranMissileTurret) (instance = new TerranMissileTurret());
        }

        return (TerranMissileTurret) instance;
    }
}
