package atlantis.production;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * One SUPPLY_BLOCKED witness for both engines (NEXT #150 follow-up):
 * the V2 snapshot owns live games, the legacy production commander
 * owns OFF games - both call this with the same provider/free/coming
 * triple, so the rows read identically whichever engine played.
 *
 * <p>Names the providers a tight supply goal is waiting on: id, tile,
 * hp and whether the engine still calls them uncompleted. A provider
 * coming for minutes (cancelled warp, phantom entry) suppresses the
 * supply goal forever with zero rows anywhere - this is the row that
 * names it. Throttled: suppression is a state, not an event.</p>
 */
public final class SupplyBlockedTwin {

    private SupplyBlockedTwin() {
    }

    public static void sample(AUnitType provider, int supplyFree, int supplyComing) {
        if (!A.everyNthGameFrame(300)) return;
        if (provider == null) return;
        // Always sample: coming==0 with no supply goal means the bug is in
        // the goal/scheduler, not in suppression. Per-unit rows below name
        // the blockers when suppressed - read from the RAW list, the same
        // source the coming count uses (alive-filtered views hide corpses).
        EventLog.key(LogKey.SUPPLY_BLOCKED)
            .param("type", provider.name())
            .param("result", "sample")
            .param("free", supplyFree)
            .param("coming", supplyComing)
            .log(AGame.now(), A.minSec());
        if (supplyFree > atlantis.production.v2.goals.DynamicGoals.SUPPLY_WORRY_LEVEL) return;
        if (supplyComing <= 0) return;

        for (AUnit unit : atlantis.units.select.BaseSelect.ourUnitsWithUnfinishedList()) {
            if (unit == null || !unit.is(provider)) continue;
            EventLog.key(LogKey.SUPPLY_BLOCKED)
                .param("type", provider.name())
                .param("result", "blocker")
                .param("id", unit.id())
                .param("tx", unit.tx())
                .param("ty", unit.ty())
                .param("hp", unit.hp())
                .param("completed", unit.isCompleted())
                .param("alive", unit.isAlive())
                .log(AGame.now(), A.minSec());
        }
        // Constructions without a unit yet suppress the same way (see
        // supplyProvidersComing): without a named row a coming>0 sample
        // with no blocker units reads as a phantom entry. Same throttle
        // as the samples above - suppression is a state, not an event.
        for (Construction construction : ConstructionRequests.constructions) {
            if (construction == null || !provider.equals(construction.buildingType())) continue;
            EventLog.key(LogKey.SUPPLY_BLOCKED)
                .param("type", provider.name())
                .param("result", "blocker-construction")
                .param("cid", construction.id())
                .param("status", String.valueOf(construction.status()))
                .param("tile", construction.buildPosition() == null ? "-"
                    : "[" + construction.buildPosition().tx() + ","
                    + construction.buildPosition().ty() + "]")
                .log(AGame.now(), A.minSec());
        }
    }

    /**
     * Race supply provider for the legacy path (mirrors the v2 race
     * policies one by one - same provider, same rows, whichever engine
     * played). Null when the race builds no site provider.
     */
    public static AUnitType legacyProvider() {
        if (atlantis.util.We.protoss()) return AUnitType.Protoss_Pylon;
        if (atlantis.util.We.terran()) return AUnitType.Terran_Supply_Depot;
        if (atlantis.util.We.zerg()) return AUnitType.Zerg_Overlord;

        return null;
    }

    /** Coming providers from engine state alone (no V2 snapshot needed). */
    public static int legacyComing(AUnitType provider) {
        return GameStateSnapshot.supplyProvidersComing(provider);
    }
}
