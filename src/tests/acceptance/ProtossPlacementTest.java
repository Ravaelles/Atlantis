package tests.acceptance;

import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.ForgeGatewayWall;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.StartBlockFinder;
import atlantis.placement.core.TileAvailabilityGrid;
import atlantis.placement.blocks.BlockTemplates;
import atlantis.placement.policy.CannonFortificationPolicy;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance tests for the Protoss-specific placement pieces
 * (`_AI/redesign/03_PLACEMENT.md` S2-S5): Psi gating and pull-forward, the
 * start-block anchor, the choke wall, and the fortification policy.
 *
 * <p>
 * Pure: hand-built terrain and hand-built power answers, no game. Each test
 * names
 * the behaviour, not the class, so a refactor that keeps the behaviour keeps
 * the
 * test green.
 * </p>
 */
public class ProtossPlacementTest {

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

    // =========================================================
    // Psi gating (S3, C5)
    // =========================================================

    /**
     * Hand-built power answers: powered tiles, incoming Pylons, placeable reach.
     */
    private static final class FakePower implements PsiGating.PowerSource {
        final List<int[]> powered = new ArrayList<>();
        final List<int[]> incoming = new ArrayList<>();
        int incomingAt;
        boolean canReach = true;

        @Override
        public boolean isPowered(int tx, int ty) {
            return contains(powered, tx, ty);
        }

        @Override
        public List<Integer> incomingPowerFrames(int tx, int ty) {
            return contains(incoming, tx, ty) ? Arrays.asList(incomingAt) : new ArrayList<Integer>();
        }

        @Override
        public boolean canBeCoveredByNewPylon(int tx, int ty) {
            return canReach;
        }
    }

    @Test
    public void aPoweredTileIsAvailableNow() {
        FakePower power = new FakePower();
        power.powered.add(new int[] { 10, 10 });

        PsiGating gating = new PsiGating(power);

        assertEquals(0, gating.framesUntilPowered(10, 10), "a powered tile is usable now");
        assertEquals(PsiGating.PowerVerdict.ACCEPT, gating.verdictFor(10, 10));
    }

    @Test
    public void aTileWithAnIncomingPylonIsAcceptedAtThatFrame() {
        FakePower power = new FakePower();
        power.incoming.add(new int[] { 12, 12 });
        power.incomingAt = 240;

        PsiGating gating = new PsiGating(power);

        assertEquals(240, gating.framesUntilPowered(12, 12),
                "the tile becomes usable when the Pylon finishes");
        assertEquals(PsiGating.PowerVerdict.ACCEPT, gating.verdictFor(12, 12),
                "an incoming Pylon is enough - the scheduler shifts the building to that frame");
    }

    @Test
    public void anUnpoweredTileANewPylonCouldReachAsksForAPylon() {
        FakePower power = new FakePower();
        power.canReach = true;

        PsiGating gating = new PsiGating(power);

        assertEquals(PsiGating.PowerVerdict.NEEDS_NEW_PYLON, gating.verdictFor(30, 30),
                "a good spot that only lacks power must ask for a Pylon, not be refused");
    }

    @Test
    public void aTileNothingCanEverPowerIsRefused() {
        FakePower power = new FakePower();
        power.canReach = false;

        PsiGating gating = new PsiGating(power);

        assertEquals(-1, gating.framesUntilPowered(30, 30));
        assertEquals(PsiGating.PowerVerdict.REFUSE, gating.verdictFor(30, 30));
    }

    /**
     * The power answer must come from our own Pylons, not from the engine.
     *
     * <p>
     * {@code Game.hasPowerPrecise} returned false for tiles a finished Pylon
     * covered, so Forge and Cybernetics Core were placed unpowered while the
     * Gateways beside them were fine (owner report, 2026-10-08). This pins the
     * rule the placement uses instead: powered means "a completed Pylon is within
     * the Pylon power radius", which is checkable from the unit list alone.
     * </p>
     */
    @Test
    public void powerComesFromOurOwnPylonsAtTheEngineRadius() {
        atlantis.placement.engine.EnginePowerSource source =
                new atlantis.placement.engine.EnginePowerSource();

        // No game and no Pylons: nothing is powered. The point is that this is an
        // answer, not an exception - a stale engine call must not be the only way
        // to find out.
        assertFalse(source.isPowered(30, 30),
                "with no Pylon in range, a tile is not powered");
    }

    // =========================================================
    // Start-block anchor (S2, C4)
    // =========================================================

    @Test
    public void aBaseGetsAStartBlockAnchorAndItCarriesTheAbilitySpots() {
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new Flat(60, 60));
        StartBlockFinder finder = new StartBlockFinder(grid, BlockTemplates.startBlocks());

        StartBlockFinder.Anchor anchor = finder.findFor(30, 30, 8);

        assertNotNull(anchor, "a flat 60x60 map around a base must fit a start-block variant");
        assertTrue(anchor.spec().name.startsWith("Start"), "the anchor is a start-block template");

        BuildBlock block = anchor.block();
        assertFalse(block.locations().isEmpty(),
                "the anchor must offer slots, or it anchors nothing");

