package atlantis.production.dynamic.protoss.tech;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import bwapi.UpgradeType;

import static bwapi.UpgradeType.Protoss_Ground_Armor;
import static bwapi.UpgradeType.Protoss_Ground_Weapons;

public class ResearchProtossGroundArmor extends Commander {
    private static UpgradeType what() {
        return Protoss_Ground_Armor;
    }

    @Override
    public boolean applies() {
        String refusedBy = refusedBy();
        boolean result = refusedBy == null;
        // NEXT #85: L1+ never proved live - this vector names the refusing
        // gate the way SINGULARITY_CHECK did for #72. Pure reads only.
        atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.TECH_CHECK)
            .param("upgrade", what().name())
            .param("result", result)
            .param("refusedBy", refusedBy == null ? "none" : refusedBy)
            .param("beingRes", UpgradeResearchCommander.isBeingResearched(what()))
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
        // Weapons first, but level 1 is enough: requiring full L3 (via
        // Weapons.isResearched()) held armor until the end of the tech path
        // (NEXT #85). Vanilla has no such cross requirement, and Terran
        // researchers carry none either.
        if (ATech.getUpgradeLevel(Protoss_Ground_Weapons) < 1) return "weapons-l1";
        if (Count.ourCombatUnits() <= 10) return "combat-10";
        if (ATech.isResearched(what())) return "researched";
        if (!A.hasMinerals(650) && !A.hasGas(400) && Count.basesWithUnfinished() <= 1) return "bank-650-gas-400-or-2nd";
        if (Queue.get().history().lastHappenedLessThanSecondsAgo(what().name(), 30)) return "recent-order";
        if (CountInQueue.count(what(), 10) > 0) return "already-queued";
        if (TooWeakToTech.check()) return "too-weak";

        if (A.hasGas(270 + (Enemy.protoss() ? 100 : 0)) && A.hasMinerals(550)) {
            if (!A.canAfford(500, 200) && Count.ourCombatUnits() <= 17) return "cant-afford-17";

            return null;
        }

        return "poor";
    }

    public static boolean isResearched() {
        boolean isResearched = ATech.isResearched(what());
        if (!isResearched) return false;

        int upgradeLevel = getCurrentUpgradeLevel();
        if (upgradeLevel >= 3) return true;

        return !A.canAfford(550, 300);
    }

    private static int getCurrentUpgradeLevel() {
        return ATech.getUpgradeLevel(what());
    }

    @Override
    protected boolean handle() {
        ResearchNow.research(what());
        return false;
    }
}
