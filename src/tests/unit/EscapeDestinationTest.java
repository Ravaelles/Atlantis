package tests.unit;

import atlantis.combat.escape.EscapeDestination;
import atlantis.combat.escape.EscapeRays;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Escape slice 8 (NEXT #172): the destination pick is walkability- and
 * size-gated, scored, tie-broken away from the threat.
 */
@Tag("combat")
public class EscapeDestinationTest extends WorldStubForTests {

    @Test
    public void openFieldStepsDirectlyAway() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 32, 20);
        threat.setEnemy();

        APosition dest = EscapeDestination.pick(dragoon, threat, 2,
            (x, y) -> true, Collections.emptyList(), Collections.emptyList());

        // All scores zero: the tie-break (farther from the threat) prefers
        // the diagonal-back corner over straight back - it opens more ground.
        assertEquals(APosition.create(28, 22), dest, "SW corner opens the most ground");
    }

    @Test
    public void wallEastIsNeverPicked() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 32, 20);
        threat.setEnemy();

        APosition dest = EscapeDestination.pick(dragoon, threat, 2,
            (x, y) -> x < 32, Collections.emptyList(), Collections.emptyList());

        assertNotNull(dest);
        assertTrue(dest.tx() < 32, "no destination inside the wall: " + dest);
    }

    @Test
    public void secondEnemySteersAround() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 40, 20);
        threat.setEnemy();
        APosition lurker = APosition.create(28, 20);

        APosition dest = EscapeDestination.pick(dragoon, threat, 2,
            (x, y) -> true, Collections.emptyList(), Collections.singletonList(lurker));

        assertNotNull(dest);
        assertNotEquals(lurker, dest, "never step onto the lurker");
        double picked = atlantis.combat.escape.EscapeScoring.enemyPressure(dest,
            Collections.singletonList(lurker), 20);
        double occupied = atlantis.combat.escape.EscapeScoring.enemyPressure(lurker,
            Collections.singletonList(lurker), 20);
        assertTrue(picked < occupied, "picked ground is safer than the lurker tile");
    }

    @Test
    public void seamFailsOpen() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 32, 20);
        threat.setEnemy();

        world(1, fakeOurs(dragoon), fakeEnemies(threat), () -> {
            assertNull(atlantis.combat.escape.EscapeScoredStep.pickScored(null, threat, 2),
                "null unit scores nothing");
            assertNull(atlantis.combat.escape.EscapeScoredStep.pickScored(dragoon, null, 2),
                "null threat scores nothing");
            assertNull(atlantis.combat.escape.EscapeScoredStep.pickScored(dragoon, threat, 0),
                "no step scores nothing");
            assertNotNull(atlantis.combat.escape.EscapeScoredStep.pickScored(dragoon, threat, 2),
                "open stub field always scores somewhere");
        });
    }

    @Test
    public void wideBodyNeedsCorridor() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, 20, 20);
        threat.setEnemy();
        // Wall east (x>=32) and south (y>=22): the only gap is (31, 20).
        EscapeRays.Walkability walls = (x, y) -> x < 32 && y < 22;

        APosition narrowPick = EscapeDestination.pick(zealot, threat, 1,
            walls, Collections.emptyList(), Collections.emptyList());
        APosition widePick = EscapeDestination.pick(dragoon, threat, 1,
            walls, Collections.emptyList(), Collections.emptyList());

        assertNotNull(narrowPick);
        assertEquals(31, narrowPick.tx(), "small slips into the gap column, got " + narrowPick);
        assertTrue(widePick == null || widePick.tx() != 31,
            "large needs the spare tile past the step, never wedges into the gap: " + widePick);
    }
}
