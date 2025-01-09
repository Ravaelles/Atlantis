package tests.unit;

import atlantis.game.A;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.ProtossStrategies;
import atlantis.production.constructions.builders.TravelToConstruct;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;

public class TravelToConstructTest extends WorldStubForTests {
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public AStrategy initBuildOrder() {
        return ProtossStrategies.PROTOSS_Forge_FE_vZ;
    }

    @Test
    public void forgeFE_travelToConstructForFirstPylon() {
        final FakeUnit worker;
        FakeUnit[] our = fakeOurs(
            fake(AUnitType.Protoss_Nexus, 8),
            fake(AUnitType.Protoss_Probe, 10),
            (worker = fake(AUnitType.Protoss_Probe, 12))
        );

        createWorld(2, () -> {
                if (A.now() <= 1) {
                    currentSupplyUsed = 8;
                }
                else {
                    currentSupplyUsed = 10;
                }

                System.err.println("=========== SUPPLY USED: " + currentSupplyUsed + " ===========");

                ProductionOrder pylonOrder = CurrentBuildOrder.get().productionOrders().get(0);
                assert pylonOrder.unitType().isPylon();

                TravelToConstruct service = new TravelToConstruct(worker);

                ArrayList<AUnitType> buildings = new ArrayList<>();
                buildings.add(Protoss_Pylon);
                buildings.add(Protoss_Forge);
                buildings.add(Protoss_Photon_Cannon);
                buildings.add(Protoss_Gateway);

                for (AUnitType building : buildings) {
                    System.err.println("===== For " + building);
                    int mineralsNeeded = service.needThisMineralsForLongDistanceConstructionTravel(
                        20, Protoss_Pylon, pylonOrder
                    );
                    System.err.println("Minerals needed: " + mineralsNeeded);
//                    for (int minerals = 0; minerals <= 90; minerals += 10) {
//                    }
                }
            },
            () -> our, () -> new FakeUnit[0]
//            Options.create().set("supplyUsed", 33).set("supplyTotal", 44)
        );
    }

    @Test
    public void forgeFE_travelToConstructForGateway() {
        final FakeUnit worker;
        FakeUnit[] our = fakeOurs(
            fake(AUnitType.Protoss_Nexus, 8),
            fake(AUnitType.Protoss_Probe, 10),
            (worker = fake(AUnitType.Protoss_Probe, 12)),
            fake(Protoss_Pylon, 20),
            fake(Protoss_Forge, 21)
        );

        createWorld(2, () -> {
                Queue.get().refresh();

                if (A.now() <= 1) {
                    currentSupplyUsed = 8;
                }
                else {
                    currentSupplyUsed = 10;
                }

                System.err.println("=========== SUPPLY USED: " + currentSupplyUsed + " ===========");

//                ProductionOrder gatewayOrder = CurrentBuildOrder.get().productionOrders().get(0);
                Orders nextOrders = Queue.get().notFinishedNext30();
                nextOrders.print("Next orders assuming we have Pylon and Forge");

                ProductionOrder gatewayOrder = nextOrders.ofType(Protoss_Gateway).first();
                assert gatewayOrder.unitType().isGateway();

                TravelToConstruct service = new TravelToConstruct(worker);

                ArrayList<AUnitType> buildings = new ArrayList<>();
//                buildings.add(Protoss_Pylon);
//                buildings.add(Protoss_Forge);
                buildings.add(Protoss_Photon_Cannon);
                buildings.add(Protoss_Gateway);

                for (AUnitType building : buildings) {
                    System.err.println("===== For " + building);
                    int mineralsNeeded = service.needThisMineralsForLongDistanceConstructionTravel(
                        20, Protoss_Gateway, gatewayOrder
                    );
                    System.err.println("Minerals needed: " + mineralsNeeded);
//                    for (int minerals = 0; minerals <= 90; minerals += 10) {
//                    }
                }
            },
            () -> our, () -> new FakeUnit[0]
//            Options.create().set("supplyUsed", 33).set("supplyTotal", 44)
        );
    }

//    @Test
//    public void OLDtravelToConstructForFirstPylon() {
//        final FakeUnit worker;
//        FakeUnit[] our = fakeOurs(
//            fake(AUnitType.Protoss_Nexus, 8),
//            fake(AUnitType.Protoss_Probe, 10),
//            (worker = fake(AUnitType.Protoss_Probe, 12))
//        );
//
//        createWorld(10, () -> {
//                currentMinerals = A.now() * 5;
//                currentSupplyUsed = 7;
////                currentSupplyUsed = 6;
//
////                System.err.println("CurrentBuildOrder.get() = " + CurrentBuildOrder.get());
////                CurrentBuildOrder.get().print();
//                ProductionOrder pylonOrder = CurrentBuildOrder.get().productionOrders().get(0);
//                assert pylonOrder.unitType().isPylon();
//
////                initQueue(A.fr * 5, 0);
//
////                currentMinerals = (A.fr - 1) * 5;
////
////                aGame.when(AGame::minerals).thenAnswer(invocation -> currentMinerals());
//
//
//                A.println("Frame: " + A.now());
//                A.println("Minerals: " + A.minerals());
//                A.println("Sup used: " + A.supplyUsed());
//
//                IsReadyToProduceOrder readyToProduceService = new IsReadyToProduceOrder();
//                boolean isApprxReady = readyToProduceService.check(pylonOrder);
//
//                System.err.println("isApprxReady = " + isApprxReady);
//
//                if (isApprxReady) {
//                    TravelToConstruct service = new TravelToConstruct(worker);
//                    int mineralsNeeded = service.needThisMineralsForLongDistanceConstructionTravel(
//                        20, Protoss_Pylon, pylonOrder
//                    );
//
//                    System.err.println("mineralsNeeded = " + mineralsNeeded);
//                }
//
//
////                int minerals = service.needThisMineralsForLongDistanceConstructionTravel(25, Protoss_Pylon);
////                System.out.println(minerals);
////            Select.our().print();
//            },
//            () -> our, () -> new FakeUnit[0],
//            Options.create().set("supplyUsed", 33).set("supplyTotal", 44)
//        );
//
//        A.errPrintln("Test finished");
//    }

}
//};
