package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;

import static atlantis.units.AUnitType.Terran_Bunker;

public class ReinforceWithBunkerAtNearestChoke extends Commander {
    private HasPosition initialPositionToReinforce;
    private HasPosition position;
    private APosition natural;

    protected ReinforceWithBunkerAtNearestChoke(HasPosition position) {
        this.initialPositionToReinforce = position;

//        if (isForNatural()) {
//            this.initialPositionToReinforce = natural;
//        }
    }

//    @Override
//    public boolean applies() {
////        if (true) return false;
//
//        if (Count.barracks() <= 0) return false;
//        if (Count.inProductionOrInQueue(Terran_Bunker) > 0) return false;
////        if (Count.unf(Terran_Bunker) > 0) return false;
//
//        position = initialPositionToReinforce;
//        AChoke choke = Chokes.nearestChoke(initialPositionToReinforce, "MAIN");
//
//        if (choke != null) {
//            translateChokeTowardsOurSide(choke);
//        }
//
//        if (position == null) return false;
//        if (Select.ourWithUnfinishedOfType(Terran_Bunker).inRadius(4, position).notEmpty()) return false;
//
//        int searchRadius = 7;
//        if (ConstructionRequests.hasNotStartedNear(Terran_Bunker, position, searchRadius)) return false;
//
//        Selection bunkersWithUnfinished = Select.ourWithUnfinishedOfType(Terran_Bunker);
//
//        int maxBunkers = Enemy.terran() ? 1 : Count.basesWithUnfinished() + 2;
//        if (bunkersWithUnfinished.count() >= maxBunkers) return false;
//
//        Selection existing = bunkersWithUnfinished.inRadius(searchRadius, position);
//        if (existing.notEmpty()) {
//            if (
//                existing.first().position().region() == position.position().region()
//                    || existing.inRadius(searchRadius, position).notEmpty()
//            ) return false;
//        }
//
//
////            "Existing = " + bunkersWithUnfinished.count()
////                + " / OneOfWhich = " + bunkersWithUnfinished.first()
////                + " / NotStarted = " + ConstructionRequests.notStartedOfType(Terran_Bunker).size()
////                + " ///// " +
////                "ExistCount = " + existing.count()
////                + " / " +
////                "NotStartCount = " + ConstructionRequests.hasNotStartedNear(Terran_Bunker, position,
////                searchRadius)
////                + " / @ " + position
////        );
//
//        return position != null;
//    }

//    private void translateChokeTowardsOurSide(AChoke choke) {
//        position = choke.translateTilesTowards(initialPositionToReinforce, 3.4);
//    }

//    @Override
//    protected boolean handle() {
//        System.err.println("Reinforce " + initialPositionToReinforce + " with BUNKER at " + position);
//        if (requestBunkerAt(position)) {
//            System.out.println("...OK");
//            return true;
//        }
//            System.out.println("...failp");
//
//        return false;
//    }

//    private boolean requestBunkerAt(HasPosition position) {
//        return AddToQueue.withHighPriority(Terran_Bunker, position) != null;
//    }

//    private boolean isForNatural() {
//        natural = DefineNaturalBase.natural();
//
//        return natural != null && initialPositionToReinforce.distToLessThan(natural, 6);
//    }
}
