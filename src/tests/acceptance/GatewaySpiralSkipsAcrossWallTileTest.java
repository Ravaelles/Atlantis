package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.map.position.PositionUtil;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Position;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #192: the Protoss spiral scans Euclidean rings, but a builder
 * walks the ground - a tile across the wall reads near by ring and far
 * by path (live: Gateway [105,110] 121 ground tiles from the choke at
 * Euclidean ring ~16, guard-nulled then order-cancelled in a loop).
 * The spiral must skip ground-far tiles and keep scanning instead of
 * returning the first Euclidean hit for the final guard to kill.
 *
 * <p>The stub world is flat, so the wall is faked in the ground metric:
 * everything within ring 10 of the search center reads 121 ground
 * tiles away, everything beyond reads its Euclidean fallback - the
 * production fallback when no BWEM path exists.</p>
 */
public class GatewaySpiralSkipsAcrossWallTileTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void acrossWallTileIsSkippedForReachableOne() {
        world(8, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 9, 10),
            fake(AUnitType.Protoss_Probe, 10, 9),
            fake(AUnitType.Protoss_Probe, 11, 10),
            fake(AUnitType.Protoss_Probe, 10, 11),
            fake(AUnitType.Protoss_Probe, 14, 14),
            fake(AUnitType.Protoss_Pylon, 50, 34),
            fake(AUnitType.Protoss_Pylon, 64, 30)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            AUnit builder = Select.ourWorkers().first();
            assertNotNull(builder);

            APosition nearTo = APosition.create(50, 30);
            try (MockedStatic<PositionUtil> mocked =
                     Mockito.mockStatic(PositionUtil.class, Mockito.CALLS_REAL_METHODS)) {
                mocked.when(() -> PositionUtil.groundDistanceTo(
                        any(Position.class), any(Position.class)))
                    .thenAnswer(inv -> {
                        Position s = inv.getArgument(0);
                        Position t = inv.getArgument(1);
                        int ring = Math.max(
                            Math.abs(s.getX() / 32 - 50), Math.abs(s.getY() / 32 - 30));
                        if (ring <= 10) return 121.0;

                        return Math.hypot(s.getX() - t.getX(), s.getY() - t.getY()) / 32.0;
                    });

                APosition found = ProtossPositionFinder.findStandardPositionFor(
                    builder, AUnitType.Protoss_Gateway, nearTo, 36
                );

                assertNotNull(found, "a reachable tile exists beyond the wall pocket");
                int resultRing = Math.max(
                    Math.abs(found.tx() - 50), Math.abs(found.ty() - 30));
                assertTrue(
                    resultRing > 10,
                    "the across-wall pocket (ring<=10, ground 121) is skipped, got " + found);
            }
        });
    }
}
