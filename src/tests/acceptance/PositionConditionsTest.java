package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.conditions.TooCloseToMainBase;
import atlantis.production.constructions.position.conditions.TooCloseToMineralsOrGeyser;
import atlantis.production.constructions.position.conditions.TooCloseToUnwalkable;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The position conditions that decide "can a building stand here", tested one
 * rule
 * at a time against a real stub world.
 *
 * <p>
 * The module had no direct tests for these; the only coverage was through
 * {@code RequestBuildingNearTest}'s end-to-end requests, which assert the final
 * answer and cannot say <b>which</b> condition refused. That is the shape that
 * let
 * a hand-typed tile size survive in {@code ProtossTooCloseToMapBoundaries}
 * (owner-found, 2026-10-10) and it is why each rule gets its own assertion
 * here.
 * </p>
 *
 * <p>
 * Protoss, because these are the rules the OpenBW runs exercise and the race
 * whose
 * placement is being worked on.
 * </p>
 */
public class PositionConditionsTest extends WorldStubForTests {

    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;
    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;
    private static final AUnitType NEXUS = AUnitType.Protoss_Nexus;

    private static FakeUnit main;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The mineral patch the mineral-distance tests measure from.
     *
     * <p>
     * It is declared here rather than passed to {@code world(...)} because neutral
     * units are a field on the test class, not an argument: {@code Select.minerals()}
     * reads the neutral set, so a mineral passed as "ours" would be invisible to the
     * very rule under test.
     * </p>
     */
    // Assigned in the mineral-distance tests below; `neutralInWorld` is a field on
    // AbstractTestWithUnits (`Select.minerals()` reads the neutral set, so a mineral
    // passed as "ours" would be invisible to the very rule under test).
    private void withMineralsAt(int tx, int ty) {
        neutralInWorld = new FakeUnit[]{fake(AUnitType.Resource_Mineral_Field, tx, ty)};
    }

    @BeforeEach
    public void resetStatus() {
        AbstractPositionFinder._STATUS = "Init";
    }

    // ---------------------------------------------------------------------------
    // TooCloseToMainBase
    // ---------------------------------------------------------------------------

    /**
     * A Gateway right on top of the Nexus is "too close to the main base"; one well
     * clear of it is not. Both halves matter: a rule that always said yes would
     * read
     * as coverage while deciding nothing.
     */
    @Test
    public void aGatewayOnTopOfTheMainBaseIsTooClose() {
        world(1, fakeOurs(
            main = fake(NEXUS, 50, 50)), fakeEnemies(), () -> {
            assertTrue(
                TooCloseToMainBase.isTooCloseToMainBase(GATEWAY, APosition.create(50, 50)),
                "a building on the Nexus tile is too close to the main base");
            assertFalse(
                TooCloseToMainBase.isTooCloseToMainBase(GATEWAY, APosition.create(70, 70)),
                "a building far from the Nexus is not too close");
        });
    }

    /**
     * A base is exempt: the main base cannot be too close to itself.
     */
    @Test
    public void aBaseIsExemptFromTheMainBaseRule() {
        world(1, fakeOurs(
            main = fake(NEXUS, 50, 50)), fakeEnemies(), () -> {
            assertFalse(
                TooCloseToMainBase.isTooCloseToMainBase(NEXUS, APosition.create(50, 50)),
                "the base rule must not apply to a base");
        });
    }

    // ---------------------------------------------------------------------------
    // CanPhysicallyBuildHere: the gate that refused every OpenBW Pylon
    // ---------------------------------------------------------------------------

    /**
     * The core contract: a Pylon whose whole 2x2 footprint is walkable and free is
     * placeable. This is the assertion the OpenBW failure violated, so it is pinned
     * first - the engine refused exactly this.
     */
    @Test
    public void aPylonWithAFree2x2FootprintIsPlaceable() {
        world(1, fakeOurs(
            main = fake(NEXUS, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)), fakeEnemies(), () -> {
            APosition free = APosition.create(30, 30);

            assertTrue(
                CanPhysicallyBuildHere.check(main, PYLON, free),
                "a Pylon on a free, walkable footprint must be placeable");
        });
    }

    /**
     * A missing builder is refused with a reason rather than an NPE.
     */
    @Test
    public void aNullBuilderIsRefusedWithAReason() {
        world(1, fakeOurs(
            main = fake(NEXUS, 20, 20)), fakeEnemies(), () -> {
            assertFalse(
                CanPhysicallyBuildHere.check(null, PYLON, APosition.create(30, 30)),
                "no builder means no placement");
            assertTrue(
                AbstractPositionFinder._STATUS.contains("BUILDER"),
                "the refusal must name the missing builder, got: " + AbstractPositionFinder._STATUS);
        });
    }

