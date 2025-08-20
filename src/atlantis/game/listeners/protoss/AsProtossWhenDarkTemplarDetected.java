package atlantis.game.listeners.protoss;

import atlantis.game.A;
import atlantis.production.constructions.cancelling.CancelNotStarted;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class AsProtossWhenDarkTemplarDetected {
    public static void update(AUnit dt) {
        if (!Have.observer() && !Have.cannon() && !A.hasMinerals(600)) {
            System.err.println("@@@@@@@@@@ DT detected...");
            if (CancelNotStarted.cancel(AUnitType.Protoss_Nexus, "Cancel due to DT!")) {
                System.err.println("@@@@@@@@@@ DT detected - cancel nexus");
            }

            if (Have.notEvenPlanned(AUnitType.Protoss_Forge)) {
                System.err.println("@@@@@@@@@@ DT detected - adding Forge to queue with top priority");
                AddToQueue.withTopPriority(AUnitType.Protoss_Forge);
            }
        }
    }
}
