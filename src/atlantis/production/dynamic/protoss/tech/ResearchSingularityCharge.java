package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Singularity_Charge;

public class ResearchSingularityCharge extends Commander {
    private static boolean isResearched = false;
    private static boolean enqueued = false;
    private int dragoons;

    public static UpgradeType what() {
        return Singularity_Charge;
    }

    public static void onResearched() {
        OurDragoonRange.onSingularityChargeResearched();
        isResearched = true;
    }

    @Override
    public boolean applies() {
        if (isResearched) return false;
//        if (enqueued) return false;
//        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return false;

//        if (CountInQueue.count(what(), 20) > 0) return false;

        if (ATech.isResearched(what())) {
            onResearched();
            return false;
        }

        dragoons = Count.dragoonsWithUnfinished();

        Decision decision;

        if ((decision = forForgeExpand()).notIndifferent()) return decision.toBoolean();
        if ((decision = againstProtoss()).notIndifferent()) return decision.toBoolean();
        if ((decision = againstZerg()).notIndifferent()) return decision.toBoolean();

        if (dragoons >= 3) return true;

        if (!A.hasMinerals(140) && ArmyStrength.ourArmyRelativeStrength() <= 80) return false;

        if (dragoons >= 4 || (A.hasGas(80) && (A.hasGas(180) || dragoons >= 6))) return true;
        if (dragoons >= 2 && A.supplyUsed() >= 38 && Army.strength() >= 120 && A.hasMinerals(240)) return true;

        return false;
    }

    private Decision againstZerg() {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;

        if (dragoons >= 1 && (dragoons >= 2 || EnemyUnits.hydras() > 0)) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }

    private Decision forForgeExpand() {
        if (Count.cannons() >= 2 && Count.dragoonsWithUnfinished() > 0) {
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private Decision againstProtoss() {
        int minDragoons = Missions.isGlobalMissionSparta() ? 2 : 3;

        if (Enemy.protoss()) {
            if (dragoons <= minDragoons) return Decision.INDIFFERENT;
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
        else if (!enqueued && AddToQueue.upgrade(what())) {
            enqueued = true;
//            Queue.get().history().addNow(what().name());
        }
    }

    public static boolean isResearched() {
        return isResearched;
    }
}
