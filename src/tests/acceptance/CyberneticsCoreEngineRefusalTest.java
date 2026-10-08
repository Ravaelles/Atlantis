package tests.acceptance;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.builders.RefreshConstructionPosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.DefineExactPositionForNewConstruction;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;
import bwapi.Race;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The two opposite symptoms of one root cause, from the reports:
 *
 * <pre>
 * Cancel construction: CyberneticsC / position still not good after refresh / at:[112,30] / buildable:false
 * Cancel construction: Gateway / Gateway took too long (45s) / at:[112,37] / buildable:false
 * Cancel construction: Pylon   / Pylon took too long (37s)   / at:[112,30] / buildable:false
 * </pre>
 *
 * <p>
 * Both come from deciding "can I build here?" with the wrong source:
 * </p>
 * <ul>
 *   <li>the raw engine tile query ({@code isBuildableIncludeBuildings}) refuses
 *       valid empty tiles on OpenBW - so a tile the finder just accepted was
 *       declared "not good" and the construction was cancelled in a loop;</li>
 *   <li>{@code CanPhysicallyBuildHere}'s JBWEB fallback reads a {@code usedGrid}
 *       that Atlantis never updates in-game ({@code onUnitDiscover}/{@code onUnitDestroy}
 *       are never called, only {@code onStart}), so an <b>occupied</b> tile was
 *       declared "good" - the builder travelled there and never built, timing out.</li>
 * </ul>
 *
 * <p>
 * The fix asks <b>occupancy</b>, from the live unit list: an empty tile the engine
 * refuses is kept; a tile with a unit on it is refreshed. The live unit list is the
 * only source that is right in both cases, so the two tests below pin exactly those
 * two behaviours.
 * </p>
 */
public class CyberneticsCoreEngineRefusalTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The engine source with OpenBW's measured unreliability: the raw tile query
     * says "not buildable" even for a valid, empty tile.
     */
    private static class EngineRefusesRawTileQuery implements MapTiles.Source {
        @Override
        public boolean isWalkable(HasPosition at) {
            return true;
        }

        @Override
        public boolean isExplored(HasPosition at) {
            return true;
        }

        @Override
        public boolean isVisible(HasPosition at) {
            return true;
        }

        @Override
        public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
            return false;
        }

        @Override
        public boolean hasPathBetween(HasPosition from, HasPosition to) {
            return true;
        }

        @Override
        public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
            return true;
        }

        @Override
        public boolean spawnsFromMapData() {
            return true;
        }
    }

    @AfterEach
    public void restoreHarnessTiles() {
        FakeMapTiles.installAsSource();
        AbstractPositionFinder._STATUS = "Init";
    }

    /**
     * Symptom 1 (the Cybernetics Core report): the engine refuses the tile, but
     * nothing is standing on it - the refresh must leave it alone.
     */
    @Test
    public void anEmptyTileTheEngineRefusesIsNotRefreshedAway() {
        MapTiles.useSource(new EngineRefusesRawTileQuery());

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 22),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            APosition accepted = APosition.create(26, 23);
            assertFalse(accepted.isBuildableIncludeBuildings(), "the engine refuses this empty tile");

            Construction construction = constructionFor(accepted);

            APosition afterRefresh = RefreshConstructionPosition.refreshIfNeeded(construction);

            assertNotNull(afterRefresh, "the position survived the refresh");
            assertEquals(accepted, afterRefresh,
                "an empty tile the engine refuses must not be refreshed away (that was the cancel loop)");
        });
    }

    /**
     * Symptom 2 (the Gateway/Pylon report): a building already stands on the tile
     * - the refresh must move the construction somewhere else instead of leaving a
     * builder to time out on a tile it can never build on.
     */
    @Test
    public void anOccupiedTileIsRefreshedAway() {
        MapTiles.useSource(new EngineRefusesRawTileQuery());

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            // A Pylon already occupying the tile the new Pylon/Gateway would use.
            fake(AUnitType.Protoss_Pylon, 26, 23),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            APosition occupied = APosition.create(26, 23);
            assertFalse(occupied.isBuildableIncludeBuildings());

            Construction construction = constructionFor(occupied);

            APosition afterRefresh = RefreshConstructionPosition.refreshIfNeeded(construction);

            assertNotEquals(occupied, afterRefresh,
                "a tile with a building on it must be refreshed away, or the builder times out on it");
        });
    }

    /**
     * Symptom 3 (the new report): the order carries an <b>exact</b> position (how
     * RequestBuildingNear places pylons, cannons and the Cybernetics Core), and
     * that position is occupied. Refreshing the Construction alone is not enough:
     * {@code DefineExactPositionForNewConstruction} hands the order's
     * {@code aroundPosition} back verbatim the next time the construction is built,
     * so the occupied tile returns and the builder is cancelled after ~57s. The
     * refresh must move the order's stored position too, or it undoes itself.
     */
    @Test
    public void anExactPositionOrderMovesWithTheRefreshedConstruction() {
        MapTiles.useSource(new EngineRefusesRawTileQuery());

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 26, 23), // occupies the exact position
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            APosition occupied = APosition.create(26, 23);
            assertFalse(occupied.isBuildableIncludeBuildings());

            Construction construction = constructionFor(occupied);
            ProductionOrder order = construction.productionOrder();
            order.markAsUsingExactPosition();
            order.setAroundPosition(occupied);

            APosition afterRefresh = RefreshConstructionPosition.refreshIfNeeded(construction);

            assertNotEquals(occupied, afterRefresh, "the construction moved off the occupied tile");
            assertEquals(afterRefresh, order.aroundPosition(),
                "the order's exact position must move with the construction, or the next order pass "
                    + "puts the occupied tile back and the builder times out on it");
        });
    }

    // =========================================================

    /**
     * The other half of symptom 3: when the construction is built from the order
     * again, an exact position that is now occupied must be discarded (and its flag
     * cleared), not handed back verbatim.
     */
    @Test
    public void anOccupiedExactPositionIsNotHonouredWhenTheOrderIsBuilt() {
        MapTiles.useSource(new EngineRefusesRawTileQuery());

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 26, 23), // occupies the exact position
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            APosition occupied = APosition.create(26, 23);

            Construction construction = constructionFor(occupied);
            ProductionOrder order = construction.productionOrder();
            order.markAsUsingExactPosition();
            order.setAroundPosition(occupied);

            APosition built = DefineExactPositionForNewConstruction.exactPositionForNewConstruction(
                AUnitType.Protoss_Cybernetics_Core, order, construction
            );

            assertNotEquals(occupied, built,
                "an occupied exact position must not be handed back to the builder");
            assertFalse(order.isUsingExactPosition(),
                "the stale exact-position request must be dropped so the search can relocate");
        });
    }

    private Construction constructionFor(APosition position) {
        Construction construction = new Construction(AUnitType.Protoss_Cybernetics_Core);
        construction.setBuilder(fake(AUnitType.Protoss_Probe, 21, 21));
        construction.setPositionToBuild(position);
        construction.setNearTo(APosition.create(24, 22));
        construction.setProductionOrder(new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, position, 0));
        return construction;
    }
}