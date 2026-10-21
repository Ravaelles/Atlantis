package tests.unit;

import atlantis.battle.escape.EscapeArc;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Arc retreat geometry (NEXT #175): bending the straight escape step back
 * toward the squad. Pure math, no world needed - the policy gate (radius,
 * blend default) lives with the caller and the tuner seam.
 */
@Tag("combat")
public class EscapeArcTest {

    private static APosition px(int x, int y) {
        return APosition.createFromPixels(x, y);
    }

    @Test
    public void zeroBlendIsTodaysBehaviorExactly() {
        APosition from = px(0, 0);
        APosition straight = px(100, 0);
        APosition squad = px(0, 100);

        assertEquals(straight, EscapeArc.bend(from, straight, squad, 0.0),
            "blend 0 walks straight away, bit-identical to no arc");
    }

    @Test
    public void fullBlendWalksAtTheSquad() {
        APosition from = px(0, 0);
        APosition straight = px(100, 0);
        APosition squad = px(0, 100);

        APosition bent = EscapeArc.bend(from, straight, squad, 1.0);

        double dx = bent.x() - from.x();
        double dy = bent.y() - from.y();
        double len = Math.hypot(dx, dy);
        assertTrue(Math.abs(len - 100) < 3,
            "the step keeps its length, only the direction bends: " + len);
        double dot = (dx * 0 + dy * 100) / (len * 100);
        assertTrue(dot > 0.95, "full blend walks at the squad: " + bent);
    }

    @Test
    public void halfBlendHalvesTheAngle() {
        APosition from = px(0, 0);
        APosition straight = px(100, 0);
        APosition squad = px(0, 100);

        APosition bent = EscapeArc.bend(from, straight, squad, 0.5);

        double angleDeg = Math.toDegrees(Math.atan2(bent.y() - from.y(), bent.x() - from.x()));
        assertTrue(Math.abs(angleDeg - 45) < 3,
            "half blend halves the 90-degree turn: " + angleDeg);
    }

    @Test
    public void nullsAndGarbageFallBackToStraight() {
        APosition from = px(0, 0);
        APosition straight = px(100, 0);
        APosition squad = px(0, 100);

        assertEquals(straight, EscapeArc.bend(null, straight, squad, 0.5), "no origin: straight");
        assertNull(EscapeArc.bend(from, null, squad, 0.5), "no step: null stays null");
        assertEquals(straight, EscapeArc.bend(from, straight, null, 0.5), "no squad: straight");
        assertEquals(straight, EscapeArc.bend(from, straight, squad, -1.0), "negative: straight");
        assertEquals(straight, EscapeArc.bend(from, straight, squad, Double.NaN), "NaN: straight");
        assertEquals(from, EscapeArc.bend(from, from, squad, 0.5), "zero step: straight");
    }
}
