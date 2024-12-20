package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Leg_Enhancements;

public class ResearchLegEnhancements extends Commander {
    public static UpgradeType tech() {
        return Leg_Enhancements;
    }

    @Override
    public boolean applies() {
        if (!Have.citadel()) return false;
        if (ATech.isResearched(tech())) return false;
        if (A.supplyUsed() <= 90 || Count.zealots() <= 4) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(tech().name(), 30)) return false;
        if (CountInQueue.count(tech(), 10) > 0) return false;
        if (TooWeakToTech.check()) return false;

        if (A.hasGas(150) && A.hasMinerals(300) && (Count.zealots() >= 7 || A.s >= 700)) {
            return true;
        }

        return false;
    }

    @Override
    protected void handle() {
        if (AddToQueue.upgrade(tech())) {
            Queue.get().history().addNow(tech().name());
        }
    }
}
