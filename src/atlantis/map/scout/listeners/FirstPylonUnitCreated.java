package atlantis.map.scout.listeners;

import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnit;

public class FirstPylonUnitCreated extends AutomaticListener {
    @Override
    public Event listensTo() {
        return Event.OUR_FIRST_PYLON_UNIT_CREATED;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        AUnit unit = (AUnit) data[0];
        AUnit builder = (AUnit) data[1];

        A.errPrintln(A.minSec() + " EVENT - First pylon created CALLBACK! " + unit + " by " + builder);

        assert builder.isWorker();

        if (ScoutState.scouts.isEmpty()) {
            A.errPrintln(A.minSec() + " ---------- No scouts, creating one.");

            ScoutState.scouts.add(builder);
            (new ScoutCommander()).invokeCommander();
        }
    }
}
