package tests.acceptance;

import atlantis.game.A;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.RequestBuildingNear;
import atlantis.production.dynamic.protoss.reinforce.BuildPylonFirst;
import atlantis.production.dynamic.protoss.reinforce.ProtossSecureBaseWithCannons;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;
import bwapi.Race;

public class RequestBuildingNearTest extends WorldStubForTests {

    /** Pylons, cannons and Protoss base reinforcement - this is a Protoss test. */
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private static FakeUnit main = null;
    private static FakeUnit natural = null;
    private static FakeUnit third = null;

    @Test
    public void testRequestingACannon_main_requestCannonInStandardWayForMain() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                fake(AUnitType.Protoss_Pylon, 10, 45),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ), fakeEnemies(), () -> {
            assertEquals(0, ConstructionRequests.all().size());
            assertEquals("Init", AbstractPositionFinder._STATUS);

            HasPosition secure = main;
            ProductionOrder order = securePositionWithCannon(secure);

//                printOrder(order, secure);

            assertNull(RequestBuildingNear.lastError);
            assertNotNull(order);
            assertEquals(1, ConstructionRequests.all().size());
            assertEquals("OK", AbstractPositionFinder._STATUS);
        });
    }

    @Test
    public void testRequestingACannon_mainAndNatural_buildAMissingPylonInNatural() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ), fakeEnemies(), () -> {
            // This natural is 32.8 ground tiles from the main and the bot refuses
            // to put a pylon that far from its base before it has 40 supply -
            // PylonTooFarFromBaseEarly, pinned on its own in
            // testRequestingAPylon_farFromMainAndSupplyUnder40_pylonIsRefused.
            initSupply(40, 42);

            assertEquals(0, ConstructionRequests.all().size());
            assertEquals("Init", AbstractPositionFinder._STATUS);
            assertNull(RequestBuildingNear.lastError);
            assertNull(BuildPylonFirst.lastError);

            HasPosition secure = natural;
            assertTrue(BuildPylonFirst.needsPylon(secure), "no pylon near the natural");

            ProductionOrder order = securePositionWithCannon(secure);

            printOrder(order, secure);

            assertNotNull(order, "the pylon is placed");
            assertNull(BuildPylonFirst.lastError);
            assertNull(RequestBuildingNear.lastError);
            assertEquals("OK", AbstractPositionFinder._STATUS);
            assertEquals(1, ConstructionRequests.all().size());
            assertTrue(ConstructionRequests.all().get(0).buildingType().isPylon(),
                "a base without power gets a pylon, not a cannon");
        });
    }

    /**
     * The rule that made the test above impossible before: a Protoss pylon is
     * power *and* an expansion, so the bot refuses to build one more than 22
     * ground tiles from its main while it still has less than 40 supply and has
     * not committed to expanding. The refusal has to be reported, not silently
     * drop the order.
     */
    @Test
    public void testRequestingAPylon_farFromMainAndSupplyUnder40_pylonIsRefused() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural, 32.8 tiles away

                fake(AUnitType.Protoss_Pylon, 10, 45), // power next to the main

                fake(AUnitType.Protoss_Probe, 7, 47)
            ), fakeEnemies(), () -> {
            assertEquals(4, A.supplyTotal());

            // Close to the main: allowed.
            assertNotNull(RequestBuildingNear.constructionOf(AUnitType.Protoss_Pylon).near(main).request(),
                "a pylon near the main is built");
            assertNull(RequestBuildingNear.lastError);
            assertEquals("OK", AbstractPositionFinder._STATUS);

            // Same building, same world, 32.8 tiles away: refused.
            ProductionOrder farAway = RequestBuildingNear.constructionOf(AUnitType.Protoss_Pylon)
                .near(natural)
                .request();

            assertNull(farAway, "no pylon that far from the main before 40 supply");
            assertNotNull(RequestBuildingNear.lastError, "and the reason is reported");
        });
    }

    @Test
    public void testRequestingACannon_mainAndNatural_buildFirstCannonAtNatural() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural
                fake(AUnitType.Protoss_Pylon, 17, 13),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ), fakeEnemies(), () -> {
            assertEquals(0, ConstructionRequests.all().size());
            assertEquals("Init", AbstractPositionFinder._STATUS);
            assertNull(RequestBuildingNear.lastError);

            HasPosition secure = natural;
            ProductionOrder order = securePositionWithCannon(secure);

            printOrder(order, secure);

            assertNull(RequestBuildingNear.lastError);
            assertNotNull(order);
            assertEquals("OK", AbstractPositionFinder._STATUS);
            assertEquals(1, ConstructionRequests.all().size());
        });
    }

    @Test
    public void testRequestingACannon_third_noPylon() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural
                third = fake(AUnitType.Protoss_Nexus, 52, 9), // Third
                fake(AUnitType.Protoss_Pylon, 17, 13),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ), fakeEnemies(), () -> {
            // The third base is ~55 ground tiles from the main, so - exactly as
            // for the natural above - it can only get power once the bot has the
            // supply for it.
            initSupply(40, 42);

            assertEquals(0, ConstructionRequests.all().size());
            assertEquals("Init", AbstractPositionFinder._STATUS);
            assertNull(RequestBuildingNear.lastError);

            HasPosition secure = third;
            assertTrue(BuildPylonFirst.needsPylon(secure), "no pylon near the third base");

            ProductionOrder order = securePositionWithCannon(secure);

            printOrder(order, secure);

            assertNotNull(order, "the pylon is placed at the third base");
            assertNull(RequestBuildingNear.lastError);
            assertEquals("OK", AbstractPositionFinder._STATUS);
            assertEquals(1, ConstructionRequests.all().size());
            assertTrue(ConstructionRequests.all().get(0).buildingType().isPylon());
        });
    }

    @Test
    public void testRequestingACannon_third_withPylon() {
        world(1, fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural
                third = fake(AUnitType.Protoss_Nexus, 52, 9), // Third
                fake(AUnitType.Protoss_Pylon, 53, 12),
                fake(AUnitType.Protoss_Pylon, 17, 13),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ), fakeEnemies(), () -> {
            assertEquals(0, ConstructionRequests.all().size());
            assertEquals("Init", AbstractPositionFinder._STATUS);
            assertNull(RequestBuildingNear.lastError);

            HasPosition secure = third;
            ProductionOrder order = securePositionWithCannon(secure);

            printOrder(order, secure);

            assertNull(RequestBuildingNear.lastError);
            assertNotNull(order);
            assertEquals("OK", AbstractPositionFinder._STATUS);
            assertEquals(1, ConstructionRequests.all().size());
            assertTrue(ConstructionRequests.all().get(0).buildingType().isCannon());
        });
    }

    // =========================================================

    private static ProductionOrder securePositionWithCannon(HasPosition secure) {
        return (new ProtossSecureBaseWithCannons(secure)).reinforce();
//        return RequestCannonAt.at(secure);
    }

    private static void printOrder(ProductionOrder order, HasPosition nearTo) {
        if (order == null || order.construction() == null) {
            System.err.println("Order is null, can't print it.");
            return;
        }

        APosition buildPosition = order.construction().buildPosition();

        System.err.println("order     = " + order);
        System.err.println("nearTo    = " + nearTo);
        System.err.println("buildPos  = " + buildPosition);
        System.err.println("buildable = " + buildPosition.isBuildableIncludeBuildings());
        System.err.println("choke     = " + Chokes.natural());
        System.err.println("dist_init = " + nearTo.distTo(buildPosition));
    }
}