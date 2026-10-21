package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * STARTED-twin gap (NEXT #84): a legacy-minted warp proved by the ledger
 * with zero STARTED rows, because STARTED only fired on the adopt path.
 * One row per construction now, whoever observes the start - the warp
 * observer names the warp tile when adoption never rowed it.
 */
@Tag("production")
public class WarpStartedTwinTest extends WorldStubForTests {

    @Test
    public void warpWithoutAdoptGetsItsStartedRow() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(probe), fakeEnemies(), () -> {
            Construction construction = new Construction(AUnitType.Protoss_Gateway);
            APosition warpTile = APosition.create(40, 20);

            assertTrue(ProduceBuilding.noteStarted(construction, null, warpTile, null),
                "first observation rows the warp");

            String log = EventLog.toJsonLines();
            int key = log.lastIndexOf("\"key\":\"BUILDING_STARTED\"");
            assertTrue(key >= 0, "row exists");
            String row = log.substring(key, log.indexOf('\n', key));
            assertTrue(row.contains("\"cid\":" + construction.id()), "names the construction: " + row);
            assertTrue(row.contains("\"tile\":\"[40,20]\""), "names the warp tile, not a site: " + row);
        });
    }

    @Test
    public void secondObservationStaysSilent() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(probe), fakeEnemies(), () -> {
            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            APosition warpTile = APosition.create(41, 21);

            assertTrue(ProduceBuilding.noteStarted(construction, probe, warpTile, null));
            assertFalse(ProduceBuilding.noteStarted(construction, probe, warpTile, null),
                "one STARTED row per construction, not a heartbeat");

            String log = EventLog.toJsonLines();
            int started = 0;
            for (String line : log.split("\n")) {
                if (line.contains("\"key\":\"BUILDING_STARTED\"")
                    && line.contains("\"cid\":" + construction.id())) started++;
            }
            assertTrue(started == 1, "exactly one STARTED row for the warp: " + started);
        });
    }
}
