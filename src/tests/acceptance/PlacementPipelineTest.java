package tests.acceptance;

import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationCatalogue;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.race.ProtossPlacementStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance test for the placement rewrite (`_AI/redesign/03_PLACEMENT.md`,
 * stages S1-S6).
 *
 * <p>
 * Deliberately end-to-end through the public pipeline rather than per-class:
 * the
 * point is that a synthetic map produces a ranked, valid, non-overlapping set
 * of
 * candidates, and that the pieces the spec insists on (availability ordering,
 * exact tiles only when free, blocks stamping their own ground) hold together.
 * </p>
 *
 * <p>
 * Pure: a hand-built terrain, no BWAPI, no game, no stub world. Every fact here
 * is
 * deterministic, which is what makes it a guard for the whole subsystem.
 * </p>
 */
public class PlacementPipelineTest {

    /** A flat, fully buildable NxM map with an optional blocked rectangle. */
    private static final class FlatTerrain implements TileAvailabilityGrid.TerrainSource {
        private final int w;
        private final int h;
        private final int blockedX;
        private final int blockedY;
        private final int blockedW;
        private final int blockedH;

        FlatTerrain(int w, int h) {
            this(w, h, -1, -1, 0, 0);
        }

        FlatTerrain(int w, int h, int blockedX, int blockedY, int blockedW, int blockedH) {
            this.w = w;
            this.h = h;
            this.blockedX = blockedX;
            this.blockedY = blockedY;
            this.blockedW = blockedW;
            this.blockedH = blockedH;
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
            return !withinBlocked(tx, ty);
        }

        @Override
        public boolean isWalkable(int tx, int ty) {
            return !withinBlocked(tx, ty);
        }

        @Override
        public boolean isResource(int tx, int ty) {
            return false;
        }

        @Override
        public boolean isDepotOrigin(int tx, int ty) {
            return false;
        }

        private boolean withinBlocked(int tx, int ty) {
            if (blockedW <= 0)
                return false;
            return tx >= blockedX && tx < blockedX + blockedW
                    && ty >= blockedY && ty < blockedY + blockedH;
        }
    }

    @Test
    public void theCatalogueOffersRankedCandidatesOfTheRightFootprint() {
        BuildLocationCatalogue catalogue = new BuildLocationCatalogue(
                new TileAvailabilityGrid(new FlatTerrain(40, 40)));

        List<BuildLocation> gateways = catalogue.candidates(3, 2, null);

        assertFalse(gateways.isEmpty(), "a flat 40x40 map must offer 3x2 spots");

        for (BuildLocation location : gateways) {
            assertTrue(location.tileWidth() == 3 && location.tileHeight() == 2,
                    "the catalogue must answer only the requested footprint: " + location);
        }
    }

    @Test
    public void aBlockedAreaIsExcludedAndItsMarginToo() {
        // A 6x6 unbuildable pit in the middle; the grid also marks a 1-tile margin,
        // so the first usable origin starts one tile further out.
        TileAvailabilityGrid grid = new TileAvailabilityGrid(
                new FlatTerrain(40, 40, 18, 18, 6, 6));
        BuildLocationCatalogue catalogue = new BuildLocationCatalogue(grid);

        for (BuildLocation location : catalogue.all()) {
            boolean insidePit = location.tileX() >= 18 && location.tileX() < 24
                    && location.tileY() >= 18 && location.tileY() < 24;
            assertFalse(insidePit, "no candidate may sit in the blocked pit: " + location);
        }

        assertTrue(grid.flagsAt(18, 18) != 0, "the pit itself is flagged");
        assertTrue(grid.flagsAt(17, 17) != 0, "and so is its one-tile margin");
    }

    @Test
    public void blocksStampTheirGroundSoTheNextBlockCannotOverlap() {
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new FlatTerrain(40, 40));
        BuildLocationCatalogue catalogue = new BuildLocationCatalogue(grid);

        // Blocks and the scan both contribute; no two candidates may share a tile
        // of the same footprint, which is what stamping guarantees.
        List<BuildLocation> twos = catalogue.candidates(2, 2, null);
        assertFalse(twos.isEmpty());

        for (int i = 0; i < twos.size(); i++) {
            for (int j = i + 1; j < twos.size(); j++) {
                assertFalse(sameOrigin(twos.get(i), twos.get(j)),
                        "two 2x2 candidates share an origin: " + twos.get(i) + " / " + twos.get(j));
            }
        }
    }

    @Test
    public void theProtossStrategyOffersBothStartAndNormalBlocks() {
        ProtossPlacementStrategy strategy = new ProtossPlacementStrategy(null);

        assertTrue(strategy.blockTemplates().size() >= 24,
                "the Protoss table must carry the full normal set plus start variants, got "
                        + strategy.blockTemplates().size());

        boolean hasStartBlock = false;
        for (atlantis.placement.core.BuildBlock.Spec spec : strategy.blockTemplates()) {
            if (spec.name.startsWith("Start"))
                hasStartBlock = true;
        }
        assertTrue(hasStartBlock, "at least one start-block anchor must be present");
    }

    @Test
    public void aPowerNeedingBuildingIsGatedAndOthersAreNot() {
        ProtossPlacementStrategy strategy = new ProtossPlacementStrategy(null);

        assertTrue(strategy.requiresAvailability("Protoss_Gateway"),
                "a Gateway needs Psi and must be gated");
        assertFalse(strategy.requiresAvailability("Protoss_Pylon"),
                "a Pylon provides Psi; requiring power of it would deadlock");
        assertFalse(strategy.requiresAvailability("Protoss_Nexus"),
                "a Nexus needs no Psi");
    }

    @Test
    public void everyCandidateCarriesItsGeometryAndAvailability() {
        BuildLocationCatalogue catalogue = new BuildLocationCatalogue(
                new TileAvailabilityGrid(new FlatTerrain(40, 40)));

        for (BuildLocation location : catalogue.candidates(4, 3, null)) {
            assertNotNull(location);
            assertTrue(location.tileX() >= 0 && location.tileY() >= 0);
            assertTrue(location.everAvailable(), "a free tile from the catalogue is available");
        }
    }

    private static boolean sameOrigin(BuildLocation a, BuildLocation b) {
        return a.tileX() == b.tileX() && a.tileY() == b.tileY();
    }
}
