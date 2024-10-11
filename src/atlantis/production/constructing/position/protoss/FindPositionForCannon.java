package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Protoss_Pylon;

public class FindPositionForCannon {
    public static APosition find(HasPosition nearTo, AUnit builder, Construction construction) {
//        System.err.println("Natural= " + DefineNaturalBase.natural() + " / Main= " + Select.main());
//        System.err.println("PRE nearTo = " + nearTo);
        nearTo = validateNearTo(nearTo);
//        System.err.println("POST nearTo = " + nearTo);
//        System.err.println("Existing CN = " + Select.ourOfType(Protoss_Photon_Cannon).first());
        builder = validateBuilder(builder, nearTo);

        if (noPylonNearby(nearTo)) {
            A.errPrintln("FindPositionForCannon: Requested pylon near " + nearTo);
            requestPylonToBeAbleToBuildCannon(nearTo);
            return null;
        }

        return ProtossPositionFinder.findStandardPositionFor(
            builder,
            Protoss_Photon_Cannon,
            nearTo,
            12
        );
    }

    private static AUnit validateBuilder(AUnit builder, HasPosition nearTo) {
        if (builder != null) return builder;

        return FreeWorkers.get().nearestTo(nearTo);
    }

    private static boolean noPylonNearby(HasPosition nearTo) {
        int radius = 8;

        return Select.ourWithUnfinished(Protoss_Pylon).inRadius(radius, nearTo).empty()
            && !ConstructionRequests.hasNotStartedNear(Protoss_Pylon, nearTo, radius);
    }

    private static void requestPylonToBeAbleToBuildCannon(HasPosition nearTo) {
        AddToQueue.withTopPriority(Protoss_Pylon, nearTo);
    }

    private static HasPosition validateNearTo(HasPosition nearTo) {
        if (nearTo != null) {
            AUnit otherNear = Select.ourOfType(Protoss_Photon_Cannon).inRadius(8, nearTo).nearestTo(nearTo);
            if (otherNear != null) return otherNear;

            return nearTo;
        }

        if (Count.basesWithUnfinished() >= 2) return forNaturalChoke();

        return Chokes.mainChoke();
    }

    private static HasPosition forNaturalChoke() {
        APosition natural = DefineNaturalBase.natural();
        if (natural == null) return null;

        AChoke naturalChoke = Chokes.natural();
        if (naturalChoke == null) return null;

        return naturalChoke.translateTilesTowards(4, natural);
    }
}
