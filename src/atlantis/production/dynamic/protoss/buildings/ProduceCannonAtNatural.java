package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.Protoss_Forge;
import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ProduceCannonAtNatural {
    private static APosition bestPosition;

    public static boolean produce() {
        if ((bestPosition = shouldProduceAt()) == null) return false;

        if (!Have.forge()) {
            AddToQueue.withTopPriority(Protoss_Forge);
        }

//        A.errPrintln("$$$$$$$$$$$$$$$$$$$$$$$$$ ProduceCannonAtNatural: Requested Photon Cannon at " + A.minSec() + " / " + bestPosition);

        return requestAtBestPosition();
    }

    private static boolean requestAtBestPosition() {
        return AddToQueue.withTopPriority(type(), bestPosition) != null;
    }

    private static APosition shouldProduceAt() {
//        if (A.everyFrameExceptNthFrame(53)) return null;
//        if (Count.basesWithUnfinished() <= 1) return null;

//        if (Count.inProduction(Protoss_Photon_Cannon) >= 2) return false;

//        if (ProtossSecureBasesCommander.invoke()) return true;

//        AUnit natural = Bases.natural();
//        System.err.println("--------------- natural = " + natural);

//        APosition naturalChoke = DefineNaturalBase.natural();
        HasPosition naturalChoke = Chokes.natural();
//        System.err.println("--------------- naturalChoke = " + naturalChoke);
//        System.err.println("--------------- naturalLocation = " + DefineNaturalBase.natural());

        if (naturalChoke == null) return null;
        if (Count.inProductionOrInQueue(type()) >= (A.hasMinerals(1000) ? 6 : 3)) return null;

        APosition atPosition = bestPosition(naturalChoke);
        if (
            Count.existingOrPlannedBuildingsNear(type(), radius(), naturalChoke) < max()
                && Count.existingOrPlannedBuildingsNear(type(), radius(), atPosition) < max()
        ) {
            return atPosition;
        }

        return null;
    }

    private static int radius() {
        return 8;
    }

    private static int max() {
        return (Enemy.zerg() ? 2 : 1)
            + (A.supplyTotal() >= 100 ? 1 : 0)
            + (A.minerals() / 750);
    }

    private static APosition bestPosition(HasPosition naturalBase) {
        AChoke naturalChoke = Chokes.natural();
        if (naturalChoke == null) {
            return naturalBase.position();
        }

        return naturalBase
            .translateTilesTowards(5, naturalChoke)
            .translatePercentTowards(20, naturalChoke);
    }

    private static AUnitType type() {
        return Protoss_Photon_Cannon;
    }
}
