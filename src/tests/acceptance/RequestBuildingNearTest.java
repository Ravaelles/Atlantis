package tests.acceptance;

import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.RequestBuildingNear;
import atlantis.production.dynamic.reinforce.protoss.BuildPylonFirst;
import atlantis.production.dynamic.reinforce.protoss.ProtossSecureBaseWithCannons;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RequestBuildingNearTest extends WorldStubForTests {
    private static FakeUnit main = null;
    private static FakeUnit natural = null;
    private static FakeUnit third = null;

    @Test
    public void testRequestingACannon_main_requestCannonInStandardWayForMain() {
        createWorld(1,
            () -> {
                assertEquals(0, ConstructionRequests.all().size());
                assertEquals("Init", AbstractPositionFinder._STATUS);

                HasPosition secure = main;
                ProductionOrder order = securePositionWithCannon(secure);

//                printOrder(order, secure);

                assertNull(RequestBuildingNear.lastError);
                assertNotNull(order);
                assertEquals(1, ConstructionRequests.all().size());
                assertEquals("OK", AbstractPositionFinder._STATUS);
            },
            () -> fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                fake(AUnitType.Protoss_Pylon, 10, 45),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ),
            () -> fakeEnemies()
        );
    }

    @Test
    public void testRequestingACannon_mainAndNatural_buildAMissingPylonInNatural() {
        createWorld(1,
            () -> {
                assertEquals(0, ConstructionRequests.all().size());
                assertEquals("Init", AbstractPositionFinder._STATUS);
                assertNull(RequestBuildingNear.lastError);
                assertNull(BuildPylonFirst.lastError);

                HasPosition secure = natural;
                ProductionOrder order = securePositionWithCannon(secure);

                printOrder(order, secure);

//                assertNull(order);
//                assertEquals(1, ConstructionRequests.all().size());

                assertNull(RequestBuildingNear.lastError);
                assertNull(BuildPylonFirst.lastError);
                assertNotNull(order);
                assertTrue(order.unitType().isPylon());
                assertEquals(1, ConstructionRequests.all().size());
                assertEquals("OK", AbstractPositionFinder._STATUS);

                Construction construction = ConstructionRequests.all().get(0);
                System.err.println("construction = " + construction);
                System.err.println("dist = " + construction.buildPosition().distTo(secure));

                assertNotNull(construction);
                assertNotNull(construction.buildPosition());
                assertTrue(construction.buildingType().isPylon());
//                assertTrue(construction.buildPosition().groundDistanceTo(secure) < 10);

//                assertNull(RequestBuildingNear.lastError);
//                assertNotNull(order);
//                assertEquals("OK", AbstractPositionFinder._STATUS);
//                assertEquals(1, ConstructionRequests.all().size());
            },
            () -> fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ),
            () -> fakeEnemies()
        );
    }

    @Test
    public void testRequestingACannon_mainAndNatural_buildFirstCannonAtNatural() {
        createWorld(1,
            () -> {
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
            },
            () -> fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural
                fake(AUnitType.Protoss_Pylon, 17, 13),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ),
            () -> fakeEnemies()
        );
    }

    @Test
    public void testRequestingACannon_third_noPylon() {
        createWorld(1,
            () -> {
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
                assertTrue(ConstructionRequests.all().get(0).buildingType().isPylon());
            },
            () -> fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural
                third = fake(AUnitType.Protoss_Nexus, 52, 9), // Third
                fake(AUnitType.Protoss_Pylon, 17, 13),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ),
            () -> fakeEnemies()
        );
    }

    @Test
    public void testRequestingACannon_third_withPylon() {
        createWorld(1,
            () -> {
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
            },
            () -> fakeOurs(
                main = fake(AUnitType.Protoss_Nexus, 9, 46), // Main
                natural = fake(AUnitType.Protoss_Nexus, 16, 14), // Natural
                third = fake(AUnitType.Protoss_Nexus, 52, 9), // Third
                fake(AUnitType.Protoss_Pylon, 53, 12),
                fake(AUnitType.Protoss_Pylon, 17, 13),

                fake(AUnitType.Protoss_Pylon, 99, 99),
                fake(AUnitType.Protoss_Forge, 98, 98),

                fake(AUnitType.Protoss_Probe, 7, 47)
            ),
            () -> fakeEnemies()
        );
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