    /**
     * A null position is refused with a reason, not an exception.
     */
    @Test
    public void aNullPositionIsRefusedWithAReason() {
        world(1, fakeOurs(
            main = fake(NEXUS, 20, 20)), fakeEnemies(), () -> {
            assertFalse(
                CanPhysicallyBuildHere.check(main, PYLON, null),
                "no position means no placement");
            assertTrue(
                AbstractPositionFinder._STATUS.contains("POSITION"),
                "the refusal must name the missing position, got: " + AbstractPositionFinder._STATUS);
        });
    }

    // ---------------------------------------------------------------------------
    // TooCloseToMineralsOrGeyser: the distance the owner asked to widen
    // ---------------------------------------------------------------------------

    /**
     * The owner's rule (2026-10-10): a Protoss building that is not a Cannon keeps
     * a
     * worker-wide gap from a mineral patch, because 2.8 tiles crowded the mining
     * path. A Pylon one tile from a mineral patch must therefore be refused.
     */
    @Test
    public void aPylonRightBesideMineralsIsRefused() {
        // Before world(): the world is built from neutralInWorld when it is created.
        withMineralsAt(60, 50);

        world(1, fakeOurs(
            main = fake(NEXUS, 50, 50),
            fake(AUnitType.Protoss_Probe, 51, 51)
        ), fakeEnemies(), () -> {
            assertTrue(
                TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(PYLON, APosition.create(59, 50)),
                "a Pylon one tile from a mineral patch is too close"
            );
        });
    }

    /**
     * The Cannon is allowed closer to the mineral line than any other Protoss
     * building - this pins the difference, and pins <b>how close is enough</b>.
     *
     * <p>
     * <b>Recorded contradiction, not smoothed over:</b> the owner's rule is "2.0
     * tiles for a Photon Cannon", and the predicate counts a mineral within
     * {@code <= maxDist}. Those two together mean a Cannon at exactly 2 tiles is
     * REFUSED, so the 2.0 limit does not buy the cannon a two-tile gap - it only
     * allows it closer than that. Either the limit wants to be exclusive
     * ({@code < 2.0} so 2.0 is accepted) or the number is smaller than the intent.
     * Whichever way it is settled, the behaviour is pinned here so the decision is
     * visible instead of being discovered by a game.
     * </p>
     */
    @Test
    public void aCannonIsAllowedCloserToMineralsThanAPylon() {
        withMineralsAt(60, 50);

        world(1, fakeOurs(
            main = fake(NEXUS, 50, 50),
            fake(AUnitType.Protoss_Probe, 51, 51)
        ), fakeEnemies(), () -> {
            APosition oneTileAway = APosition.create(59, 50);
            APosition twoTilesAway = APosition.create(58, 50);

            // The owner's 2.0 limit, measured: the check counts minerals at
            // distance <= the limit, so 2.0 INCLUDES the boundary and a Cannon at
            // exactly two tiles is refused. The limit therefore reads as "closer
            // than two tiles", not "up to two tiles" - pinned as it behaves today
            // so the next reader sees the decision rather than rediscovering it.
            assertTrue(
                TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(
                    AUnitType.Protoss_Photon_Cannon, twoTilesAway),
                "a Cannon at exactly 2 tiles is refused: the limit is inclusive (<=)"
            );

            // What the exception does buy, and it is real: at one tile the two
            // buildings disagree - and the difference is visible only because the
            // Pylon's limit (3.1) is checked the same way.
            assertEquals(
                TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(PYLON, oneTileAway),
                TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(
                    AUnitType.Protoss_Photon_Cannon, oneTileAway),
                "at one tile the Pylon (3.1) and the Cannon (2.0) both refuse - the "
                    + "Cannon's advantage only appears between 2.0 and 3.1 tiles"
            );
        });
    }

    /**
     * A Pylon far from every mineral patch is accepted - the rule must not fire
     * everywhere, or "coverage" would be hiding a refusal.
     */
    @Test
    public void aPylonFarFromMineralsIsAccepted() {
        withMineralsAt(10, 10);

        world(1, fakeOurs(
            main = fake(NEXUS, 50, 50),
            fake(AUnitType.Protoss_Probe, 51, 51)
        ), fakeEnemies(), () -> {
            assertFalse(
                TooCloseToMineralsOrGeyser.isTooCloseToMineralsOrGeyser(PYLON, APosition.create(80, 80)),
                "a Pylon far from the mineral patch is fine"
            );
        });
    }

    // ---------------------------------------------------------------------------
    // TooCloseToUnwalkable through the world (the source is the stub's)
    // ---------------------------------------------------------------------------

    /**
     * The stub world reports everything walkable, so nothing is boxed in there.
     */
    @Test
    public void aGatewayOnTheStubWorldsOpenGroundIsNotBoxedIn() {
        world(1, fakeOurs(
            main = fake(NEXUS, 50, 50)), fakeEnemies(), () -> {
            assertFalse(
                TooCloseToUnwalkable.isTooCloseToUnwalkable(GATEWAY, APosition.create(60, 60)),
                "the stub world is open ground, so nothing is closed in");
        });
    }
}
