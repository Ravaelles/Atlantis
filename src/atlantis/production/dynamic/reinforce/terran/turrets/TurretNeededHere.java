package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class TurretNeededHere extends Commander {
    @Override
    public boolean applies() {
//        if (true) return false; // @TODO DISABLED

        return We.terran()
            && (A.hasMinerals(200) || A.seconds() >= 400)
            && A.everyNthGameFrame(67)
            && Have.engBay()
            && Count.inProductionOrInQueue(Terran_Missile_Turret) <= (A.hasMinerals(500) ? 3 : 1);
//            && EnemyInfo.hasLotOfAirUnits();
    }

    @Override
    protected void handle() {
//        System.err.println("@ " + A.now() + " - TurretNeededHere?");

        AUnit tank = reinforceTankUnderAirAttack();

        if (tank != null) {
            haveTurretNear(tank);
        }
    }

    private void haveTurretNear(AUnit unit) {
        if (alreadyHaveTurretNear(unit)) return;

//        System.err.println("enqueue turret");
//
//        CurrentProductionQueue.print("PRE");

        AddToQueue.withHighPriority(Terran_Missile_Turret, unit.position());

//        CurrentProductionQueue.print(null);
//        ConstructionRequests.requestConstructionOf(Terran_Missile_Turret, unit.position());
    }

    private boolean alreadyHaveTurretNear(HasPosition position) {
        return Count.ourOfTypeWithUnfinished(Terran_Missile_Turret, position, 7) > 0;
    }

    private HasPosition reinforceTankUnderAirAttack() {
        for (AUnit tank : Select.ourTanks().list()) {
            if (isUnderAttackByAirOrSoItSeems(tank)) {
                return tank;
            }
        }
        return null;
    }

    private boolean isUnderAttackByAirOrSoItSeems(AUnit unit) {
        Selection airEnemies;

        return unit.lastUnderAttackLessThanAgo(30 * 3)
            && unit.woundPercent() >= 10
            && (airEnemies = unit.enemiesNear().air().havingAntiGroundWeapon().inRadius(7, unit)).notEmpty()
            && unit.friendsNear().havingAntiAirWeapon().count() > airEnemies.count();
    }
}
