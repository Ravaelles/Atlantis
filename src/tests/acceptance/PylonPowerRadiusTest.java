package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
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
 * decides whether a Protoss building may stand somewhere.
 *
 * <p>
 * Both planners answer it from the same rule now ({@code PsiField}, the
 * engine's mask read from {@code /sc-ai/OpenBW/openbw/bwgame.h}): the legacy
 * search through {@code IsPoweredByAPylon}, the catalogue planner through its
 * power source. A drift between them means one planner accepts tiles the
 * other refuses, which is exactly the class of disagreement the placement
 * investigation kept running into - so this test pins the legacy side of the
 * agreement on the game-measured tiles, and {@code PsiFieldTest} pins the
 * mask itself without a world.
 * </p>
 */
public class PylonPowerRadiusTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    /**
     * A tile beside the Pylon is powered; one well outside the field is not.
     * Both halves are needed - a check that always answered true would pass a
     * one-sided test while letting buildings be placed unpowered.
     */
    @Test
    public void aTileInsideTheFieldIsPoweredAndOneOutsideItIsNot() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 50, 50);

        world(1, fakeOurs(pylon), fakeEnemies(), () -> {
            assertTrue(
                    IsPoweredByAPylon.check(APosition.create(52, 50), AUnitType.Protoss_Gateway),
                    "a tile two tiles from the Pylon is inside its field");

            assertFalse(
                    IsPoweredByAPylon.check(APosition.create(50 + 12, 50), AUnitType.Protoss_Gateway),
                    "a tile 12 tiles away is well outside the field");
        });
    }

    /**
     * The game-measured case (20261013_112034_long): a Gateway requested at
     * [14,51] with a finished Pylon at [16,46] was refused by the engine 315
     * times on power - while the old Chebyshev rule called it powered. The
     * finder must agree with the engine, or it keeps requesting dead tiles.
     */
    @Test
    public void theGameMeasuredUnpoweredSiteIsRefused() {
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 16, 46);

        world(1, fakeOurs(pylon), fakeEnemies(), () -> {
            assertFalse(
                    IsPoweredByAPylon.check(APosition.create(14, 51), AUnitType.Protoss_Gateway),
                    "5 tiles below the Pylon is outside the engine's 160px vertical bound");

            assertTrue(
                    IsPoweredByAPylon.check(APosition.create(14, 48), AUnitType.Protoss_Gateway),
                    "two tiles nearer is inside - the boundary is razor-thin here");
        });
    }

    /**
     * The shared chain refuses an unpowered site with a named reason (NEXT
     * #50): the Protoss sweep has its own gate, but exact-position orders
     * reach this chain without it. A Pylon itself never needs power.
     */
    @Test
    public void theSharedChainRefusesAnUnpoweredSiteByName() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 16, 46);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 15, 50);

        world(1, fakeOurs(nexus, pylon, probe), fakeEnemies(), () -> {
            assertFalse(
                    PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
                            probe, AUnitType.Protoss_Gateway, APosition.create(14, 51), probe),
                    "a Gateway outside every Pylon field must not pass the shared chain");

            assertEquals(
                    "Not powered by a Pylon", AbstractPositionFinder._STATUS,
                    "the refusal names power, so the next hunt starts at the cause");
        });
    }
}
