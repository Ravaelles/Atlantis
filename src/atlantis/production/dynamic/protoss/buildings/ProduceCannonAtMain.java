package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Forge;
import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProduceCannonAtMain {
    private static APosition bestPosition;

    public static boolean produce() {
        if ((bestPosition = shouldProduceAt()) == null) return false;

        if (!Have.forge()) {
            AddToQueue.withTopPriority(Protoss_Forge);
        }

        return requestAtBestPosition();
    }

    private static boolean requestAtBestPosition() {
        return AddToQueue.withTopPriority(type(), bestPosition) != null;
    }

    private static APosition shouldProduceAt() {
        HasPosition mainChoke = Chokes.mainChoke();
//        System.err.println("--------------- mainChoke = " + mainChoke);
//        System.err.println("--------------- naturalLocation = " + DefineNaturalBase.natural());

        if (mainChoke == null) return null;
        if (Count.inProductionOrInQueue(type()) >= (A.hasMinerals(1000) ? 6 : 3)) return null;

        HasPosition atPosition = bestPosition(mainChoke);
        if (
            Count.existingOrPlannedBuildingsNear(type(), radius(), mainChoke) < max()
                && Count.existingOrPlannedBuildingsNear(type(), radius(), atPosition) < max()
        ) {
            return atPosition != null ? atPosition.position() : null;
        }

        return null;
    }

    private static int radius() {
        return 8;
    }

    private static int max() {
        return (Enemy.zerg() ? 2 : 1)
            + (Enemy.protoss() && EnemyInfo.goesOrHasHiddenUnits() ? 1 : 0)
            + (A.supplyTotal() >= 100 ? 1 : 0)
            + (A.minerals() / 750);
    }

    private static HasPosition bestPosition(HasPosition at) {
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) {
            return at;
        }

        return at
            .translateTilesTowards(5, mainChoke)
            .translatePercentTowards(20, mainChoke);
    }

    private static AUnitType type() {
        return Protoss_Photon_Cannon;
    }
}
