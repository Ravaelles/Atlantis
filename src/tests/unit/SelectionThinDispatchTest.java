package tests.unit;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pin-first for the NEXT #10 thin-dispatcher slice: nearestToMain,
 * mostDistantToBase, center and nearestToDistMore are answered through
 * already-moved seams, so the move is verbatim with one-line delegates.
 * Each assert runs before and after the move (same behavior, new home).
 */
@Tag("unit")
public class SelectionThinDispatchTest extends WorldStubForTests {

    @Test
    public void nearestToMainReadsThroughNearestTo() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(nexus, dragoon), fakeEnemies(), () -> {
            Selection ours = Select.ourCombatUnits();
            assertTrue(ours.nearestToMain().equals(ours.nearestTo(Select.mainOrAnyUnit())),
                "main dispatch keeps the nearest unit");
        });
    }

    @Test
    public void mostDistantToBaseReadsThroughMostDistantTo() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit near = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit far = fake(AUnitType.Protoss_Dragoon, 40, 20);

        world(1, fakeOurs(nexus, near, far), fakeEnemies(), () -> {
            Selection ours = Select.ourCombatUnits();
            assertTrue(ours.mostDistantToBase().equals(far),
                "the farther dragoon answers: " + ours.mostDistantToBase());
            assertTrue(ours.mostDistantToBase()
                .equals(ours.mostDistantTo(Select.mainOrAnyBuilding())));
        });
    }

    @Test
    public void centerIsTheUnitsAverage() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(nexus, a, b), fakeEnemies(), () -> {
            Selection ours = Select.ourCombatUnits();
            APosition center = ours.center();
            assertTrue(Math.abs(center.tx() - 25) <= 1 && Math.abs(center.ty() - 20) <= 1,
                "midpoint of (20,20) and (30,20): " + center);
        });
    }

    @Test
    public void nearestToDistMoreMirrorsLess() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(nexus, dragoon), fakeEnemies(), () -> {
            assertFalse(Select.ourBuildings().nearestToDistMore(dragoon, 99));
            assertTrue(Select.ourBuildings().nearestToDistMore(dragoon, 0.1));
            assertTrue(Select.ourBuildings().nearestToDistLess(dragoon, 99));
        });
    }
}
