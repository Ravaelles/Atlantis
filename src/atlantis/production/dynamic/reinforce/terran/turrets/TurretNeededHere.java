package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class TurretNeededHere extends Commander {
    @Override
    public boolean applies() {
        return We.terran()
            && A.hasMinerals(500)
            && A.everyNthGameFrame(83)
            && Have.engBay()
            && Count.inProductionOrInQueue(Terran_Missile_Turret) <= (A.hasMinerals(700) ? 3 : 1);
//            && EnemyInfo.hasLotOfAirUnits();
    }

    @Override
    protected void handle() {
        AUnit tank = checkIfThereAreTanksUnderAirAttack();

        if (tank != null) {
            buildNear(tank);
        }
    }

    private void buildNear(AUnit unit) {
        AddToQueue.withHighPriority(Terran_Missile_Turret, unit.position());
//        ConstructionRequests.requestConstructionOf(Terran_Missile_Turret, unit.position());
    }

    private AUnit checkIfThereAreTanksUnderAirAttack() {
        for (AUnit tank : Select.ourTanks().list()) {
            if (isUnderAttackByAirOrSoItSeems(tank)) {
                return tank;
            }
        }
        return null;
    }

    private boolean isUnderAttackByAirOrSoItSeems(AUnit unit) {
        return unit.lastUnderAttackLessThanAgo(30 * 3)
            && unit.enemiesNear().air().havingGroundWeapon().inRadius(7, unit).notEmpty();
    }
}
