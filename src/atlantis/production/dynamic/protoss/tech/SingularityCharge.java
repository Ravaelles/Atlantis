package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.range.OurDragoonWeaponRange;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Singularity_Charge;

public class SingularityCharge extends Commander {
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

        if (CountInQueue.count(what(), 20) > 0) return false;

        if (ATech.isResearched(what())) {
            OurDragoonWeaponRange.onSingularityChargeResearched();
            isResearched = true;
            return false;
        }

        dragoons = Count.dragoons();

        Decision decision;

        if ((decision = againstProtoss()).notIndifferent()) return decision.toBoolean();

        if (!A.hasMinerals(300) && ArmyStrength.ourArmyRelativeStrength() <= 80) return false;

        if (A.hasGas(150) && (A.hasGas(260) || dragoons >= 3)) return true;

        return false;
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
