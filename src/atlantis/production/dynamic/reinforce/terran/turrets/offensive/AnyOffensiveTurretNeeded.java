package atlantis.production.dynamic.reinforce.terran.turrets.offensive;

import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class AnyOffensiveTurretNeeded {
    public HasPosition getTurretNeededHere() {
        HasPosition tank = reinforceTankUnderAirAttack();

        return tank != null && !alreadyHaveTurretNear(tank) ? tank : null;
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

    private void haveTurretNear(HasPosition position) {
        if (alreadyHaveTurretNear(position)) return;

//        System.err.println("enqueue turret");
//
//        CurrentProductionQueue.print("PRE");

        AddToQueue.withHighPriority(Terran_Missile_Turret, position.position());

//        CurrentProductionQueue.print(null);
//        ConstructionRequests.requestConstructionOf(Terran_Missile_Turret, position.position());
    }

    private boolean alreadyHaveTurretNear(HasPosition position) {
        return Count.ourWithUnfinished(Terran_Missile_Turret, position, 7) > 0;
    }
}
