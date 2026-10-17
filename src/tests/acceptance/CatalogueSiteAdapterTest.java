package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Phase 4 slice 1: the legacy search delegates "which tile" to the catalogue
 * ({@link APositionFinder} → {@link CatalogueSiteAdapter}).
 *
 * <p>
 * The adapter declines everything outside its scope (gas, bases, orderless
 * rows) with null so the caller falls back to {@code FindPosition}; an
 * honoured exact tile and an answered neighbourhood prove the translation.
 * The injected planner runs over a synthetic flat grid (no game map), the
 * same seam {@code PlacementPlannerTest} uses.
 * </p>
 */
public class CatalogueSiteAdapterTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        EventLog.clearForTests();
    }

    /** Flat, fully buildable map. */
    private static final class Flat implements TileAvailabilityGrid.TerrainSource {
        private final int w;
        private final int h;

        Flat(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public int mapWidth() {
            return w;
        }

        @Override
        public int mapHeight() {
            return h;
        }

        @Override
        public boolean isBuildable(int tx, int ty) {
            return true;
        }

        @Override
        public boolean isWalkable(int tx, int ty) {
            return true;
        }

        @Override
        public boolean isResource(int tx, int ty) {
            return false;
        }

        @Override
        public boolean isDepotOrigin(int tx, int ty) {
            return false;
        }
    }

    private static CataloguePlacementPlanner blocklessPlanner(int w, int h) {
        return new CataloguePlacementPlanner(new TileAvailabilityGrid(new Flat(w, h)), null, null, false);
    }

    private static Construction constructionFor(AUnitType type, ProductionOrder order, APosition near) {
        Construction construction = new Construction(type);
        if (order != null) construction.setProductionOrder(order);
        if (near != null) construction.setNearTo(near);
        return construction;
    }

    @Test
    public void gasBuildingsStayOnTheirDedicatedFinder() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            Construction construction = constructionFor(
                AUnitType.Protoss_Assimilator,
                new ProductionOrder(AUnitType.Protoss_Assimilator, 8), null);
            assertNull(
                CatalogueSiteAdapter.siteFor(blocklessPlanner(40, 40), null, AUnitType.Protoss_Assimilator, construction),
                "a refinery builds only on its geyser's exact tile - the catalogue must decline");
            assertTrue(EventLog.toJsonLines().contains(
                "\"key\":\"CATALOGUE_SITE\",\"type\":\"Assimilator\",\"result\":\"declined\",\"reason\":\"dedicated-finder\""),
                "the decline lands in the twin with its reason, so a game log names the path");
        });
    }

    @Test
    public void basesStayOnTheirDedicatedFinder() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            Construction construction = constructionFor(
                AUnitType.Protoss_Nexus,
                new ProductionOrder(AUnitType.Protoss_Nexus, 30), null);
            assertNull(
                CatalogueSiteAdapter.siteFor(blocklessPlanner(40, 40), null, AUnitType.Protoss_Nexus, construction),
                "a base builds on a base location - the catalogue must decline");
        });
    }

    @Test
    public void anOrderlessRowIsDeclined() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            assertNull(
                CatalogueSiteAdapter.siteFor(
                    blocklessPlanner(40, 40), null, AUnitType.Protoss_Pylon, new Construction(AUnitType.Protoss_Pylon)),
                "no order, no constraint - the legacy null contract holds");
        });
    }

    @Test
    public void anExactTileIsHonoured() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, APosition.create(25, 25), 10);
            order.markAsUsingExactPosition();
            Construction construction = constructionFor(AUnitType.Protoss_Gateway, order, null);

            APosition site = CatalogueSiteAdapter.siteFor(blocklessPlanner(40, 40), null, AUnitType.Protoss_Gateway, construction);

            assertNotNull(site, "a free exact tile on a flat map must be answered");
            assertEquals(25, site.tx());
            assertEquals(25, site.ty());
            assertTrue(EventLog.toJsonLines().contains(
                "\"key\":\"CATALOGUE_SITE\",\"type\":\"Gateway\",\"result\":\"answered\",\"tx\":25,\"ty\":25"),
                "the answered tile lands in the twin, so a game log names the path");
        });
    }

    @Test
    public void aNeighbourhoodIsAnswered() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, APosition.create(20, 20));

            APosition site = CatalogueSiteAdapter.siteFor(blocklessPlanner(40, 40), null, AUnitType.Protoss_Pylon, construction);

            assertNotNull(site, "a Pylon near (20,20) on a flat map must be answered");
        });
    }

    @Test
    public void aCatalogueFailureIsDeclined() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            CataloguePlacementPlanner planner = blocklessPlanner(40, 40);
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, APosition.create(25, 25), 10);
            order.markAsUsingExactPosition();

            // First building claims the tile on the shared grid; the adapter's
            // own startPass keeps the grid (test seam) while clearing the pass,
            // so the same exact tile is refused the second time.
            CatalogueSiteAdapter.siteFor(
                planner, null, AUnitType.Protoss_Gateway, constructionFor(AUnitType.Protoss_Gateway, order, null));
            APosition retry = CatalogueSiteAdapter.siteFor(
                planner, null, AUnitType.Protoss_Gateway, constructionFor(AUnitType.Protoss_Gateway, order, null));

            assertNull(retry, "a refused catalogue answer must decline to the legacy search, never invent a tile");
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
