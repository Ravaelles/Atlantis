package atlantis.production.dynamic.reinforce.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class ProtossExtraEarlyCannonCommander extends Commander {

    private int cannonsWithUnfinished;

    @Override
    public boolean applies() {
        return We.protoss()
            && A.s <= 60 * 6
            && Count.basesWithUnfinished() >= 2
            && (cannonsWithUnfinished = Count.withPlanned(type())) <= 2
            && ConstructionRequests.countNotFinishedOfType(type()) <= (Army.strength() <= 75 ? 1 : 1)
            && A.everyNthGameFrame(29)
            && (shouldReinforceVsProtoss() || shouldReinforceVsZerg());
    }

    @Override
    protected void handle() {
        requestNew();
    }

    private void requestNew() {
        HasPosition at = defineAt();
        ProductionOrder order = AddToQueue.withHighPriority(type());

        if (order == null) return;

        Construction construction = order.construction();
        if (construction == null) {
//            ErrorLog.printMaxOncePerMinute("ProtossExtraEarlyCannonCommander: construction is null");
            return;
        }

        construction.setNearTo(at);
        construction.findPositionForNewBuilding();
    }

    private static HasPosition defineAt() {
        HasPosition at = Select.ourWithUnfinished(type()).last();
        if (at != null) return at;

        return Chokes.natural().groundTranslateTowardsMain(5);
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
                || (cannonsWithUnfinished <= 2 && Army.strengthWithoutCB() <= 70)
        );
    }

    private boolean shouldReinforceVsZerg() {
        return Enemy.zerg()
            && (
            (A.s <= 280 && EnemyUnits.combatUnits() >= 11)
                || (A.s <= 340 && EnemyUnits.combatUnits() >= 14)
//                || lotsOfEnemiesNearCannon()
                || (cannonsWithUnfinished <= 2 && Army.strength() <= 110)
        );
    }

    private boolean lotsOfEnemiesNearCannon() {
        if (Army.strength() >= 120) return false;

        AUnit base = Bases.natural();
        if (base == null) base = Select.main();
        if (base == null) return false;

        AUnit cannon = Select.ourOfType(AUnitType.Protoss_Photon_Cannon).nearestTo(base);
        if (cannon == null) return false;

        return cannon.enemiesNear().combatUnits().atLeast(Enemy.zerg() ? 11 : 6);
    }
}
