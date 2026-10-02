package tests.unit.world;

import atlantis.core.world.UnitSnapshot;
import atlantis.units.AUnitType;
import atlantis.units.fogged.FakeFoggedUnit;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Stage E: fogged units expose their last-known state as a snapshot.
 *
 * <p>Deliberately not run inside a stub world: the projection is pure
 * delegation, and world-based tests leak static mocks across test classes
 * (see _AI/NOTES.md), which makes suite results order-dependent.</p>
 */
public class FoggedSnapshotTest {

    @Test
    void foggedUnitProjectsLastKnownState() {
        FakeUnit zergling = new FakeUnit(AUnitType.Zerg_Zergling, 40, 20);
        FakeFoggedUnit fogged = FakeFoggedUnit.fromFake(zergling);
        UnitSnapshot snapshot = fogged.snapshot();

        assertEquals(zergling.id(), snapshot.id());
        assertEquals(AUnitType.Zerg_Zergling, snapshot.type());
        assertEquals(fogged.position(), snapshot.position());
        assertEquals(fogged.hp(), snapshot.hp());
        assertEquals(fogged.shields(), snapshot.shields());
    }
}
