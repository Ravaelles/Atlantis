package atlantis.production.dynamic.protoss.tech;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.protoss.prioritize.ProtossCriticalStuffInQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Count;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import bwapi.UpgradeType;


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
    public boolean appliesInner() {
        // Read first: early exits below leave the field stale otherwise.
        dragoons = Count.dragoonsWithUnfinished();
        boolean result = decide();

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

    private boolean decide() {
        if (isBeingResearched()) return false;

        if (ATech.isResearched(what())) {
            onResearched();
            return false;
        }

        if (!ProtossCriticalStuffInQueue.hasEnoughGas()) return false;

        dragoons = Count.dragoonsWithUnfinished();

        // NEXT #93 absorption track: the trio reads posture off the board.
        // Same numbers, new home - the plain gates below keep the field.
        atlantis.decisions.board.BoardState board =
            atlantis.production.GoalBoardAdvisor.snapshot();

        Boolean want;
        if ((want = forForgeExpand(board)) != null) return want;
        if ((want = againstProtoss(board)) != null) return want;
        if ((want = againstZerg(board)) != null) return want;

//        if (dragoons >= 2) return true;

        if (!A.hasGas(100) && Army.strength() <= 90) return false;

        if (dragoons >= 4 || (A.hasGas(80) && (A.hasGas(180) || dragoons >= 6))) return true;
        if (dragoons >= 2 && A.supplyUsed() >= 38 && Army.strength() >= 120 && A.hasMinerals(240)) return true;

        return false;
    }

    private Boolean againstZerg(atlantis.decisions.board.BoardState board) {
        if (!"Zerg".equals(board.enemyRace)) return null;

        if (board.dragoonsUnfinished <= 1) return false;

        if (board.dragoonsUnfinished >= 3 || board.enemyHydras > 0) return true;

        return null;
    }

    private Boolean forForgeExpand(atlantis.decisions.board.BoardState board) {
        if (board.cannonCount >= 2 && board.dragoonsUnfinished > 0) {
            return true;
        }

        return null;
    }

    private Boolean againstProtoss(atlantis.decisions.board.BoardState board) {
        int minDragoons = board.spartaMission ? 2 : 3;

        if ("Protoss".equals(board.enemyRace)) {
            if (board.dragoonsUnfinished <= minDragoons) return null;
            if (board.dragoonsUnfinished >= minDragoons + 2) return true;
        }

        return null;
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
