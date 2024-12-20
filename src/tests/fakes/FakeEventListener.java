// WARNING!!! This has to have "atlantis" namespace, otherwise AutoRegisterEventListeners will not detect it.
package atlantis.tests.fakes;

import atlantis.game.event.Listener;
import tests.unit.EventTest;

public class FakeEventListener extends Listener {
    @Override
    public String listensTo() {
        return "FakeEventName";
    }

    @Override
    public void onEvent(String event, Object... data) {
        EventTest.state = "Foo! "
            + data[0].getClass().getSimpleName() + ":" + data[0]
            + ", null:" + data[1]
            + ", " + data[2].getClass().getSimpleName() + ":" + data[2];
    }
}
