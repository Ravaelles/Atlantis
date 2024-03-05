package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.constructing.position.FindPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.workers.FreeWorkers;

import static atlantis.units.AUnitType.Protoss_Pylon;

public class ReinforceProtossBaseWithCannons {
    private HasPosition initialPositionToReinforce;
    private HasPosition finalPosition;
    private APosition natural;

    public ReinforceProtossBaseWithCannons(HasPosition position) {
        this.initialPositionToReinforce = position;
        this.finalPosition = position;

        if (isForNatural()) {
            this.finalPosition = modifyPositionForNatural();
        }
    }

    public void reinforce() {
//        System.err.println(A.at() + " Reinforce " + initialPositionToReinforce + " with CANNON at " + finalPosition);
//        haveCannonAtTheNearestChoke();

        if (ensurePylonExists()) {
            buildACannon();
        }
    }

    private void buildACannon() {
//        System.err.println("############### GET CANNON AT " + finalPosition);
        RequestCannonAt.at(finalPosition);
    }

    private boolean ensurePylonExists() {
        if (ConstructionRequests.countExistingAndPlannedInRadius(Protoss_Pylon, 6, finalPosition) == 0) {
            AUnit builder = FreeWorkers.get().nearestTo(finalPosition);
            APosition positionForPylon = FindPosition.findForBuilding(
                builder,
                Protoss_Pylon,
                null,
                finalPosition,
                9
            );
//            System.err.println("__positionForPylon = " + positionForPylon);

            if (positionForPylon != null) {
                ProductionOrder order = AddToQueue.withTopPriority(Protoss_Pylon, positionForPylon);
                NewConstructionRequest.requestConstructionOf(Protoss_Pylon, positionForPylon, order);

                if (order != null) {
                    order.forceSetPosition(positionForPylon);
                    order.setMinSupply(0);

//                    System.err.println("---------- HAVE PYLON AT " + positionForPylon + " / " + positionForPylon.isBuildable());
                    //                CameraCommander.centerCameraOn(positionForPylon);
                }

                return false;
            }

//            ProductionOrder order = AddToQueue.withTopPriority(Protoss_Pylon, null);
//
//            if (order != null) {
//                APosition positionForPylon = FindPosition.findForBuildingNear(Protoss_Pylon, finalPosition);
//                order.forceSetPosition(positionForPylon);
//
//                System.err.println("---------- HAVE PYLON AT " + positionForPylon + " / " + finalPosition + " / " + positionForPylon.distTo(finalPosition));
//                CameraCommander.centerCameraOn(positionForPylon);
//            }

            A.errPrintln("Failed to add Pylon for Cannon at " + finalPosition);
            return false;
        }

        return true;
    }

    private HasPosition modifyPositionForNatural() {
        AChoke choke = Chokes.natural();
        if (choke == null) return initialPositionToReinforce;

        return translateChokeTowardsOurSide(choke);
    }

    private APosition translateChokeTowardsOurSide(AChoke choke) {
        return initialPositionToReinforce.translateTilesTowards(choke, 3.4);
    }

//    private void haveCannonAtTheNearestChoke() {
//        AddToQueue.withTopPriority(Protoss_Photon_Cannon, finalPosition);
//    }

    private boolean isForNatural() {
        natural = DefineNaturalBase.natural();

        return natural != null && initialPositionToReinforce.distToLessThan(natural, 5);
    }
}
