package tests.unit;

import atlantis.battle.escape.EscapeDestination;
import atlantis.battle.escape.EscapeRays;
import atlantis.battle.strategy.DuelKite;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Long-vector escape (NEXT #173, owner doctrine): walk straight out as
 * far as possible (5 tiles); only when the straight-5 is blocked does
 * the horizon shrink - and then by deflecting at full length, never by
 * shuffling short into the pocket. Open ground answers exactly like
 * the plain pick; only the wall case changes.
 */
@Tag("combat")
public class EscapeLongVectorTest extends WorldStubForTests {

    /** Step length along its ray (straight 5 and diagonal 5 both read 5). */
    private static int rayLen(APosition dest, FakeUnit from) {
        return Math.max(Math.abs(dest.tx() - from.tx()), Math.abs(dest.ty() - from.ty()));
    }

    private static FakeUnit dragoonAt(int tx, int ty) {
        return fake(AUnitType.Protoss_Dragoon, tx, ty);
    }

    private static FakeUnit threatAt(int tx, int ty) {
        FakeUnit threat = fake(AUnitType.Zerg_Zergling, tx, ty);
        threat.setEnemy();

        return threat;
    }

    @Test
    public void openGroundKeepsShortStep() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(25, 20);

        APosition plain = EscapeDestination.pick(dragoon, threat, 2,
            (x, y) -> true, Collections.emptyList(), Collections.emptyList());
        APosition escape = EscapeDestination.pickEscape(dragoon, threat, 2,
            (x, y) -> true, Collections.emptyList(), Collections.emptyList());

        assertNotNull(plain);
        assertEquals(plain, escape, "open ground must not change: " + escape + " vs " + plain);
        assertEquals(2, rayLen(escape, dragoon),
            "tactical footwork stays short: " + escape);
    }

    @Test
    public void wallAheadDeflectsAtFive() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(27, 20);
        // Solid wall directly behind the dragoon (east, x>=32).
        EscapeRays.Walkability wall = (x, y) -> x < 32;

        APosition dest = EscapeDestination.pickEscape(dragoon, threat, 2,
            wall, Collections.emptyList(), Collections.singletonList(threat));

        assertNotNull(dest, "a wall behind must deflect, not stand");
        assertEquals(5, rayLen(dest, dragoon),
            "full-length sidestep, not a short shuffle: " + dest);
        assertTrue(dest.tx() < 32, "no destination inside the wall: " + dest);
    }

    @Test
    public void deflectRowsItsTwin() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(27, 20);
        EscapeRays.Walkability wall = (x, y) -> x < 32;

        world(3, fakeOurs(dragoon), fakeEnemies(threat), () -> {
            if (atlantis.game.A.now() != 2) return;

            atlantis.util.log.EventLog.clearForTests();
            APosition dest = EscapeDestination.pickEscape(dragoon, threat, 2,
                wall, Collections.emptyList(), Collections.singletonList(threat));

            assertNotNull(dest);
            String rows = atlantis.util.log.EventLog.toJsonLines();
            assertTrue(rows.contains("\"result\":\"long-deflect\""),
                "the long branch must name itself in the log: " + rows);
        });
    }

    @Test
    public void openGroundRowsNoDeflect() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(25, 20);

        world(3, fakeOurs(dragoon), fakeEnemies(threat), () -> {
            if (atlantis.game.A.now() != 2) return;

            atlantis.util.log.EventLog.clearForTests();
            APosition dest = EscapeDestination.pickEscape(dragoon, threat, 2,
                (x, y) -> true, Collections.emptyList(), Collections.emptyList());

            assertNotNull(dest);
            assertFalse(atlantis.util.log.EventLog.toJsonLines().contains("long-deflect"),
                "short steps stay silent");
        });
    }

    @Test
    public void boxedStaysNull() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(27, 20);
        // Only the adjacent ring is walkable: nothing at 2, nothing at 5.
        EscapeRays.Walkability pocket = (x, y) ->
            Math.abs(x - 30) <= 1 && Math.abs(y - 20) <= 1;

        assertNull(EscapeDestination.pickEscape(dragoon, threat, 2,
            pocket, Collections.emptyList(), Collections.singletonList(threat)),
            "a true box still fights - no direction exists at any length");
    }

    @Test
    public void runPathUnchangedOnOpen() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(25, 20);

        APosition plain = EscapeDestination.pick(dragoon, threat, 8,
            (x, y) -> true, Collections.emptyList(), Collections.emptyList());
        APosition escape = EscapeDestination.pickEscape(dragoon, threat, 8,
            (x, y) -> true, Collections.emptyList(), Collections.emptyList());

        assertNotNull(plain);
        assertEquals(plain, escape, "long requests keep their answer on open ground");
    }

    @Test
    public void runPathShortensOnlyAsLastResort() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(25, 20);
        // Open bubble radius 6, wall beyond: nothing scores at 8.
        EscapeRays.Walkability capped = (x, y) ->
            Math.max(Math.abs(x - 30), Math.abs(y - 20)) <= 6;

        assertNull(EscapeDestination.pick(dragoon, threat, 8,
            capped, Collections.emptyList(), Collections.emptyList()),
            "precondition: nothing scores at 8");
        APosition dest = EscapeDestination.pickEscape(dragoon, threat, 8,
            capped, Collections.emptyList(), Collections.emptyList());

        assertNotNull(dest, "smaller horizon is the last resort before fighting");
        assertEquals(5, rayLen(dest, dragoon),
            "rescue at full-length deflect, not shorter: " + dest);
    }

    @Test
    public void duelKiteSeamDeflectsAtWall() {
        FakeUnit dragoon = dragoonAt(30, 20);
        FakeUnit threat = threatAt(27, 20);
        EscapeRays.Walkability wall = (x, y) -> x < 32;

        APosition dest = DuelKite.yieldStep(dragoon, threat, wall,
            Collections.emptyList(), Collections.singletonList(threat));

        assertNotNull(dest, "the duel seam must not stand at a wall");
        assertEquals(5, rayLen(dest, dragoon),
            "the duel step becomes a sidestep at the wall: " + dest);
    }

    @Test
    public void awayDirectionIsOppositeTheThreat() {
        FakeUnit dragoon = dragoonAt(30, 20);

        assertEquals(0, EscapeRays.awayDirIndex(dragoon, threatAt(25, 20)),
            "threat west means away east (E first)");
        assertEquals(4, EscapeRays.awayDirIndex(dragoon, threatAt(35, 20)),
            "threat east means away west");
        assertEquals(2, EscapeRays.awayDirIndex(dragoon, threatAt(30, 15)),
            "threat north means away south");
    }
}
