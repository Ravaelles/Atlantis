package tests.unit;

import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Retreat never orders an unwalkable destination (owner log: a Zealot
 * ordered into [55,22], refused in the funnel with a stack trace while
 * the unit still counted as retreated). The validity gate owns the
 * fallback: a standable rally marches, an unstandable one falls back
 * to the unscattered anchor, two bad points fight in place.
 *
 * <p>Pure positions, no map: walkability injects as a predicate.</p>
 */
@Tag("combat")
public class RetreatStandableTest {

    private static final Function<APosition, Boolean> WALKABLE_WEST_OF_100 =
        pos -> pos != null && pos.tx() < 100;

    private static HasPosition at(int tx, int ty) {
        return APosition.create(tx, ty);
    }

    @Test
    public void standableRallyMarches() {
        assertEquals(at(50, 50), RetreatFromLostFight.standableOrFallback(
            at(50, 50), at(60, 60), WALKABLE_WEST_OF_100));
    }

    @Test
    public void unstandableRallyFallsBackToAnchor() {
        assertEquals(at(60, 60), RetreatFromLostFight.standableOrFallback(
            at(150, 22), at(60, 60), WALKABLE_WEST_OF_100));
    }

    @Test
    public void twoBadPointsFightInPlace() {
        assertNull(RetreatFromLostFight.standableOrFallback(
            at(150, 22), at(160, 60), WALKABLE_WEST_OF_100),
            "nowhere standable to run - the verdict is NOWHERE_TO_RUN, never a refused order");
    }

    @Test
    public void nullRallyFallsBackToAnchor() {
        assertEquals(at(60, 60), RetreatFromLostFight.standableOrFallback(
            null, at(60, 60), WALKABLE_WEST_OF_100));
    }

    @Test
    public void nullsFightInPlace() {
        assertNull(RetreatFromLostFight.standableOrFallback(null, null, WALKABLE_WEST_OF_100));
    }
}
