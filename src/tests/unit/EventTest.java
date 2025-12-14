package tests.unit;

import atlantis.game.event.Event;
import atlantis.game.event.Events;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest extends AbstractTestWithUnits {
    public static String state = "-";

    @Test
    public void eventIsListenedToAndReceivesParams() {
        EventTest.state = "unchanged";

//        new AutomaticListener() {
//            @Override
//            public String listensTo() {
//                return "FakeEventName";
//            }
//
//            @Override
//            public void onEvent(String event) {
//                EventTest.state = "action taken!";
//            }
//        };

        assertEquals("unchanged", state);

        Events.dispatch(Event.FAKE_EVENT, 666, null, "hello");

        assertEquals("Foo! Integer:666, null:null, String:hello", state);
    }
}

//class FakeListener extends AutomaticListener {
//    @Override
//    public String listensTo() {
//        return "FakeEventName";
//    }
//
//    @Override
//    public void onEvent(String event) {
//        EventTest.state = "action taken!";
//    }
//};
