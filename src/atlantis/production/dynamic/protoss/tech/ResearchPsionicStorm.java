package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import bwapi.TechType;

import static bwapi.TechType.Psionic_Storm;

public class ResearchPsionicStorm extends Commander {
    private static boolean isResearched = false;
    private static boolean enqueued = false;
    private int dragoons;

    public static TechType what() {
        return Psionic_Storm;
    }

    @Override
    public boolean applies() {
        if (isResearched) return false;
        if (enqueued) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return false;

        if (CountInQueue.count(what(), 5) > 0) return false;

        if (ATech.isResearched(what())) {
            isResearched = true;
            return false;
        }

        int ht = Count.ht();

        if (ht >= 1 && A.canAfford(what())) {
            return true;
        }
        if (ht >= 2) {
            return AddToQueue.tech(what());
        }

        return false;
    }

    @Override
    protected void handle() {
        if (ResearchNow.research(what())) {
            enqueued = true;
        }

//        if (AddToQueue.upgrade(tech())) {
//            enqueued = true;
//            Queue.get().history().addNow(tech().name());
//        }
    }

    public static boolean isResearched() {
        return isResearched;
    }
}
