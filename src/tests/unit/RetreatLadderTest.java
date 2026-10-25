package tests.unit;

import atlantis.combat.retreating.RetreatLadder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Retreat ladder (owner order 2026-10-26: "keep a history of the places
 * we fought around - if we fought beyond MiddleFocusChoke, go back to
 * it; if we lose even there, fall back to the natural choke, and never
 * further, never to the main"). The ladder answers which rung a losing
 * fight drops to, from depths alone: pure, so it pins without a world.
 *
 * <p>Depths are distances to the main: the natural choke is always the
 * last rung, the middle one only ever lifts above it.</p>
 */
@Tag("combat")
public class RetreatLadderTest {

    private static final double NATURAL = 30;
    private static final double MIDDLE = 98;

    @Test
    public void aFightBeyondTheMiddleRungsToTheMiddleChoke() {
        assertEquals(
            RetreatLadder.Rung.MIDDLE,
            RetreatLadder.rungFor(140, NATURAL, MIDDLE),
            "we fought beyond the middle choke, so the retreat goes back to it");
    }

    @Test
    public void aFightOnOrBehindTheMiddleRungsToTheNaturalChoke() {
        assertEquals(
            RetreatLadder.Rung.NATURAL,
            RetreatLadder.rungFor(MIDDLE, NATURAL, MIDDLE),
            "losing ON the middle line already is the \"even there\" case");
        assertEquals(
            RetreatLadder.Rung.NATURAL,
            RetreatLadder.rungFor(45, NATURAL, MIDDLE),
            "a fight between the lines falls back to the natural choke");
        assertEquals(
            RetreatLadder.Rung.NATURAL,
            RetreatLadder.rungFor(5, NATURAL, MIDDLE),
            "a fight inside the base never lifts the rung above the natural");
    }

    @Test
    public void aMiddleChokeBehindTheNaturalNeverLiftsTheRung() {
        // Bogus world (a "middle" inboard of the natural choke): the
        // ladder must never hand back a rung deeper than the natural.
        assertEquals(
            RetreatLadder.Rung.NATURAL,
            RetreatLadder.rungFor(500, 60, 20),
            "an inverted ladder still bottoms out at the natural choke");
    }

    @Test
    public void noMiddleChokeKeepsTheNaturalRung() {
        assertEquals(
            RetreatLadder.Rung.NATURAL,
            RetreatLadder.rungFor(500, NATURAL, -1),
            "no middle choke on the path: the natural choke is the rung");
    }

    @Test
    public void unknownFightGroundIsTheNaturalRung() {
        assertEquals(
            RetreatLadder.Rung.NATURAL,
            RetreatLadder.rungFor(RetreatLadder.NO_FIGHT_RECORDED, NATURAL, MIDDLE),
            "nothing recorded yet: today's behavior, the natural choke");
    }
}
