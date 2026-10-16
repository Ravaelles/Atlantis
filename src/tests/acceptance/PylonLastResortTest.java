package tests.acceptance;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A Pylon search that finds nothing must not happen: doctrine decides
 * whenever it finds anything, and only the total-failure outcome gets a
 * last resort.
 *
 * <p><b>Measured (OpenBW {@code 20261016_104914_long}, corner Nexus
 * [119,10]):</b> ~3000 consecutive refusals (physical footprint, street
 * grid, edge margins), {@code PYLON_FIND_FAILED} 12x, then {@code Can't
 * find place for Pylon} 3x. The relaxed-margin pass alone cannot cover
 * the corner - the grid kills what the margin spares. So the ladder is
 * strict, relaxed margins, then the first tile in ring order the engine
 * itself will take (no grid, no spacing, no margins). This is not a
 * corner reproduction (stub terrain is flat) and not a doctrine change:
 * the rescued tile below is refused by the grid on purpose, on a
 * comfortable bank so no escape hatch is open.</p>
 */
public class PylonLastResortTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void gridRefusedButBuildableTileIsRescued() {
        fired = false;
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            // NOTE: off the candidate tile on purpose - the stub's
            // isBuildable counts every unit (no builder exclusion; that
            // contract lives in production, NEXT #49), so a probe standing
            // on the tile would fail engine truth here and prove nothing
            // about doctrine.
            fake(AUnitType.Protoss_Probe, 44, 44)
        ), fakeEnemies(), () -> {
            if (fired) return;
            fired = true;

            currentSupplyUsed = 10;
            currentSupplyTotal = 25;
            currentMinerals = 400;
            currentGas = 100;

            AUnit builder = Select.ourWorkers().first();

            // tx 44 sits on the street grid (44 % 11 == 0) and maxDist 1
            // examines this tile only: both doctrine passes refuse it, but
            // stub ground is empty so the engine-truth scan takes it.
            APosition found = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(44, 41), 1
            );

            assertEquals(
                APosition.create(44, 41), found,
                "the last resort returns the engine-buildable tile doctrine refused");
            assertFalse(
                ProtossPositionFinder.lastRefusalBreakdown().isEmpty(),
                "the strict pass still records why doctrine refused it");
        });
    }

    @Test
    public void fullyOccupiedBlockStillYieldsNothing() {
        fired = false;
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Probe, 10, 10)
        ), fakeEnemies(), () -> {
            if (fired) return;
            fired = true;

            currentSupplyUsed = 10;
            currentSupplyTotal = 25;
            currentMinerals = 400;

            AUnit builder = Select.ourWorkers().first();

            APosition found = ProtossPositionFinder.findStandardPositionFor(
                builder, AUnitType.Protoss_Pylon, APosition.create(10, 10), 1
            );

            assertNull(
                found,
                "the last resort scans engine truth, it does not invent tiles");
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
