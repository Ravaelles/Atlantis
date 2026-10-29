package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Builder hand-overs twin once each (CONSTRUCTION_REQUESTED): an assigned
 * builder that never travels, or a builder silently taken away, reads here
 * instead of in a post-mortem (measured live: a Nexus held builder #167
 * from creation and never warped, with zero twin rows anywhere - the
 * assignment was invisible).
 */
@Tag("production")
public class BuilderAssignedTwinTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    private static int rowsFor(int cid) {
        int count = 0;

        for (String line : EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"key\":\"CONSTRUCTION_REQUESTED\"")
                && line.contains("\"cid\":" + cid)) count++;
        }

        return count;
    }

    private static boolean anyRowFor(int cid, String fragment) {
        for (String line : EventLog.toJsonLines().split("\n")) {
            if (line.contains("\"key\":\"CONSTRUCTION_REQUESTED\"")
                && line.contains("\"cid\":" + cid)
                && line.contains(fragment)) return true;
        }

        return false;
    }

    @Test
    public void assignmentUnassignmentAndReassignmentRowOnceEach() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit other = fake(AUnitType.Protoss_Probe, 31, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            probe,
            other
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setPositionToBuild(APosition.create(12, 10));

            construction.setBuilder(probe);
            construction.setBuilder(probe);
            assertEquals(1, rowsFor(construction.id()), "re-affirming the same builder stays silent");

            construction.setBuilder(null);
            construction.setBuilder(other);
            assertEquals(3, rowsFor(construction.id()), "unassign and hand-over row once each");

            assertTrue(anyRowFor(construction.id(), "\"builder\":" + other.id()),
                "a row names the new builder");
            assertTrue(anyRowFor(construction.id(), "\"prev\":" + probe.id()),
                "and the one it replaced");
        });
    }
}
