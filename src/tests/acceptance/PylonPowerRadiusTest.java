package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.placement.core.PsiGating;
import atlantis.production.constructions.position.conditions.can_build_here.IsPoweredByAPylon;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Is this tile inside a finished Pylon's power field?" - the question that
 * decides
 * whether a Protoss building may stand somewhere.
 *
 * <p>
 * Two implementations answer it, on purpose, for two different planners:
 * {@code IsPoweredByAPylon} (the legacy search) and {@code PsiGating} (the
 * rewritten
 * catalogue planner). The class javadoc of the first says the two must be kept
 * in step -
 * and until now nothing enforced that, because neither had a test (measured
 * 2026-10-10:
 * {@code IsPoweredByAPylon} had zero references under {@code src/tests}). A
 * radius that
 * drifts between them means one planner accepts tiles the other refuses, which
 * is exactly
 * the class of disagreement this whole placement investigation kept running
 * into.
 * </p>
 */
public class PylonPowerRadiusTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * The radius is 6 tiles in the engine and both implementations must agree on
     * it. A
     * split here is silent: each planner would simply behave differently.
     */
    @Test
    public void bothPlannersUseTheEnginesSixTileRadius() {
        assertEquals(
                IsPoweredByAPylon.PYLON_POWER_RADIUS_TILES,
                PsiGating.PYLON_POWER_RADIUS_TILES,
                "the legacy finder and the catalogue planner must not disagree about the "
                        + "Pylon power radius - a drift makes one accept tiles the other refuses");

        assertEquals(6, IsPoweredByAPylon.PYLON_POWER_RADIUS_TILES,
                "BWAPI's Pylon power radius is 6 tiles; a hand-typed change here is a bug, "
                        + "not a tuning knob");
    }

    /**
     * A tile beside the Pylon is powered; one well outside the radius is not. Both
     * halves
     * are needed - a check that always answered true would pass a one-sided test
     * while
     * letting buildings be placed unpowered.
     */
    @Test
    public void aTileInsideTheRadiusIsPoweredAndOneOutsideItIsNot() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 50, 50);

        world(1, fakeOurs(pylon), fakeEnemies(), () -> {
            assertTrue(
                    IsPoweredByAPylon.check(APosition.create(52, 50)),
                    "a tile two tiles from the Pylon is inside its field");

            assertFalse(
                    IsPoweredByAPylon.check(APosition.create(50 + 12, 50)),
                    "a tile 12 tiles away is well outside a 6-tile radius");
        });
    }

    /**
     * The distance is measured from the Pylon's <b>footprint</b>, and a Pylon is
     * 2x2. A
     * tile diagonally off the far corner is therefore 6+1 tiles from the Pylon's
     * tile
     * origin but still 6 from the nearest tile of its footprint - the exact
     * arithmetic
     * the class comment says was wrong once and made genuinely powered tiles read
     * as
     * unpowered.
     */
    @Test
    public void theDistanceIsMeasuredFromTheFootprintNotTheOriginTile() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 60, 60);

        world(1, fakeOurs(pylon), fakeEnemies(), () -> {
            // Pylon occupies (60,60)-(61,61). This tile is 6 tiles diagonally from the
            // far corner of the footprint and must still be powered.
            APosition diagonalOffFootprint = APosition.create(61 + 6, 61 + 6);

            assertTrue(
                    IsPoweredByAPylon.check(diagonalOffFootprint),
                    "6 tiles from the nearest footprint tile is inside the radius; measuring "
                            + "from the origin tile instead would wrongly refuse it");
        });
    }
}
