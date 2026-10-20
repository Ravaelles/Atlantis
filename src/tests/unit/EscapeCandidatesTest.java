package tests.unit;

import atlantis.battle.escape.EscapeCandidates;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Escape slice 3 (NEXT #175): candidate generation is 8 dirs x rings
 * with unwalkable destinations filtered.
 */
@Tag("combat")
public class EscapeCandidatesTest {

    @Test
    public void openGroundGivesAll24() {
        List<APosition> cands = EscapeCandidates.generate(APosition.create(30, 20), (x, y) -> true);

        assertEquals(24, cands.size(), "8 dirs x {4, 8, 12}");
    }

    @Test
    public void wallEastRemovesItsDestinations() {
        List<APosition> cands = EscapeCandidates.generate(APosition.create(30, 20), (x, y) -> x < 34);

        assertTrue(cands.size() < 24, "eastward destinations pruned, got " + cands.size());
        for (APosition c : cands) {
            assertTrue(c.tx() < 34, "no destination inside the wall: " + c);
        }
    }

    @Test
    public void nullInputGivesEmptyList() {
        assertTrue(EscapeCandidates.generate(null, (x, y) -> true).isEmpty());
        assertTrue(EscapeCandidates.generate(APosition.create(30, 20), null).isEmpty());
    }
}
