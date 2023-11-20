package atlantis.production.dynamic.reinforce.terran.turrets.here;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class OffensiveTurretsCommander extends Commander {
    @Override
    public boolean applies() {
//        if (true) return false; // @TODO DISABLED

        return turretNeeded();
//            && EnemyInfo.hasLotOfAirUnits();
    }

    private static boolean turretNeeded() {
        return We.terran()
            && (A.hasMinerals(200) || A.seconds() >= 400)
            && A.everyNthGameFrame(67)
            && Have.engBay()
            && Count.inProductionOrInQueue(Terran_Missile_Turret) <= (A.hasMinerals(500) ? 2 : 0);
    }

    @Override
    protected void handle() {
        HasPosition position = (new AnyOffensiveTurretNeeded()).getTurretNeededHere();

        if (position != null) {
            System.err.println("@ " + A.now() + " - OffensiveTurretAdded");
            AddToQueue.withHighPriority(Terran_Missile_Turret, position.position());
        }
    }
}
