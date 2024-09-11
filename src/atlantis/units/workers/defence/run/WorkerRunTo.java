package atlantis.units.workers.defence.run;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class WorkerRunTo {
    public static boolean runToFarthestMineral(AUnit worker, AUnit enemy) {
        AUnit mineral = Select.minerals().inRadius(10, enemy).mostDistantTo(enemy);
        if (mineral != null) {
            worker.gather(mineral);
            worker.setTooltipTactical("DidntSignUpForThis");
            return true;
        }
        return false;
    }
}
