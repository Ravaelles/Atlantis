package tests.unit;

import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.game.event.Events;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest extends AbstractTestWithUnits {
    public static String state = "-";

    @Test
    public void eventIsListenedToAndReceivesParams() {
        Events.register(Event.FAKE_EVENT, new FakeListener());

        EventTest.state = "unchanged";

        assertEquals("unchanged", state);

        Events.dispatch(Event.FAKE_EVENT, 666, null, "hello");

        assertEquals("action taken!", state);
    }
}

/**
 * This class is not auto-registered, because it lives in /tests namespace.
 * Auto-scanning happens only in atlantis.*, see: findClasses("atlantis")
 */
class FakeListener extends AutomaticListener {
    @Override
    public Event listensTo() {
        return Event.FAKE_EVENT;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        EventTest.state = "action taken!";
    }
};
