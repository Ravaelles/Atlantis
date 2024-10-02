package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
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

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Protoss_Pylon;

public class NewCannonPositionFinder {
    public static APosition find(HasPosition nearTo, AUnit builder, Construction construction) {
        nearTo = validateNearTo(nearTo);

        if (noCannonNearby(nearTo)) {
            A.errPrintln("NewCannonPositionFinder: Requested pylon near " + nearTo);
            requestPylonToBeAbleToBuildCannon(nearTo);
            return null;
        }

        return ProtossPositionFinder.findStandardPositionFor(
            builder,
            Protoss_Photon_Cannon,
            nearTo,
            10
        );
    }

    private static boolean noCannonNearby(HasPosition nearTo) {
        return Select.ourWithUnfinished(Protoss_Pylon).inRadius(8, nearTo).empty()
            && !ConstructionRequests.hasNotStartedNear(Protoss_Photon_Cannon, nearTo, 8);
    }

    private static void requestPylonToBeAbleToBuildCannon(HasPosition nearTo) {
        AddToQueue.withTopPriority(Protoss_Pylon, nearTo);
    }

    private static HasPosition validateNearTo(HasPosition nearTo) {
        if (nearTo != null) return nearTo;

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
