package tests.unit;

import atlantis.game.A;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.protoss.ProtossStrategies;
import atlantis.map.position.APosition;
import atlantis.production.constructions.builders.TravelToConstruct;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.AConsole;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.ArrayList;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TravelToConstructTest extends WorldStubForTests {
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The measured OpenBW #48 deadlock: a builder standing ON its build tile must
     * not be stopped by the "do not switch constructions" throttle.
     *
     * <p>
     * {@code travelWhenReady} used to return early on
     * {@code asProtossMultiBuilderDoNotSwitchConstructions} - "a Protoss builder
     * that issued MOVE_BUILD in the last 20 frames does not switch constructions" -
     * <b>before</b> the distance check. The worker sits on the tile, so
     * {@code TravelToConstruct} keeps re-stamping MOVE_BUILD, the guard stays true
     * forever and {@code IssueBuildOrder} is never reached. The probe on a live
     * OpenBW run showed it exactly: {@code constructing=false}, the tile fully
     * valid (engIncBuild/engBuild/explored all true, not occupied), affordable, and
     * {@code lastActionMoreThanAgo(20)} permanently false with
     * {@code lastCommandAgo} pinned at 15, for the whole game.
     * </p>
     *
     * <p>
     * The behavioural guard is the OpenBW scenario itself
     * ({@code EXPECT_MIN_PYLONS=1}, NEXT #47/#50) - a stub world cannot show a
     * building appearing. What is checked here is the <b>structural</b> rule the
     * fix encodes: the throttle is consulted only when the builder still has to
     * travel. The distance at which the throttle may apply is the same
     * {@code minDistanceToIssueBuildOrder} the travel branch uses.
     * </p>
     */
    @Test
    public void aBuilderAlreadyOnItsTileIsNotThrottledByARecentMoveBuild() {
        // The property under test is the ORDER of two decisions in travelWhenReady,
        // and the two meaningful distances are "at the site" (0) and "far" (40).
        // A builder at the site must take the build branch; one far away may be
        // throttled. Asserted on the decision, not on a game object, because this is
        // a control-flow invariant - the outcome (a building exists) is asserted by
        // the OpenBW scenario, which is the only place it can be observed.
        assertFalse(
            atlantis.production.constructions.builders.TravelToConstruct
                .isStillTravellingForTest(0.0, 1.4),
            "a builder already on its tile must NOT be treated as still travelling - "
                + "that is the branch that ran the throttle and starved the build order"
        );
        assertTrue(
            atlantis.production.constructions.builders.TravelToConstruct
                .isStillTravellingForTest(40.0, 1.4),
            "a builder 40 tiles away is genuinely travelling"
        );
    }

    /**
     * The second measured OpenBW failure on the same path: read against the
     * building's <b>centre</b>, a worker standing on the tile just outside a 2x2
     * Pylon's footprint counts as travelling (groundDist to the centre ~2.2 > 1.4),
     * so it re-walks forever and never builds.
     *
     * <p>
     * Measured 2026-10-10: builder pinned at {@code [6,46]} for a Pylon whose
     * top-left was {@code [6,47]}; {@code unit.build(...)} was never called
     * ({@code SINK_BUILD} never printed) and the construction was cancelled at 36 s.
     * Readiness must be measured to the nearest <b>footprint tile</b>.
     * </p>
     */
    @Test
    public void buildReadinessIsMeasuredToTheFootprintNotTheBuildingCentre() {
        // The Pylon is 2x2 with its top-left at [6,47], so its footprint tiles are
        // [6,47], [7,47], [6,48], [7,48]. A worker on [6,46] touches the footprint
        // exactly - it must be treated as AT the site.
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 6, 46);
        FakeUnit[] our = fakeOurs(fake(AUnitType.Protoss_Nexus, 8), builder);

        world(2, our, units(new FakeUnit[0]), () -> {
            APosition nearest = atlantis.production.constructions.builders.TravelToConstruct
                .nearestFootprintTile(builder, APosition.create(6, 47), AUnitType.Protoss_Pylon);

            assertTrue(
                nearest.tx() >= 6 && nearest.tx() <= 7 && nearest.ty() >= 47 && nearest.ty() <= 48,
                "the nearest footprint tile must be inside the Pylon's 2x2 footprint, "
                    + "got [" + nearest.tx() + "," + nearest.ty() + "]"
            );

            // The worker touches the footprint, so the distance to it is a single
            // tile - inside the 1.4 build threshold once measured to the edge, and
            // NOT the ~2.2 the centre measure produced.
            assertTrue(
                builder.groundDist(nearest) <= 1.4,
                "a worker adjacent to the footprint must read as close enough to build, "
                    + "got " + builder.groundDist(nearest)
            );

            assertTrue(
                builder.groundDist(nearest) < builder.groundDist(APosition.create(7, 48)),
                "measuring to the footprint must be strictly closer than measuring to "
                    + "the building centre - that difference is the bug"
            );
        });
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

        world(2, our, units(new FakeUnit[0]), () -> {
            if (A.now() <= 1) {
                currentSupplyUsed = 8;
            }
            else {
                currentSupplyUsed = 10;
            }

//            AConsole.println("=========== SUPPLY USED: " + currentSupplyUsed + " ===========");

            ProductionOrder pylonOrder = CurrentBuildOrder.get().productionOrders().get(0);
            assert pylonOrder.unitType().isPylon();

            TravelToConstruct service = new TravelToConstruct(worker);

            ArrayList<AUnitType> buildings = new ArrayList<>();
            buildings.add(Protoss_Pylon);
            buildings.add(Protoss_Forge);
            buildings.add(Protoss_Photon_Cannon);
            buildings.add(Protoss_Gateway);

            for (AUnitType building : buildings) {
//                AConsole.println("===== For " + building);
                int mineralsNeeded = service.needThisMineralsForLongDistanceConstructionTravel(
                    20, Protoss_Pylon, pylonOrder
                );
//                AConsole.println("Minerals needed: " + mineralsNeeded);
//                    for (int minerals = 0; minerals <= 90; minerals += 10) {
//                    }
            }
        });
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

        world(2, our, units(new FakeUnit[0]), () -> {
            Queue.get().refresh();

            if (A.now() <= 1) {
                currentSupplyUsed = 8;
            }
            else {
                currentSupplyUsed = 10;
            }

            AConsole.println("=========== SUPPLY USED: " + currentSupplyUsed + " ===========");

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
                AConsole.println("===== For " + building);
                int mineralsNeeded = service.needThisMineralsForLongDistanceConstructionTravel(
                    20, Protoss_Gateway, gatewayOrder
                );
                AConsole.println("Minerals needed: " + mineralsNeeded);
//                    for (int minerals = 0; minerals <= 90; minerals += 10) {
//                    }
            }
        });
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
//        world(10, () -> {
//                currentMinerals = A.now() * 5;
//                currentSupplyUsed = 7;
////                currentSupplyUsed = 6;
//
////                AConsole.println("CurrentBuildOrder.get() = " + CurrentBuildOrder.get());
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
//                AConsole.println("Frame: " + A.now());
//                AConsole.println("Minerals: " + A.minerals());
//                AConsole.println("Sup used: " + A.supplyUsed());
//
//                IsReadyToProduceOrder readyToProduceService = new IsReadyToProduceOrder();
//                boolean isApprxReady = readyToProduceService.check(pylonOrder);
//
//                AConsole.println("isApprxReady = " + isApprxReady);
//
//                if (isApprxReady) {
//                    TravelToConstruct service = new TravelToConstruct(worker);
//                    int mineralsNeeded = service.needThisMineralsForLongDistanceConstructionTravel(
//                        20, Protoss_Pylon, pylonOrder
//                    );
//
//                    AConsole.println("mineralsNeeded = " + mineralsNeeded);
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
//        AConsole.errPrintln("Test finished");
//    }

}
//};
