package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.production.orders.production.queue.add.History;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryTest extends AbstractTestWithUnits {
    @Test
    public void testLastHappenedAgo() {
        History history = new History();

        assertEquals(null, history.last());
        assertEquals(99998765, history.lastHappenedAgo("eventA"));

        history.addNow("eventA");

        assertEquals(0, history.lastHappenedAgo("eventA"));
        assertEquals("eventA", history.get(0));
        assertEquals(1, history.size());

        history.addNow("eventB");

        assertEquals(2, history.size());
        assertEquals("eventB", history.get(1));
    }
}
