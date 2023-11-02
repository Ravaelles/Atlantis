package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Bunker;
import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class ReinforceBunkersWithTurrets extends Commander {

    public static final int MAX_DISTANCE_FROM_BUNKER = 6;

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(111) && Have.engBay() && Count.bunkers() > 0
            && Count.inProductionOrInQueue(Terran_Missile_Turret) <= 1;
    }

    @Override
    protected void handle() {
        for (AUnit bunker : Select.ourOfType(Terran_Bunker).list()) {
            reinforceBunkerWithTurrets(bunker);
        }
    }

    protected void reinforceBunkerWithTurrets(AUnit bunker) {
        Selection turretsNear = Select.ourWithUnfinishedOfType(Terran_Missile_Turret).inRadius(6, bunker);
        int optimalTurrets = 2;

        if (turretsNear.count() < optimalTurrets) {
            ProductionOrder order = AddToQueue.withStandardPriority(Terran_Missile_Turret, bunker.position());
            if (order != null) order.setMaximumDistance(MAX_DISTANCE_FROM_BUNKER);
        }
    }
}
