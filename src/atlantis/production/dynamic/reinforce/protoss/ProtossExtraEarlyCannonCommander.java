package atlantis.production.dynamic.reinforce.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.map.base.Bases;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class ProtossExtraEarlyCannonCommander extends Commander {

    private int cannonsWithUnfinished;

    @Override
    public boolean applies() {
        return We.protoss()
            && A.s <= 60 * 6
            && Count.basesWithUnfinished() >= 2
            && (cannonsWithUnfinished = Count.withPlanned(type())) <= 2
            && ConstructionRequests.countNotFinishedOfType(type()) <= (OurArmy.strength() <= 75 ? 1 : 1)
            && A.everyNthGameFrame(29)
            && (shouldReinforceVsProtoss() || shouldReinforceVsZerg());
    }

    @Override
    protected void handle() {
        requestNew();
    }

    private void requestNew() {
        AUnit cannon = Select.ourWithUnfinished(type()).last();
        ProductionOrder order = AddToQueue.withHighPriority(type());

        if (order == null) return;

        Construction construction = order.construction();
        if (construction == null) {
//            ErrorLog.printMaxOncePerMinute("ProtossExtraEarlyCannonCommander: construction is null");
            return;
        }
        construction.setNearTo(cannon);
        construction.findPositionForNewBuilding();
    }

    private static AUnitType type() {
        return AUnitType.Protoss_Photon_Cannon;
    }

    private boolean shouldReinforceVsProtoss() {
        return Enemy.protoss()
            && (
            (A.s <= 280 && EnemyUnits.zealots() >= 4)
                || (A.s <= 330 && EnemyUnits.zealots() >= 5)
                || (A.s <= 370 && EnemyUnits.zealots() >= 6)
                || EnemyInfo.goesTemplarArchives()
                || EnemyUnits.darkTemplars() > 0
                || (cannonsWithUnfinished <= 2 && OurArmy.strengthWithoutCB() <= 70)
        );
    }

    private boolean shouldReinforceVsZerg() {
        return Enemy.zerg()
            && (
            (A.s <= 280 && EnemyUnits.combatUnits() >= 11)
                || (A.s <= 340 && EnemyUnits.combatUnits() >= 14)
//                || lotsOfEnemiesNearCannon()
                || (cannonsWithUnfinished <= 2 && OurArmy.strength() <= 110)
        );
    }

    private boolean lotsOfEnemiesNearCannon() {
        if (OurArmy.strength() >= 120) return false;

        AUnit base = Bases.natural();
        if (base == null) base = Select.main();
        if (base == null) return false;

        AUnit cannon = Select.ourOfType(AUnitType.Protoss_Photon_Cannon).nearestTo(base);
        if (cannon == null) return false;

        return cannon.enemiesNear().combatUnits().atLeast(Enemy.zerg() ? 11 : 6);
    }
}
