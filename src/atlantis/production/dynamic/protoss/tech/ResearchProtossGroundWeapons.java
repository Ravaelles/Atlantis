package atlantis.production.dynamic.protoss.tech;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Protoss_Ground_Weapons;

public class ResearchProtossGroundWeapons extends UpgradeResearchCommander {
    private static boolean isResearched = false;
    private static boolean isEnqueued = false;

    public UpgradeType what() {
        return Protoss_Ground_Weapons;
    }

    @Override
    public boolean appliesInner() {
        String refusedBy = refusedBy();
        boolean result = refusedBy == null;
        // NEXT #85: L1+ never proved live - this vector names the refusing
        // gate the way SINGULARITY_CHECK did for #72. Pure reads only.
        // `refusedBy` names the first failing gate (`none` when firing),
        // so the next post-Forge rich game says which term holds research
        // back instead of repeating the Python shape (bank 772, combat 8,
        // twin silent on why).
        atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.TECH_CHECK)
            .param("upgrade", what().name())
            .param("result", result)
            .param("refusedBy", refusedBy == null ? "none" : refusedBy)
            .param("beingRes", isBeingResearched(what()))
            .param("engineLevel", ATech.getUpgradeLevel(what()))
            .param("minerals", A.minerals())
            .param("gas", A.gas())
            .param("combat", Count.ourCombatUnits())
            .log(A.now(), A.minSec());
        return result;
    }

    /**
     * First failing gate, or null when research may fire. Same order and
     * same conditions as the old boolean chain - only the answer shape
     * changed, from silent-false to a named refusal.
     */
    private String refusedBy() {
        if (!Have.forge()) return "no-forge";
        if (isResearched()) return "researched";
        if (Count.ourCombatUnits() <= 10) return "combat-10";
        if (!A.hasMinerals(700) && Count.basesWithUnfinished() <= 2) return "bank-700-or-3rd";
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return "recent-order";
        if (CountInQueue.count(what(), 4) > 0) return "already-queued";
        if (TooWeakToTech.check()) return "too-weak";

        if (A.hasGas(150 + (Enemy.zerg() ? 80 : 0)) && A.hasMinerals(290)) {
            if (!A.canAfford(500, 200) && Count.ourCombatUnits() <= 17) return "cant-afford-17";

            return null;
        }

        return "poor";
    }

    @Override
    protected void setEnqueued(boolean isEnqueued) {
        this.isEnqueued = isEnqueued;
    }

    @Override
    protected boolean isEnqueued() {
        return isEnqueued;
    }

    public static boolean isResearched() {
        boolean isResearched = ATech.isResearched(Protoss_Ground_Weapons);
        if (!isResearched) return false;

        int upgradeLevel = getCurrentUpgradeLevel();
        if (upgradeLevel >= 3) return true;

        return !A.canAfford(550, 300);
    }

    private static int getCurrentUpgradeLevel() {
        return ATech.getUpgradeLevel(Protoss_Ground_Weapons);
    }

    @Override
    protected boolean handle() {
        ResearchNow.research(what());
        return false;
    }

}
