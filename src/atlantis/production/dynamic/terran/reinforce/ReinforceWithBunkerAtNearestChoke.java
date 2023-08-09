package atlantis.production.dynamic.terran.reinforce;

import atlantis.architecture.Commander;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ReinforceWithBunkerAtNearestChoke extends Commander {
    private HasPosition initialPositionToReinforce;
    private HasPosition positionForBunker;

    public ReinforceWithBunkerAtNearestChoke(HasPosition position) {
        this.initialPositionToReinforce = position;
    }

    @Override
    public boolean applies() {
        if (Count.barracks() <= 0) return false;
        if (Count.inProductionOrInQueue(Terran_Bunker) > 0) return false;

        positionForBunker = initialPositionToReinforce;
        AChoke choke = Chokes.nearestChoke(initialPositionToReinforce);
        if (choke != null) {
            positionForBunker = choke.translateTilesTowards(initialPositionToReinforce, 5);
        }
        if (positionForBunker == null) {
            return false;
        }

        int searchRadius = 15;
        Selection existing = Select.ourWithUnfinishedOfType(Terran_Bunker).inRadius(searchRadius, positionForBunker);
        if (existing.notEmpty()) {
            if (existing.first().position().region() == positionForBunker.position().region()) {
                return false;
            }
        }
        if (ConstructionRequests.hasNotStartedNear(Terran_Bunker, positionForBunker, searchRadius)) {
            return false;
        }
//        System.out.println(
//            "ZA = " + Select.ourWithUnfinishedOfType(Terran_Bunker).count()
//            + " / ZA1st = " + Select.ourWithUnfinishedOfType(Terran_Bunker).first()
//            + " / ZB = " + ConstructionRequests.notStartedOfType(Terran_Bunker).size()
//            + " ///// " +
//            "A = " + existing.count()
//            + " / " +
//            "B = " + ConstructionRequests.hasNotStartedNear(Terran_Bunker, positionForBunker, searchRadius)
//            + " / " + positionForBunker
//        );

        return positionForBunker != null;
    }

    @Override
    protected void handle() {
        System.err.println("Reinforce with BUNKER at " + positionForBunker);
        haveBunkerAtTheNearestChoke();
    }

    private void haveBunkerAtTheNearestChoke() {
        AddToQueue.withTopPriority(Terran_Bunker, positionForBunker);
    }
}
