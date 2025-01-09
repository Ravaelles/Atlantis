package atlantis.production.dynamic.terran.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.tech.ResearchNow;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.range.OurMarineRange;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.UpgradeType;

public class ResearchU238 extends Commander {
    private static boolean isResearched = false;
    private static boolean isEnqueued = false;

    public static UpgradeType what() {
        return UpgradeType.U_238_Shells;
    }

    public static void onResearched() {
        OurMarineRange.onU238Researched();
        isResearched = true;
    }

    @Override
    public boolean applies() {
        if (isResearched) return true;
//        if (isEnqueued) return true;

        if (!Have.academy()) return false;
        if (Count.medics() <= 0) return false;
        if (ATech.isResearched(what())) {
            onResearched();
            return false;
        }

        if (Strategy.get().goingBio()) {
            int infantry = Count.infantry();
            if (
                (infantry >= 6 && A.canAffordWithReserved(100, 100))
                    || (infantry >= 10 && A.canAfford(250, 100))
            ) {
                if (ATech.isNotResearchedOrPlanned(what())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void handle() {
        if (ResearchNow.research(what())) {
//            isEnqueued = true;
        }
        else if (!isEnqueued) {
            if (CountInQueue.count(what(), 50) == 0) {
//                System.err.println("U238 not enqueued, ADD TO QUEUE.");
                AddToQueue.upgrade(what());
                isEnqueued = true;
            }
        }
    }

    public static boolean isResearched() {
        return isResearched || ATech.isResearched(what());
    }
}
