// WARNING!!! This has to have "atlantis" namespace, otherwise AutoRegisterEventListeners will not detect it.
package tests.fakes;

import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import tests.unit.EventTest;

public class FakeEventListener extends AutomaticListener {
    @Override
    public Event listensTo() {
        return Event.FAKE_EVENT;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        EventTest.state = "Foo! "
            + data[0].getClass().getSimpleName() + ":" + data[0]
            + ", null:" + data[1]
            + ", " + data[2].getClass().getSimpleName() + ":" + data[2];
    }
}
