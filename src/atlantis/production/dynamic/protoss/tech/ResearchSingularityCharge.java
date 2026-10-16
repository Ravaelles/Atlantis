package atlantis.production.dynamic.protoss.tech;

import atlantis.combat.missions.Missions;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.prioritize.ProtossCriticalStuffInQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import atlantis.game.player.Enemy;
import atlantis.units.select.Select;
import bwapi.UpgradeType;

import java.util.List;

import static bwapi.UpgradeType.Singularity_Charge;

public class ResearchSingularityCharge extends UpgradeResearchCommander {
    private static boolean isResearched = false;
    private static boolean isEnqueued = false;

    private int dragoons;

    public static UpgradeType upgrade() {
        return Singularity_Charge;
    }

    /**
     * D3/D4: Singularity waits above standard spending, so the Dragoon pump
     * saves the 150 gas instead of out-spending it (the starvation behind
     * "Dragoons fight without range all game").
     */
    @Override
    protected ProductionOrderPriority orderPriority() {
        return ProductionOrderPriority.HIGH;
    }

    @Override
    public UpgradeType what() {
        return upgrade();
    }

    public static void onResearched() {
        OurDragoonRange.onSingularityChargeResearched();
        isResearched = true;
    }

    @Override
    public boolean applies() {
        // Read first: early exits below leave the field stale otherwise.
        dragoons = Count.dragoonsWithUnfinished();
        boolean result = appliesInner();

        // NEXT #72: stub fires, live never does - this throttled vector
        // names the refusing gate (prime suspect: a stuck isUpgrading flag
        // from an upgrade that never completes on OpenBW). Pure reads only.
        EventLog.key(LogKey.SINGULARITY_CHECK)
            .param("result", result)
            .param("staticRes", isResearched)
            .param("beingRes", isBeingResearched(upgrade()))
            .param("engineRes", ATech.isResearched(what()))
            .param("critGas", ProtossCriticalStuffInQueue.hasEnoughGas())
            .param("dragoons", dragoons)
            .param("minerals", A.minerals())
            .param("gas", A.gas())
            .log(A.now(), A.minSec());
        return result;
    }

    public boolean appliesInner() {
        if (isBeingResearched()) return false;

        if (ATech.isResearched(what())) {
            onResearched();
            return false;
        }

        if (!ProtossCriticalStuffInQueue.hasEnoughGas()) return false;

        dragoons = Count.dragoonsWithUnfinished();

        Decision decision;
        if ((decision = forForgeExpand()).notIndifferent()) return decision.toBoolean();
        if ((decision = againstProtoss()).notIndifferent()) return decision.toBoolean();
        if ((decision = againstZerg()).notIndifferent()) return decision.toBoolean();

//        if (dragoons >= 2) return true;

        if (!A.hasGas(100) && Army.strength() <= 90) return false;

        if (dragoons >= 4 || (A.hasGas(80) && (A.hasGas(180) || dragoons >= 6))) return true;
        if (dragoons >= 2 && A.supplyUsed() >= 38 && Army.strength() >= 120 && A.hasMinerals(240)) return true;

        return false;
    }

    private Decision againstZerg() {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;

        if (dragoons <= 1) return Decision.FALSE;

        if (dragoons >= 3 || EnemyUnits.hydras() > 0) return Decision.TRUE;

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
    public static boolean isResearched() {
        return isResearched;
    }

    @Override
    protected boolean handle() {
        // NEXT #72: the decision fires in stub but no game ever researched -
        // this row says whether it fires live (downstream hunt) or not
        // (stub/live divergence hunt). Zero behavior change.
        EventLog.key(LogKey.SINGULARITY_APPLIES)
            .param("dragoons", dragoons)
            .param("minerals", A.minerals())
            .param("gas", A.gas())
            .log(A.now(), A.minSec());
        return super.handle();
    }

    @Override
    protected void setEnqueued(boolean isEnqueued) {
        this.isEnqueued = isEnqueued;
    }

    @Override
    protected boolean isEnqueued() {
        return isEnqueued;
    }

    public static boolean isBeingResearched() {
        return isBeingResearched(upgrade());
    }

    public static boolean isResearchedOrBeingResearched() {
        return isResearched() || isBeingResearched();
    }
}
