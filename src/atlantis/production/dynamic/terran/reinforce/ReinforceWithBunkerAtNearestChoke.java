package atlantis.production.dynamic.terran.reinforce;

import atlantis.architecture.Commander;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
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

        APosition natural = Bases.natural();
        if (natural != null && position.distToLessThan(natural, 6)) {
            this.initialPositionToReinforce = natural;
        }
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

        Selection bunkersWithUnfinished = Select.ourWithUnfinishedOfType(Terran_Bunker);

        if (bunkersWithUnfinished.count() >= (Count.basesWithUnfinished())) return false;

        int searchRadius = 7;
        Selection existing = bunkersWithUnfinished.inRadius(searchRadius, positionForBunker);
        if (existing.notEmpty()) {
            if (
                existing.first().position().region() == positionForBunker.position().region()
                    || existing.inRadius(searchRadius, positionForBunker).notEmpty()
            ) {
                return false;
            }
        }
        if (ConstructionRequests.hasNotStartedNear(Terran_Bunker, positionForBunker, searchRadius)) {
            return false;
        }

//        System.out.println(
//            "Existing = " + bunkersWithUnfinished.count()
//                + " / OneOfWhich = " + bunkersWithUnfinished.first()
//                + " / NotStarted = " + ConstructionRequests.notStartedOfType(Terran_Bunker).size()
//                + " ///// " +
//                "ExistCount = " + existing.count()
//                + " / " +
//                "NotStartCount = " + ConstructionRequests.hasNotStartedNear(Terran_Bunker, positionForBunker,
//                searchRadius)
//                + " / @ " + positionForBunker
//        );

        return positionForBunker != null;
    }

    @Override
    protected void handle() {
        System.out.println("Reinforce " + initialPositionToReinforce + " with BUNKER at " + positionForBunker);
        haveBunkerAtTheNearestChoke();
    }

    private void haveBunkerAtTheNearestChoke() {
        AddToQueue.withTopPriority(Terran_Bunker, positionForBunker);
    }
}
