package tests.unit;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pinning the radius family semantics before its extraction out of
 * {@code Selection} (NEXT #10, slice 1): distances are tile Chebyshev-ish
 * euclidean on stub positions, the anchor unit itself is always included,
 * air units skip ground-distance filtering, counts agree with sets.
 *
 * <p>Written against the current implementation; the extraction that
 * follows must keep every assertion green without touching them.</p>
 */
public class SelectionRadiusTest extends WorldStubForTests {

    @Test
    public void inRadiusByUnitAndPositionAgree() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();
            assertEquals(3, ours.size());

            assertEquals(2, ours.inRadius(5, a).size());
            assertEquals(2, ours.inRadius(5, APosition.create(10, 10)).size());
            assertEquals(1, ours.inRadius(1, a).size());
            assertTrue(ours.inRadius(30, a).size() == 3);
        });
    }

    @Test
    public void inRadiusOfSelectionUsesNearestMember() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();
            Selection onlyB = ours.ofType(AUnitType.Protoss_Probe).exclude(a);

            assertEquals(1, onlyB.size());
            assertEquals(2, ours.inRadius(5, onlyB).size());
            assertEquals(1, ours.inRadius(1, onlyB).size());
        });
    }

    @Test
    public void notInRadiusIsTheComplement() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(1, ours.notInRadius(5, APosition.create(10, 10)).size());
            assertEquals(3, ours.notInRadius(1, APosition.create(50, 50)).size());
        });
    }

    @Test
    public void countInRadiusAgreesWithSet() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(
                ours.inRadius(5, APosition.create(10, 10)).size(),
                ours.countInRadius(5, APosition.create(10, 10)));
            assertEquals(2, ours.countInRadius(5, a));
        });
    }

    @Test
    public void groundRadiusMatchesDistanceAndAirSkipsFiltering() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit air = fake(AUnitType.Protoss_Observer, 40, 40);

        world(1, fakeOurs(a, b, c, air), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(2, ours.inGroundRadius(5, a).size());
            assertEquals(4, ours.maxGroundDist(5, air).size());
            assertEquals(2, ours.maxGroundDist(5, a).size());
        });
    }
}
