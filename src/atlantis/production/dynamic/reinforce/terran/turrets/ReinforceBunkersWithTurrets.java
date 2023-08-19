package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.architecture.Commander;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Bunker;
import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class ReinforceBunkersWithTurrets extends Commander {
    @Override
    public boolean applies() {
        return Have.engBay() && Count.bunkers() > 0 && Count.inProductionOrInQueue(Terran_Missile_Turret) <= 1;
    }

    @Override
    protected void handle() {
        for (AUnit bunker : Select.ourOfType(Terran_Bunker).list()) {
            reinforceBunkerWithTurrets(bunker);
        }
    }

    protected void reinforceBunkerWithTurrets(AUnit bunker) {
        Selection turretsNear = Select.ourOfType(Terran_Missile_Turret).inRadius(6, bunker);
        int optimalTurrets = 2;

        if (turretsNear.count() < optimalTurrets) {
            AddToQueue.withStandardPriority(Terran_Missile_Turret, bunker.position());
        }
    }
}
