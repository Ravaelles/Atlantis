package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceTurretForBunker {
    private static APosition bestPosition;

    public static boolean produce() {
        if ((bestPosition = shouldProduceAt()) == null) return false;

        if (!Have.engBayWithUnfinished()) {
            return AddToQueue.maxAtATime(Terran_Engineering_Bay, 1, ProductionOrderPriority.HIGH) != null;
        }

//        A.errPrintln("$$$$$$$$$$$$$$$$$$$$$$$$$ ProduceCannonAtNatural: Requested Photon Cannon at " + A.minSec() + " / " + bestPosition);

        return requestAtBestPosition();
    }

    private static boolean requestAtBestPosition() {
        ProductionOrder order = AddToQueue.withTopPriority(type(), bestPosition);
        if (order == null) return false;

        order.setAroundPosition(bestPosition);
        order.setMaximumDistance(5);
        return true;
    }

    private static APosition shouldProduceAt() {
//        if (Count.bunkersWithUnfinished() <= 0) return null;
        if (Count.inProductionOrInQueue(type()) >= 3) return null;
        if (Enemy.terran()) return null;
        if (A.supplyUsed() <= (Enemy.zerg() ? 25 : 42)) return null;

        APosition position = bestPosition();
        if (position == null) return null;

        if (Count.existingOrPlannedBuildingsNear(type(), 7, position) >= max()) return null;

//        System.out.println(A.minSec() + " ########### requested TURRET FOR BUNKER at " + position);

        return position;

//        return ConstructionRequests.countExistingAndPlannedInRadius(type(), 6, position) < max()
//            ? position : null;
    }

    private static int radius() {
        return 8;
    }

    private static int max() {
        return EnemyInfo.hasHiddenUnits()
            ? 3
            : A.whenEnemyProtossTerranZerg(1, 0, 2);
    }

    private static APosition bestPosition() {
        HasPosition naturalChoke = Chokes.naturalOrAnyBuilding();

        Selection bunkers = Select.ourOfTypeWithUnfinished(Terran_Bunker).inRadius(7, naturalChoke);
        HasPosition bunker = bunkers.nearestTo(naturalChoke);

//        if (bunker == null) {
//            AChoke mainChoke = Chokes.mainChoke();
//            if (mainChoke == null) return null;
//
//            bunker = mainChoke.translateTilesTowards(5, Select.mainOrAnyBuilding());
//        }

        if (bunker == null) return null;

        double moveTilesToNaturalChoke = bunkers.size() <= 0 ? -0.9 : -0.2;

        return bunker.translateTilesTowards(moveTilesToNaturalChoke, naturalChoke);
    }

    private static AUnitType type() {
        return Terran_Missile_Turret;
    }
}
