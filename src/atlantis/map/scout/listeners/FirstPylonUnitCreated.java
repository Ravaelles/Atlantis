package atlantis.map.scout.listeners;

import atlantis.game.A;
import atlantis.game.event.Listener;
import atlantis.map.scout.ScoutCommander;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnit;

public class FirstPylonUnitCreated extends Listener {
    @Override
    public String listensTo() {
        return "FirstPylonUnitCreated";
    }

    @Override
    public void onEvent(String event, Object... data) {
//    public void onEvent(String event, AUnit unit, AUnit builder) {
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