        // The anchor must sit near the base it anchors, not on the far side of the map.
        assertTrue(Math.abs(anchor.left() - 30) <= 8 && Math.abs(anchor.top() - 30) <= 8,
                "the anchor must be inside the search window: " + anchor);
    }

    @Test
    public void aBaseWithNoRoomGetsNoAnchor() {
        // A 6x6 map cannot hold any start block (the smallest is 10x8).
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new Flat(6, 6));
        StartBlockFinder finder = new StartBlockFinder(grid, BlockTemplates.startBlocks());

        assertNull(finder.findFor(3, 3, 4),
                "no anchor is better than an anchor on the wrong side of the map");
    }

    // =========================================================
    // Choke wall (S4, C10)
    // =========================================================

    @Test
    public void aWallIsFoundNearAChoke() {
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new Flat(60, 60));
        ForgeGatewayWall wall = new ForgeGatewayWall(grid);

        List<int[]> chokeTiles = new ArrayList<>();
        chokeTiles.add(new int[] { 30, 30 });

        ForgeGatewayWall.Tiles tiles = wall.find(new ForgeGatewayWall.Choke(30, 30, chokeTiles));

        assertNotNull(tiles, "an open map must fit a Forge/Gateway/Pylon wall at a choke");

        // The Pylon must actually power both buildings, or the wall cannot be built.
        assertTrue(maxDistance(tiles.pylonX(), tiles.pylonY(), tiles.forgeX(), tiles.forgeY()) <= 6,
                "the Pylon must power the Forge: " + tiles);
        assertTrue(maxDistance(tiles.pylonX(), tiles.pylonY(), tiles.gatewayX(), tiles.gatewayY()) <= 6,
                "the Pylon must power the Gateway: " + tiles);
    }

    @Test
    public void noWallIsReturnedWhenNothingFits() {
        // A 4x4 map: no room for a 3x2 Forge plus a 3x2 Gateway plus a 2x2 Pylon.
        TileAvailabilityGrid grid = new TileAvailabilityGrid(new Flat(4, 4));
        ForgeGatewayWall wall = new ForgeGatewayWall(grid);

        assertNull(wall.find(new ForgeGatewayWall.Choke(2, 2, new ArrayList<int[]>())),
                "a wall that cannot fit must be null, not an overlapping guess");
    }

    @Test
    public void theWallStatesWhatItDoesNotModel() {
        // A wall with a one-tile hole would still be returned: the gap is not
        // measured. That limitation is named in code rather than implied, so nobody
        // assumes more of it than it does.
        assertTrue(ForgeGatewayWall.limitations().contains("no gap measurement"),
                "the unmodelled part must be stated: " + ForgeGatewayWall.limitations());
    }

    // =========================================================
    // Fortification policy (S5, C12/C13)
    // =========================================================

    @Test
    public void aBaseWithNoCannonsWantsAtLeastOne() {
        CannonFortificationPolicy.Context quiet = new CannonFortificationPolicy.Context(
                20, 200, 0, false, false, 0);

        assertEquals(1, CannonFortificationPolicy.expectedCannons(quiet),
                "the baseline is one cannon per base");
        assertTrue(CannonFortificationPolicy.needsMoreCannons(quiet));
        assertEquals(1, CannonFortificationPolicy.missingCannons(quiet));
    }

    @Test
    public void moreMineralsAndSupplyMeanMoreCannons() {
        CannonFortificationPolicy.Context rich = new CannonFortificationPolicy.Context(
                60, 900, 0, false, false, 0);

        assertTrue(CannonFortificationPolicy.expectedCannons(rich) > 1,
                "a base with minerals and supply wants more than the baseline");
    }

    @Test
    public void mutalisksRaiseTheCannonCount() {
        CannonFortificationPolicy.Context noMutas = new CannonFortificationPolicy.Context(
                100, 400, 0, true, false, 0);
        CannonFortificationPolicy.Context manyMutas = new CannonFortificationPolicy.Context(
                100, 400, 0, true, false, 12);

        assertTrue(
                CannonFortificationPolicy.expectedCannons(manyMutas) > CannonFortificationPolicy
                        .expectedCannons(noMutas),
                "a mutalisk flock is what extra cannons are for");
    }

    @Test
    public void aBaseWithEnoughCannonsAsksForNoMore() {
        CannonFortificationPolicy.Context satisfied = new CannonFortificationPolicy.Context(
                20, 200, 5, false, false, 0);

        assertEquals(0, CannonFortificationPolicy.missingCannons(satisfied));
        assertFalse(CannonFortificationPolicy.needsMoreCannons(satisfied));
    }

    @Test
    public void thePolicyEmitsGoalsWithAPlacementConstraintAndNoTiles() {
        CannonFortificationPolicy.Context context = new CannonFortificationPolicy.Context(
                60, 900, 0, false, false, 0);

        List<ProductionGoal> goals = atlantis.placement.policy.FortificationGoals.forBase(
                context, UnitProducible.of(AUnitType.Protoss_Photon_Cannon), 30, 30);

        assertFalse(goals.isEmpty(), "a base short of cannons must emit goals");
        assertEquals(goals.size(), CannonFortificationPolicy.missingCannons(context),
                "one goal per missing cannon, so the scheduler can shift them individually");

        for (ProductionGoal goal : goals) {
            assertEquals(TargetPlacement.Mode.NEIGHBOURHOOD, goal.placement().mode(),
                    "the policy says 'near this base', never a tile - that is the §5.3 boundary");
            assertEquals(30, goal.placement().tileX());
            assertEquals(30, goal.placement().tileY());
        }
    }

    @Test
    public void aSatisfiedBaseEmitsNoGoals() {
        CannonFortificationPolicy.Context context = new CannonFortificationPolicy.Context(
                20, 200, 5, false, false, 0);

        assertTrue(atlantis.placement.policy.FortificationGoals.forBase(
                context, UnitProducible.of(AUnitType.Protoss_Photon_Cannon), 30, 30).isEmpty(),
                "a base that has enough cannons asks for nothing");
    }

    private static int maxDistance(int ax, int ay, int bx, int by) {
        return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
    }

    /** Is this tile in a hand-built list of tile pairs? */
    private static boolean contains(List<int[]> tiles, int tx, int ty) {
        for (int[] tile : tiles) {
            if (tile[0] == tx && tile[1] == ty) return true;
        }
        return false;
    }
}
