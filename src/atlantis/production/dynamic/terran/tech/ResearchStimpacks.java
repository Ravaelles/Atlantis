package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.ResearchNow;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.TechType;
import bwapi.UpgradeType;

public class ResearchStimpacks extends Commander {
    private static boolean isResearched = false;
    private static boolean isEnqueued = false;

    public static void onResearched() {
        isResearched = true;
    }

    @Override
    public boolean applies() {
        if (isResearched) return true;

        if (!Have.academy()) return false;
        if (!ATech.isResearched(UpgradeType.U_238_Shells)) return false;

        if (ATech.isResearched(what())) {
            onResearched();
            return false;
        }

        if (Strategy.get().goingBio()) {
            if (Count.infantry() >= 8 && A.canAffordWithReserved(100, 100)) {
                if (ATech.isNotResearchedOrPlanned(what())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static TechType what() {
        return TechType.Stim_Packs;
    }

    @Override
    protected void handle() {
        if (ResearchNow.research(what())) {
//            isEnqueued = true;
        }
        else if (!isEnqueued) {
            if (CountInQueue.count(what(), 50) == 0) {
//                System.err.println("Stimpacks not enqueued, ADD TO QUEUE.");
                AddToQueue.tech(what());
                isEnqueued = true;
            }
        }
    }

    public static boolean isResearched() {
        return isResearched || ATech.isResearched(what());
    }
}
