package atlantis.debug;

import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class OurWorkerWasKilled {
    private static int workersKilledCount = 0;

    public static void onWorkedKilled(AUnit unit) {
        workersKilledCount++;

        if (!unit.isWorker()) return;
        if (A.seconds() >= 400) return;
        if (unit.lastActionLessThanAgo(30 * 2, Actions.REPAIR)) return;




//        CameraCommander.centerCameraOn(unit.lastPosition());
//        GameSpeed.pauseGame();
    }
}
