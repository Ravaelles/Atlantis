package tests.unit;

import atlantis.placement.core.PsiGating;
import atlantis.placement.engine.EnginePowerSource;
import atlantis.placement.race.ProtossPlacementStrategy;
import atlantis.placement.core.RacePlacementStrategy;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import bwapi.TilePosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.fakes.TileMapSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A power-needing building is judged at its footprint center, not its
 * origin tile (NEXT: live 2026-11-03 vs Steamhammer, zero Gateways).
 * A 4x3 Gateway at [123,26] with a warping Pylon at [121,22] reads
 * powered on the origin tile (dy=144, in the mask) while the engine
 * enforces the center (dy=176, out) - the row walked 82 seconds,
 * refused every Build, timed out, re-queued and died again with the
 * bank full. The V2-direct verdict below is the only power gate that
 * read the origin; the funnel and the legacy spiral both read the
 * footprint and would have declined.
 *
 * <p>Wiring pin (no decision predicate owns footprint-vs-origin, the
 * verdict does): the edge tile must not come back AVAILABLE, and a
 * truly powered tile must still come back AVAILABLE.</p>
 */
@Tag("placement")
public class GatewayPsiEdgeTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private ProtossPlacementStrategy strategy() {
        return new ProtossPlacementStrategy(new PsiGating(new EnginePowerSource()));
    }

    @Test
    public void gatewayAtPsiEdgeIsNotServedAsIs() {
        FakeUnit warpingPylon = fake(AUnitType.Protoss_Pylon, 121, 22);
        warpingPylon.setCompleted(false);
        warpingPylon.hp = warpingPylon.maxHp() / 2;

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 114, 11),
            fake(AUnitType.Protoss_Probe, 115, 12),
            warpingPylon
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                // The trap, documented: the origin tile reads powered.
                assertEquals(PsiGating.PowerVerdict.ACCEPT,
                    new PsiGating(new EnginePowerSource()).verdictFor(123, 26),
                    "tile-point reads ACCEPT - this is the address the engine does NOT enforce");

                // The rule: a 4x3 Gateway there must not be served as-is.
                RacePlacementStrategy.AvailabilityVerdict verdict = strategy().availabilityVerdict(
                    123, 26, AUnitType.Protoss_Gateway.name());
                assertNotEquals(RacePlacementStrategy.AvailabilityVerdict.AVAILABLE, verdict,
                    "footprint center dy=176 is outside the psi field: serving it "
                        + "walks a builder into 82 seconds of refused Builds");
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    @Test
    public void poweredGatewayStillServed() {
        FakeUnit warpingPylon = fake(AUnitType.Protoss_Pylon, 121, 22);
        warpingPylon.setCompleted(false);
        warpingPylon.hp = warpingPylon.maxHp() / 2;

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 114, 11),
            fake(AUnitType.Protoss_Probe, 115, 12),
            warpingPylon
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                // Same warping Pylon, genuinely covered 4x3 site (center
                // dy=112, in the mask): the fix must not move the working shape.
                assertEquals(RacePlacementStrategy.AvailabilityVerdict.AVAILABLE, strategy().availabilityVerdict(
                    121, 24, AUnitType.Protoss_Gateway.name()),
                    "a truly powered site keeps serving");
                assertTrue(strategy().framesUntilAvailable(
                    121, 24, AUnitType.Protoss_Gateway.name()) > 0,
                    "the Pylon is still warping, so the scheduler waits for its completion frame");
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    /**
     * Live shape 2026-11-07 vs Steamhammer (TauCross, zero Gateways all
     * game): a finished Pylon at origin [15,49] centers on (16,50), and
     * the engine reads power from the origin - a 4x3 Gateway at [12,54]
     * sits dy=176px out. The power read fed tx()/ty() (center tiles on
     * a live engine, origin tiles in stubs) into {@code PsiField}, which
     * centers the Pylon itself - so live double-centered the field and
     * phantom-powered the rim the stubs never saw. The stub Pylon below
     * is engine-posed (center pixels, origin tilePosition) so the pin
     * reads what the game read, not what the stubs assume.
     */
    @Test
    public void liveCenteredPylonDoesNotPhantomPower() {
        FakeUnit livePylon = new FakeUnit(AUnitType.Protoss_Pylon, 15, 49) {
            @Override
            public TilePosition tilePosition() {
                return new TilePosition(15, 49);
            }
        };
        livePylon.position = APosition.createFromPixels(15 * 32 + 32, 49 * 32 + 32);

        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            livePylon
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;
            TileMapSource.everythingFine().install();
            try {
                assertFalse(new EnginePowerSource().isPowered(12, 54, 4, 3),
                    "origin [15,49] leaves the Gateway center dy=176 out: no phantom rim power");
                assertEquals(RacePlacementStrategy.AvailabilityVerdict.NEEDS_SUPPORT,
                    strategy().availabilityVerdict(12, 54, AUnitType.Protoss_Gateway.name()),
                    "an unpowered slot asks for its Pylon instead of serving the Gateway");
            }
            finally {
                TileMapSource.uninstall();
            }
        });
    }

    @Test
    public void pylonNeedsNoPower() {        world(3, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 114, 11),
            fake(AUnitType.Protoss_Probe, 115, 12)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            // The planner checks requiresAvailability before the verdict, so
            // a Pylon never reaches the power gate at all - pin that dispatch
            // instead of calling the power gate for a Pylon directly.
            assertFalse(strategy().requiresAvailability(AUnitType.Protoss_Pylon.name()),
                "Pylons need no power: the availability gate is skipped for them");
            assertTrue(strategy().requiresAvailability(AUnitType.Protoss_Gateway.name()),
                "Gateways do need power: the gate must answer for them");
        });
    }
}
