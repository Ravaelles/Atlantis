package tests.unit;

import atlantis.debug.tweaker.TunerObjective;
import atlantis.debug.tweaker.TunerObjective.Row;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #160 step 1: the batch winner is computed, not eyeballed.
 * Win first, then hits-taken ascending, then seconds ascending.
 */
@Tag("combat")
public class TunerObjectiveTest {

    @Test
    public void winBeatsLossRegardlessOfHits() {
        Row loss = new Row("g1", false, 0, 20);
        Row win = new Row("g2", true, 19, 90);

        assertTrue(TunerObjective.compare(win, loss) < 0, "a win always outranks a loss");
        assertEquals("g2", TunerObjective.best(Arrays.asList(loss, win)).get().game());
    }

    @Test
    public void fewerHitsWinsAmongWins() {
        Row sloppy = new Row("g1", true, 19, 20);
        Row clean = new Row("g2", true, 6, 60);

        assertEquals("g2", TunerObjective.best(Arrays.asList(sloppy, clean)).get().game());
    }

    @Test
    public void fasterWinsWhenHitsTie() {
        Row slow = new Row("g1", true, 10, 90);
        Row fast = new Row("g2", true, 10, 25);

        assertEquals("g2", TunerObjective.best(Arrays.asList(slow, fast)).get().game());
    }

    @Test
    public void unknownCountersSortAfterKnownOnes() {
        Row blind = TunerObjective.parseBatchLine("g9,W,?,5,0,?,12,300");
        Row seen = TunerObjective.parseBatchLine("g3,W,25,5,0,10,12,300");

        assertTrue(TunerObjective.compare(seen, blind) < 0, "a fully-instrumented row beats a blind one");
    }

    @Test
    public void parsesBatchCsvShapeAndSkipsHeaderAndGarbage() {
        Row row = TunerObjective.parseBatchLine("g4,W,31,1,0,11,33,120");

        assertTrue(row.win());
        assertEquals(11, row.hitsTaken());
        assertEquals(31, row.seconds());

        assertNull(TunerObjective.parseBatchLine("game,result,seconds,killed,lost,hits,shots,score"));
        assertNull(TunerObjective.parseBatchLine("g5,X,31,1,0,11,33,120"));
        assertNull(TunerObjective.parseBatchLine("too,short"));
    }

    @Test
    public void rankingIsDeterministicOnFullTies() {
        Row b = new Row("g-b", true, 10, 30);
        Row a = new Row("g-a", true, 10, 30);

        List<Row> ranked = TunerObjective.rank(Arrays.asList(b, a));

        assertEquals("g-a", ranked.get(0).game());
        assertEquals("g-b", ranked.get(1).game());
    }

    @Test
    public void emptyBatchHasNoWinner() {
        Optional<Row> winner = TunerObjective.best(Arrays.asList());

        assertFalse(winner.isPresent(), "no rows means no winner, never a crash");
    }
}
