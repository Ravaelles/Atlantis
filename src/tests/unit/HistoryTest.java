package tests.unit;

import atlantis.production.orders.production.queue.add.History;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("infra")
public class HistoryTest extends AbstractTestWithUnits {
    @Test
    public void testLastHappenedAgo() {
        History history = new History();

        assertNull(history.last());
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
