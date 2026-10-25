package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.formation.FormationSlots;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Pins the Moon crescent slots (formation Phase A): the gate (3+
 * units, known pivot, pivot out of contact), one walkable slot per
 * unit on the leader's circle around the pivot, deterministic within
 * a frame. Pairs and duels never form up - the 1v1 rules own them.
 */
@Tag("combat")
public class FormationSlotsTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        BattleCommander.clearForTests();
    }

    @Test
    public void gateNeedsThreeUnitsAndAPivot() {
        assertFalse(FormationSlots.applies(0));
        assertFalse(FormationSlots.applies(1));
        assertFalse(FormationSlots.applies(2));
        assertTrue(FormationSlots.applies(3));
        assertTrue(FormationSlots.applies(8));
    }

    @Test
    public void pairNeverFormsUp() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit pivot = fake(AUnitType.Protoss_Zealot, 20, 10);

        world(1, fakeOurs(a, b), fakeEnemies(pivot), () -> {
            Selection ours = Select.our();

            assertNull(FormationSlots.slotFor(a, ours, pivot),
                    "two units fight per-unit - no crescent, the duel rules own them");
        });
    }

    @Test
    public void nullPivotMeansNoSlots() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 11, 12);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            assertNull(FormationSlots.slotFor(a, Select.our(), null),
                    "marching with no enemy anywhere keeps the leader bunching");
        });
    }

    @Test
    public void threeUnitsGetDistinctSlotsOnTheCircle() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 11, 12);
        FakeUnit pivot = fake(AUnitType.Protoss_Zealot, 20, 10);

        world(1, fakeOurs(a, b, c), fakeEnemies(pivot), () -> {
            Selection ours = Select.our();
            APosition slotA = FormationSlots.slotFor(a, ours, pivot);
            APosition slotB = FormationSlots.slotFor(b, ours, pivot);
            APosition slotC = FormationSlots.slotFor(c, ours, pivot);

            assertNotNull(slotA);
            assertNotNull(slotB);
            assertNotNull(slotC);

            double r = ours.list().get(0).distTo(pivot);
            for (APosition slot : new APosition[]{slotA, slotB, slotC}) {
                double d = slot.distTo(pivot);
                assertTrue(Math.abs(d - r) < 1.5,
                        "slots sit on the leader's circle around the pivot, got " + d + " vs " + r);
            }

            assertTrue(!slotA.equals(slotB) && !slotB.equals(slotC) && !slotA.equals(slotC),
                    "every unit owns a different slot - no stacking on one pixel");

            assertEquals(slotA, FormationSlots.slotFor(a, ours, pivot),
                    "same frame answers the same slot - no jitter between units");
        });
    }

    @Test
    public void pivotInContactMeansMicroNotGeometry() {
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 11, 12);
        FakeUnit pivot = fake(AUnitType.Protoss_Zealot, 11, 10);

        world(1, fakeOurs(a, b, c), fakeEnemies(pivot), () -> {
            assertNull(FormationSlots.slotFor(a, Select.our(), pivot),
                    "pivot inside 3.3 tiles is contact - the kite rules own it, not the arc");
        });
    }
}
