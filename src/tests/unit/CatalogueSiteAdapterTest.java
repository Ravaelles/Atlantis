package tests.unit;

import tests.acceptance.WorldStubForTests;
import atlantis.map.position.APosition;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4 slice 1: the legacy search delegates "which tile" to the catalogue
 * ({@link AbstractPositionFinder} → {@link CatalogueSiteAdapter}).
 *
 * <p>
 * The adapter declines everything outside its scope (gas, orderless
 * rows) with null so the caller falls back; an honoured exact tile,
 * an answered neighbourhood and (since migration A2) an answered base
 * near its natural prove the translation.
 * The injected planner runs over a synthetic flat grid (no game map), the
 * same seam {@code PlacementPlannerTest} uses.
 * </p>
 */
@Tag("placement")
public class CatalogueSiteAdapterTest extends WorldStubForTests {

    private static CataloguePlacementPlanner blocklessPlanner(int w, int h) {
        return new CataloguePlacementPlanner(new TileAvailabilityGrid(new Flat(w, h)), null, null, false);
    }

    private static Construction constructionFor(AUnitType type, ProductionOrder order, APosition near) {
        Construction construction = new Construction(type);
        if (order != null) construction.setProductionOrder(order);
        if (near != null) construction.setNearTo(near);

        return construction;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        EventLog.clearForTests();
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
                    "\"type\":\"Assimilator\",\"result\":\"declined\",\"reason\":\"dedicated-finder\""),
                "the decline lands in the twin with its reason, so a game log names the path");
        });
    }

    @Test
    public void basesAreAnsweredNearTheirNatural() {
        // Migration A2 (owner #146): bases are catalogue ground, the
        // "dedicated-finder" decline moved to gas-only. A Nexus with a
        // natural neighbourhood gets an answered tile; the legacy base
        // search stays as the fallback for a refusing planner.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            Construction construction = constructionFor(
                AUnitType.Protoss_Nexus,
                new ProductionOrder(AUnitType.Protoss_Nexus, 30), APosition.create(25, 25));
            ScriptedPlanner planner = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.success(25, 25, 0));

            APosition site = CatalogueSiteAdapter.siteFor(
                planner, probe, AUnitType.Protoss_Nexus, construction);

            assertNotNull(site,
                "migration A2: the catalogue answers base siting near the natural");
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"Nexus\",\"result\":\"answered\""),
                "the answer lands in the twin with its reason: " + EventLog.toJsonLines());
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
        // No map physics here: the translation (order exact tile -> catalogue
        // exact constraint) is pinned directly below, and the answered path is
        // proven by the game run. What this pins is the gate: even a
        // catalogue YES is refused when the builder-time checks say no, so a
        // tile the issuer would reject never leaves the adapter.
        // V3 S1: the verdict runs before physics, so the tile must be powered
        // (a Pylon stands by) and free of verdict gates - the refusal below
        // names the physics itself (a standing building covers the tile),
        // not power the verdict already owns.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 21, 24),
            fake(AUnitType.Protoss_Gateway, 25, 25),
            probe
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, APosition.create(25, 25), 10);
            order.markAsUsingExactPosition();
            Construction construction = constructionFor(AUnitType.Protoss_Gateway, order, null);
            ScriptedPlanner yes = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.success(25, 25, 0));

            APosition site = CatalogueSiteAdapter.siteFor(yes, probe, AUnitType.Protoss_Gateway, construction);

            assertNull(site, "the stub map refuses the tile - the adapter must decline, never override the gate");
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"Gateway\",\"result\":\"declined\",\"reason\":\"legacy-refused\""));
        });
    }

    @Test
    public void withoutABuilderTheLegacySearchPicksOne() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, APosition.create(20, 20));

            assertNull(
                CatalogueSiteAdapter.siteFor(blocklessPlanner(40, 40), null, AUnitType.Protoss_Pylon, construction),
                "validation needs a builder to check against - decline and let FindPosition define one");
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"Pylon\",\"result\":\"declined\",\"reason\":\"no-builder\""));
        });
    }

    @Test
    public void aNeighbourhoodIsTranslated() {
        // Pure translation, no physics: the order's neighbourhood hint must
        // reach the catalogue as a neighbourhood constraint. The answered
        // path itself is proven by the game run (slice 1e commit).
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
        Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, APosition.create(20, 20));

        atlantis.production.placement.TargetPlacement constraint =
            CatalogueSiteAdapter.constraintFor(order, construction);

        assertEquals(atlantis.production.placement.TargetPlacement.Mode.NEIGHBOURHOOD, constraint.mode());
        assertEquals(20, constraint.tileX());
        assertEquals(20, constraint.tileY());
    }

    @Test
    public void anExactOrderIsTranslated() {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, APosition.create(25, 25), 10);
        order.markAsUsingExactPosition();
        Construction construction = constructionFor(AUnitType.Protoss_Gateway, order, null);

        atlantis.production.placement.TargetPlacement constraint =
            CatalogueSiteAdapter.constraintFor(order, construction);

        assertEquals(atlantis.production.placement.TargetPlacement.Mode.EXACT_TILE, constraint.mode());
        assertEquals(25, constraint.exactTileX());
        assertEquals(25, constraint.exactTileY());
    }

    @Test
    public void noHintMeansAnywhere() {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
        Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, null);

        atlantis.production.placement.TargetPlacement constraint =
            CatalogueSiteAdapter.constraintFor(order, construction);

        assertEquals(atlantis.production.placement.TargetPlacement.Mode.ANYWHERE, constraint.mode());
    }

    @Test
    public void aCatalogueFailureIsDeclined() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, APosition.create(20, 20));
            ScriptedPlanner failing = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.failure());

            APosition site = CatalogueSiteAdapter.siteFor(failing, probe, AUnitType.Protoss_Pylon, construction);

            assertNull(site, "a refused catalogue answer must decline to the legacy search, never invent a tile");
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"Pylon\",\"result\":\"declined\",\"reason\":\"no-tile\""));
        });
    }

    @Test
    public void anAnsweredRowNamesWhereItWasAsked() {
        // NEXT #214a: the answer carries the request's nearTo next to the
        // chosen tile, so a far-away warp names its origin without tile
        // forensics. Pylon needs no power, so the stub validator passes it.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
            Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, APosition.create(20, 20));
            ScriptedPlanner yes = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.success(30, 30, 0));

            APosition site = CatalogueSiteAdapter.siteFor(yes, probe, AUnitType.Protoss_Pylon, construction);

            assertNotNull(site, "a clear tile with a live builder answers");
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"Pylon\",\"result\":\"answered\",\"tx\":30,\"ty\":30,\"nearTx\":20,\"nearTy\":20"),
                "the answer names the request origin next to the chosen tile: " + EventLog.toJsonLines());
        });
    }

    @Test
    public void sitedSearchStaysLegacyWhenFlagIsOff() {
        atlantis.config.env.Env.setPlacementCatalogueForTest(false);

        assertNull(CatalogueSiteAdapter.siteForSited(
                null, AUnitType.Protoss_Photon_Cannon, APosition.create(20, 20), 12),
            "flag off means the sited path never touches the catalogue - FindPosition answers as before");
    }

    @Test
    public void rangeDisciplineIsPureMath() {
        APosition site = APosition.create(20, 20);
        APosition near = APosition.create(22, 20);

        assertTrue(CatalogueSiteAdapter.withinRange(site, near, 12));
        assertTrue(CatalogueSiteAdapter.withinRange(site, near, 2));
        assertFalse(CatalogueSiteAdapter.withinRange(site, near, 1));
        assertTrue(CatalogueSiteAdapter.withinRange(site, null, 1));
        assertTrue(CatalogueSiteAdapter.withinRange(site, near, -1));
        assertTrue(CatalogueSiteAdapter.withinRange(null, near, 1));
    }

    @Test
    public void sitedFailureDeclinesToLegacy() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            ScriptedPlanner failing = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.failure());

            APosition site = CatalogueSiteAdapter.siteForSited(
                failing, probe, AUnitType.Protoss_Photon_Cannon, APosition.create(20, 20), 12);

            assertNull(site, "a refused sited answer declines - the legacy search below answers as before");
            assertTrue(EventLog.toJsonLines().contains(
                "\"type\":\"PhotonC\",\"result\":\"declined\",\"reason\":\"no-tile\","
                    + "\"nearTx\":20,\"nearTy\":20,\"via\":\"sited\""),
                "the decline names its origin like answers do: " + EventLog.toJsonLines());
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

    /**
     * Flat, fully buildable map.
     */
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

    /**
     * A planner with a scripted answer: the seam for decline routing without map physics.
     */
    @Test
    public void refusedTileIsDeclinedWithItsOwnReason() {
        // Owner #151: an engine-refused tile must not be re-served. The
        // twin names the refusal (not a generic no-tile), the caller
        // re-sites or retries instead of looping on the cursed tile.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            atlantis.production.constructions.position.RefusedTiles.clearForTests();
            atlantis.production.constructions.position.RefusedTiles.note(
                APosition.create(25, 25), AUnitType.Protoss_Gateway);

            Construction construction = constructionFor(
                AUnitType.Protoss_Gateway,
                new ProductionOrder(AUnitType.Protoss_Gateway, 8), null);
            ScriptedPlanner planner = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.success(25, 25, 0));

            APosition site = CatalogueSiteAdapter.siteFor(planner, probe, AUnitType.Protoss_Gateway, construction);

            assertNull(site, "a refused tile is never re-served, even when the planner offers it");
            assertTrue(EventLog.toJsonLines().contains("\"result\":\"declined\"")
                && EventLog.toJsonLines().contains("refused-tile"),
                "the decline is twinned with its reason: " + EventLog.toJsonLines());

            atlantis.production.constructions.position.RefusedTiles.clearForTests();
        });
    }

    /**
     * Owner 2026-11-01: the main base must reach the site by ground.
     * Last and once per proposal (reachability is the costly query): a
     * disconnected region (UMS/island pocket, measured as a first Pylon
     * ~35 tiles from the main with no land path) declines to the legacy
     * spiral instead of stranding the builder.
     */
    @Test
    public void disconnectedRegionDeclinesToLegacy() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            atlantis.map.ReachableTiles.useReachability(
                (fromTx, fromTy, toTx, toTy) -> false, "test-disconnected");
            try {
                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
                Construction construction = constructionFor(AUnitType.Protoss_Pylon, order, APosition.create(20, 20));
                ScriptedPlanner planner = new ScriptedPlanner(
                    atlantis.production.placement.PlacementReservation.success(55, 55, 0));

                APosition site = CatalogueSiteAdapter.siteFor(planner, probe, AUnitType.Protoss_Pylon, construction);

                assertNull(site, "no land path from the main means no catalogue tile - the legacy search owns it");
                assertTrue(EventLog.toJsonLines().contains("\"reason\":\"unreachable\""),
                    "the decline names itself in the twin: " + EventLog.toJsonLines());
            }
            finally {
                atlantis.map.ReachableTiles.reset();
            }
        });
    }

    /**
     * V3 S6 slice 2 (owner order 2026-11-04, Stardust doctrine): a free
     * reserved slot serves BEFORE the planner hunts - the layout picked
     * once at game start owns issuance, per-frame search is the
     * fallback. A failing planner must not matter when G1 is free.
     */
    @Test
    public void aFreeReservedSlotServesBeforeThePlanner() {
        atlantis.placement.core.TileAvailabilityGrid grid =
            new atlantis.placement.core.TileAvailabilityGrid(new Flat(40, 40));
        java.util.List<atlantis.placement.core.ReservedSlots.Slot> layout =
            atlantis.placement.core.ReservedSlots.planOn(
                new atlantis.placement.core.BuildLocationCatalogue(grid),
                APosition.create(20, 20),
                AUnitType.Protoss_Gateway, 10);

        // Expected slot computed purely: the live main base stands on
        // its tile, so slots under the 4x3 Nexus are born covered.
        atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
        final APosition expected =
            atlantis.placement.core.ReservedSlots.firstFreeSlotOfType(
                AUnitType.Protoss_Gateway,
                (tile, type) -> coversNexusBase(tile, type));
        assertNotNull(expected, "the layout owes a free gateway slot");

        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, expected.tx() + 5, expected.ty()),
            probe
        ), fakeEnemies(), () -> {
            atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
            try {
                // The production occupancy (live Select) must agree with
                // the scripted one above - otherwise the test proves
                // nothing about the live path.
                APosition liveExpected =
                    atlantis.placement.core.ReservedSlots.firstFreeSlotOfType(
                        AUnitType.Protoss_Gateway,
                        (tile, type) ->
                            atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding
                                .check(tile, type)
                                || atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding
                                .check(tile, type));
                assertNotNull(liveExpected, "a free slot exists live too");
                assertEquals(expected.tx(), liveExpected.tx(), "scripted and live occupancy agree");
                assertEquals(expected.ty(), liveExpected.ty(), "scripted and live occupancy agree");

                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
                Construction construction = constructionFor(
                    AUnitType.Protoss_Gateway, order, APosition.create(20, 20));
                ScriptedPlanner failing = new ScriptedPlanner(
                    atlantis.production.placement.PlacementReservation.failure());

                APosition site = CatalogueSiteAdapter.siteFor(
                    failing, probe, AUnitType.Protoss_Gateway, construction);

                assertNotNull(site, "a free slot serves even when the planner declines: "
                    + EventLog.toJsonLines());
                assertEquals(expected.tx(), site.tx(), "the slot tile, not a hunted one");
                assertEquals(expected.ty(), site.ty(), "the slot tile, not a hunted one");
                assertTrue(EventLog.toJsonLines().contains("\"via\":\"slot\""),
                    "the answer names the slot path: " + EventLog.toJsonLines());
            }
            finally {
                atlantis.placement.core.ReservedSlots.clearForTests();
            }
        });
    }

    /**
     * Scripted twin of the production occupancy for the slot tests:
     * the 4x3 Nexus standing on the main base tile covers whatever
     * footprint it touches. Pure, so the expected slot is known
     * before the world (and its power Pylon) is built.
     */
    private static boolean coversNexusBase(APosition tile, AUnitType type) {
        int w = type.getTilesWidth();
        int h = type.getTilesHeights();
        return tile.tx() < 20 + 4 && 20 < tile.tx() + w
            && tile.ty() < 20 + 3 && 20 < tile.ty() + h;
    }

    @Test
    public void anOccupiedSlotIsSkippedForTheNextFreeOne() {
        atlantis.placement.core.TileAvailabilityGrid grid =
            new atlantis.placement.core.TileAvailabilityGrid(new Flat(40, 40));
        java.util.List<atlantis.placement.core.ReservedSlots.Slot> layout =
            atlantis.placement.core.ReservedSlots.planOn(
                new atlantis.placement.core.BuildLocationCatalogue(grid),
                APosition.create(20, 20),
                AUnitType.Protoss_Gateway, 10);
        atlantis.placement.core.ReservedSlots.Slot g1 = null;
        atlantis.placement.core.ReservedSlots.Slot g2 = null;
        for (atlantis.placement.core.ReservedSlots.Slot slot : layout) {
            if (slot.label.equals("G1")) g1 = slot;
            if (slot.label.equals("G2")) g2 = slot;
        }
        assertNotNull(g1, "the layout owes G1");
        assertNotNull(g2, "the layout owes G2");
        final atlantis.placement.core.ReservedSlots.Slot taken = g1;
        final atlantis.placement.core.ReservedSlots.Slot next = g2;

        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, next.tile.tx() + 5, next.tile.ty()),
            fake(AUnitType.Protoss_Gateway, taken.tile.tx(), taken.tile.ty()),
            probe
        ), fakeEnemies(), () -> {
            atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
            try {
                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
                Construction construction = constructionFor(
                    AUnitType.Protoss_Gateway, order, APosition.create(20, 20));
                ScriptedPlanner failing = new ScriptedPlanner(
                    atlantis.production.placement.PlacementReservation.failure());

                APosition site = CatalogueSiteAdapter.siteFor(
                    failing, probe, AUnitType.Protoss_Gateway, construction);

                assertNotNull(site, "an occupied G1 skips to G2, never declines while slots stay free");
                assertEquals(next.tile.tx(), site.tx(), "the next free slot");
                assertEquals(next.tile.ty(), site.ty(), "the next free slot");
            }
            finally {
                atlantis.placement.core.ReservedSlots.clearForTests();
            }
        });
    }

    /**
     * V3 S6 slice 3: an unpowered reserved slot orders its support -
     * the drought shape (228 unpowered declines, zero Pylons) dies
     * here. The support Pylon rides the same high-priority queue the
     * planner drain uses, so no new ordering rules; dedup still owns
     * flood control.
     */
    @Test
    public void anUnpoweredSlotQueuesItsSupportPylon() {
        atlantis.placement.core.TileAvailabilityGrid grid =
            new atlantis.placement.core.TileAvailabilityGrid(new Flat(40, 40));
        java.util.List<atlantis.placement.core.ReservedSlots.Slot> layout =
            atlantis.placement.core.ReservedSlots.planOn(
                new atlantis.placement.core.BuildLocationCatalogue(grid),
                APosition.create(20, 20),
                AUnitType.Protoss_Gateway, 10);
        atlantis.placement.core.ReservedSlots.Slot g1 = null;
        for (atlantis.placement.core.ReservedSlots.Slot slot : layout) {
            if (slot.label.equals("G1")) g1 = slot;
        }
        assertNotNull(g1, "the layout owes G1");
        final atlantis.placement.core.ReservedSlots.Slot first = g1;

        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
            try {
                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
                Construction construction = constructionFor(
                    AUnitType.Protoss_Gateway, order, APosition.create(20, 20));
                ScriptedPlanner failing = new ScriptedPlanner(
                    atlantis.production.placement.PlacementReservation.failure());

                APosition site = CatalogueSiteAdapter.siteFor(
                    failing, probe, AUnitType.Protoss_Gateway, construction);

                assertNull(site, "no power, no tile - the slot declines like the hunt would");
                boolean queued = false;
                for (ProductionOrder row : Queue.get().allOrders().list()) {
                    if (row.unitType() != null && row.unitType().isPylon()
                        && row.aroundPosition() != null
                        && row.priority() == atlantis.production.orders.production.queue.order.ProductionOrderPriority.HIGH
                        && Math.max(Math.abs(row.aroundPosition().tx() - first.tile.tx()),
                            Math.abs(row.aroundPosition().ty() - first.tile.ty())) <= 6) {
                        queued = true;
                    }
                }
                assertTrue(queued, "the unpowered slot orders its support Pylon high-priority"
                    + " on a covering Pylon slot, not the bare Gateway tile: "
                    + Queue.get().allOrders().list());
                assertTrue(EventLog.toJsonLines().contains("\"result\":\"supported\""),
                    "the support order is twinned: " + EventLog.toJsonLines());
                assertTrue(EventLog.toJsonLines().contains("\"supportTx\""),
                    "the twin names where the Pylon should land: " + EventLog.toJsonLines());
            }
            finally {
                atlantis.placement.core.ReservedSlots.clearForTests();
            }
        });
    }

    @Test
    public void aPoweredSlotOrdersNoSupport() {
        atlantis.placement.core.TileAvailabilityGrid grid =
            new atlantis.placement.core.TileAvailabilityGrid(new Flat(40, 40));
        java.util.List<atlantis.placement.core.ReservedSlots.Slot> layout =
            atlantis.placement.core.ReservedSlots.planOn(
                new atlantis.placement.core.BuildLocationCatalogue(grid),
                APosition.create(20, 20),
                AUnitType.Protoss_Gateway, 10);
        atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
        final APosition expected =
            atlantis.placement.core.ReservedSlots.firstFreeSlotOfType(
                AUnitType.Protoss_Gateway,
                (tile, type) -> coversNexusBase(tile, type));
        assertNotNull(expected, "the layout owes a free gateway slot");

        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, expected.tx() + 5, expected.ty()),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
            try {
                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
                Construction construction = constructionFor(
                    AUnitType.Protoss_Gateway, order, APosition.create(20, 20));
                ScriptedPlanner failing = new ScriptedPlanner(
                    atlantis.production.placement.PlacementReservation.failure());

                APosition site = CatalogueSiteAdapter.siteFor(
                    failing, probe, AUnitType.Protoss_Gateway, construction);

                assertNotNull(site, "a powered slot serves: " + EventLog.toJsonLines());
                boolean queued = false;
                for (ProductionOrder row : Queue.get().allOrders().list()) {
                    if (row.unitType() != null && row.unitType().isPylon()) queued = true;
                }
                assertFalse(queued, "power present means no support order: "
                    + Queue.get().allOrders().list());
            }
            finally {
                atlantis.placement.core.ReservedSlots.clearForTests();
            }
        });
    }

    @Test
    public void fullSlotsFallThroughToThePlanner() {
        atlantis.placement.core.TileAvailabilityGrid grid =
            new atlantis.placement.core.TileAvailabilityGrid(new Flat(40, 40));
        java.util.List<atlantis.placement.core.ReservedSlots.Slot> layout =
            atlantis.placement.core.ReservedSlots.planOn(
                new atlantis.placement.core.BuildLocationCatalogue(grid),
                APosition.create(20, 20),
                AUnitType.Protoss_Gateway, 10);

        java.util.List<FakeUnit> ours = new java.util.ArrayList<FakeUnit>();
        ours.add(fake(AUnitType.Protoss_Nexus, 20, 20));
        ours.add(fake(AUnitType.Protoss_Pylon, 30, 25));
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        ours.add(probe);
        for (atlantis.placement.core.ReservedSlots.Slot slot : layout) {
            if (slot.type.equals(AUnitType.Protoss_Gateway)) {
                ours.add(fake(AUnitType.Protoss_Gateway, slot.tile.tx(), slot.tile.ty()));
            }
            if (slot.type.equals(AUnitType.Protoss_Pylon)) {
                ours.add(fake(AUnitType.Protoss_Pylon, slot.tile.tx(), slot.tile.ty()));
            }
        }
        world(5, fakeOurs(ours.toArray(new FakeUnit[0])), fakeEnemies(), () -> {
            atlantis.placement.core.ReservedSlots.setSlotsForTests(layout);
            try {
                ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
                Construction construction = constructionFor(
                    AUnitType.Protoss_Pylon, order, APosition.create(20, 20));
                ScriptedPlanner yes = new ScriptedPlanner(
                    atlantis.production.placement.PlacementReservation.success(33, 20, 0));

                APosition site = CatalogueSiteAdapter.siteFor(
                    yes, probe, AUnitType.Protoss_Pylon, construction);

                assertNotNull(site, "a covered type with no free slot falls back to the hunt, never declines: "
                    + EventLog.toJsonLines());
                assertEquals(33, site.tx(), "the planner tile, not a slot");
                assertEquals(20, site.ty(), "the planner tile, not a slot");
                assertTrue(EventLog.toJsonLines().contains("\"via\":\"funnel\""),
                    "the fallback names the funnel path: " + EventLog.toJsonLines());
            }
            finally {
                atlantis.placement.core.ReservedSlots.clearForTests();
            }
        });
    }

    /**
     * A planner with a scripted answer: the seam for decline routing without map physics.
     */
    private static final class ScriptedPlanner implements atlantis.production.placement.PlacementPlanner {
        private final atlantis.production.placement.PlacementReservation answer;
        private final java.util.List<atlantis.placement.core.SupportRequest> support;

        ScriptedPlanner(atlantis.production.placement.PlacementReservation answer) {
            this(answer, java.util.Collections.emptyList());
        }

        ScriptedPlanner(atlantis.production.placement.PlacementReservation answer,
                        java.util.List<atlantis.placement.core.SupportRequest> support) {
            this.answer = answer;
            this.support = support;
        }

        @Override
        public void startPass() {
        }

        @Override
        public atlantis.production.placement.PlacementReservation reservePlacement(
            atlantis.production.producibles.Producible building,
            atlantis.production.placement.TargetPlacement constraint, int targetFrame
        ) {
            return answer;
        }

        @Override
        public java.util.List<atlantis.placement.core.SupportRequest> drainSupportRequests() {
            return new java.util.ArrayList<>(support);
        }
    }

    /**
     * B1 (owner #147, Pylon family): the expansion-power job of the
     * deleted per-base Pylon producer lives here - siting a Gateway on
     * an unpowered tile queues the support Pylon with high priority
     * instead of serving the tile, so the new base gets power first.
     */
    @Test
    public void unpoweredSitingQueuesSupportPylon() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 21, 21);
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            probe
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 8);
            Construction construction = constructionFor(AUnitType.Protoss_Gateway, order, APosition.create(20, 20));
            ScriptedPlanner planner = new ScriptedPlanner(
                atlantis.production.placement.PlacementReservation.failure(),
                java.util.Collections.singletonList(
                    new atlantis.placement.core.SupportRequest(AUnitType.Protoss_Pylon, 25, 25)));

            APosition site = CatalogueSiteAdapter.siteFor(planner, probe, AUnitType.Protoss_Gateway, construction);

            assertNull(site, "no tile served without power - support instead");
            boolean queued = false;
            for (ProductionOrder row : Queue.get().allOrders().list()) {
                if (row.unitType() != null && row.unitType().isPylon()
                    && row.aroundPosition() != null
                    && row.aroundPosition().tx() == 25 && row.aroundPosition().ty() == 25
                    && row.priority() == atlantis.production.orders.production.queue.order.ProductionOrderPriority.HIGH) {
                    queued = true;
                }
            }
            assertTrue(queued, "the support Pylon lands in the queue with high priority: "
                + Queue.get().allOrders().list());
        });
    }
}
