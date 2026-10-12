package tests.acceptance;

import atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Units already on the board at game start (army, mineral line) never pass a
 * creation event - on OpenBW, where the client attaches late, that is every
 * unit that matters. The seed sweep records them, or {@code units_first_seen}
 * stays empty and the viewer shows no units at all (measured: 0 frames in a
 * real trace with 16 logged units).
 */
public class CherryVisSeedCurrentUnitsTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void seedRecordsOursAndMinerals() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 10);
        FakeUnit mineral = fake(AUnitType.Resource_Mineral_Field, 12);
        neutralInWorld = new FakeUnit[]{ mineral };

        world(1, units(probe), units(), () -> {
            ACherryVis_UnitFirstSeen.clear();
            ACherryVis_UnitFirstSeen.seedCurrentUnits();

            String json = ACherryVis_UnitFirstSeen.get();
            assertTrue(json.contains("\"id\":" + probe.id()), "own unit seeded, got: " + json);
            assertTrue(json.contains("\"id\":" + mineral.id()), "mineral seeded, got: " + json);

            ACherryVis_UnitFirstSeen.clear();
        });
    }
}
