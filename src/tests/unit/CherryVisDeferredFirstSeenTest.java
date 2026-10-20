package tests.unit;

import atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A mobile unit must be first-seen at its OWN position, not its producer's.
 *
 * <p><b>Measured on OpenBW (NEXT #68 follow-up):</b> every late Probe recorded
 * at the Nexus - a unit being trained reports its producer's position, and the
 * viewer (nearest same-type within 25px, no retry) matches nothing in the
 * crowd, so every future unit reads "i???" while seeded ones match. Mobile
 * units are therefore deferred a few frames past completion, until they walk
 * out with a position of their own; the same frame in the trace and in the
 * replay means distance zero, so the delay cannot hurt matching.</p>
 */
@Tag("infra")
public class CherryVisDeferredFirstSeenTest {

    @AfterEach
    public void clearFirstSeen() {
        ACherryVis_UnitFirstSeen.clear();
    }

    @Test
    public void deferredUnitRecordsItsOwnPositionAfterWalkingOut() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 120, 60);

        ACherryVis_UnitFirstSeen.defer(probe.id(), 100);

        ACherryVis_UnitFirstSeen.processPending(id -> probe, 103);
        assertFalse(
            ACherryVis_UnitFirstSeen.get().contains("\"id\":" + probe.id()),
            "too early: the unit has not walked out yet, nothing recorded"
        );

        ACherryVis_UnitFirstSeen.processPending(id -> probe, 110);
        String json = ACherryVis_UnitFirstSeen.get();
        assertTrue(
            json.contains("\"id\":" + probe.id()),
            "after the delay the unit records with its current position"
        );
        assertTrue(
            json.contains("\"110\":[{\"id\":" + probe.id()),
            "the record carries the frame it was taken at, not the completion frame"
        );
    }

    @Test
    public void deadOrUnknownUnitsAreDroppedSilently() {
        ACherryVis_UnitFirstSeen.defer(4242, 100);

        ACherryVis_UnitFirstSeen.processPending(id -> null, 200);

        assertFalse(
            ACherryVis_UnitFirstSeen.get().contains("\"id\":4242"),
            "a unit that is gone by processing time must not linger in pending"
        );
        ACherryVis_UnitFirstSeen.processPending(id -> null, 300);
    }
}
