package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Singularity_Charge;

public class ResearchSingularityCharge extends Commander {
    private static boolean isResearched = false;
    private static boolean enqueued = false;
    private int dragoons;

    public static UpgradeType what() {
        return Singularity_Charge;
    }

    @Override
    public boolean applies() {
        if (isResearched) return false;
        if (enqueued) return false;
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return false;

//        if (CountInQueue.count(what(), 20) > 0) return false;

        if (ATech.isResearched(what())) {
            OurDragoonRange.onSingularityChargeResearched();
            isResearched = true;
            return false;
        }

        dragoons = Count.dragoonsWithUnfinished();

        Decision decision;

        if ((decision = againstProtoss()).notIndifferent()) return decision.toBoolean();

        if ((decision = forForgeExpand()).notIndifferent()) return decision.toBoolean();

        if (!A.hasMinerals(240) && ArmyStrength.ourArmyRelativeStrength() <= 80) return false;

        if (dragoons >= 5 || (A.hasGas(80) && (A.hasGas(180) || dragoons >= 3))) return true;
        if (dragoons >= 2 && A.supplyUsed() >= 38 && OurArmy.strength() >= 120 && A.hasMinerals(240)) return true;

        return false;
    }

    private Decision forForgeExpand() {
        if (Count.cannons() >= 2 && Count.dragoonsWithUnfinished() > 0) {
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private Decision againstProtoss() {
        int minDragoons = Missions.isGlobalMissionSparta() ? 2 : 4;

        if (Enemy.protoss()) {
            if (dragoons <= minDragoons) return Decision.FALSE;
            if (dragoons >= minDragoons + 2) return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    @Override
    protected void handle() {
        AddToQueue.upgrade(what());

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
