package atlantis.production.requests.produce;

import atlantis.game.A;
import atlantis.production.GoalBoardFeeder;
import atlantis.production.constructions.OutstandingBuildings;
import atlantis.production.orders.production.queue.PriorityWaiters;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The refusal gates of the single building door in evaluation order
 * (NEXT #155). The order is semantic - a gate reordered wrongly re-trips
 * an earlier refusal - so it lives here as one explicit list, not as an
 * if-chain whose sequence nothing pins. Slices migrate readers gate by
 * gate with a game proof each; the registry itself changes no behavior.
 */
public final class BuildGates {

    private BuildGates() {
    }

    private static final List<BuildGate> ALL = Collections.unmodifiableList(Arrays.asList(
        new BelowMinSupplyGate(),
        new LiveConstructionGate(),
        new CoreSingleInstanceGate(),
        new PylonCapGate(),
        new PylonOverstockGate(),
        new UnstartedPylonGate(),
        new WarpingLinkGate(),
        new EarlierReserveGate(),
        new FundingGate(),
        new CorePriorityGate(),
        new PowerNotFundedGate()
    ));

    /** Every refusal gate, evaluation order. Never null elements. */
    public static List<BuildGate> all() {
        return ALL;
    }

    // Owner 2026-10-16: no building starts below its supply on a
    // poor bank - revokes the old one-early rule (early claims
    // held builders while the worker pump starved). Below
    // minSupply only price+50 builds. Site picking (PREPARING,
    // two early, no builder) is unaffected.
    private static class BelowMinSupplyGate implements BuildGate {
        public String name() {
            return "below-min-supply-and-poor";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (A.supplyUsed() < order.minSupply()
                && !A.canAfford(type.mineralPrice() + 50, type.gasPrice())) return name();
            return null;
        }
    }

    // NEXT #57: an adopted construction with a living builder needs
    // no new request - the builder is dispatched or walking, and
    // re-requesting every frame piled same-tile duplicates (121
    // accepts in one game, totalOfType pinned at 1). Wait for warp,
    // cancel, or builder death instead. Builderless adoptions still
    // fall through to a fresh request below (status quo). STARTING
    // is the travelling half of the same pair (owner 2026-10-19):
    // unnamed here it minted a second site behind the walking
    // builder (Wine oid-72 storm: every BUILD row a fresh cid).
    private static class LiveConstructionGate implements BuildGate {
        public String name() {
            return "live-construction";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (ProduceBuilding.hasLiveConstruction(order)) return name();
            return null;
        }
    }

    // Owner 2026-10-17: single-instance structures (Core) refuse
    // while ANY is coming for another order - a sited sibling or
    // a warping building. Creation-time caps miss the
    // finish-removal vs re-request race (measured: #32 requested
    // one frame after #9 warped, #9's site already gone from the
    // list). Own traces excluded, so a same-order re-request after
    // a genuine loss still proceeds.
    private static class CoreSingleInstanceGate implements BuildGate {
        public String name() {
            return "core-single-instance";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (type.equals(AUnitType.Protoss_Cybernetics_Core)
                && OutstandingBuildings.excluding(order, type) >= 1) return name();
            return null;
        }
    }

    // Owner 2026-10-17: one supply block, one Pylon at a time (two
    // in flight max) - BO rows, the fallback and the supply
    // commander each fire alone correctly and triple together
    // (measured: warps at 3:31/3:35/3:45 from BO#8 + mirror + BO#11
    // at 25/25). The cap counts warping + not-started, so a row
    // whose need is already being built waits instead of re-siting.
    private static class PylonCapGate implements BuildGate {
        public String name() {
            return "pylon-cap";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (type.isPylon() && ProduceBuilding.tooManyPylonsInFlight()) return name();
            return null;
        }
    }

    // Owner: below 40 supply never more than 8 free - any Pylon
    // row (BO included: At-29 fired at 25/33 into 12 free) that
    // would push free past that waits. Above 40 the old 12-cap
    // in the queue door still applies.
    private static class PylonOverstockGate implements BuildGate {
        public String name() {
            return "pylon-overstock";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (type.isPylon() && A.supplyUsed() < 40 && A.supplyFree() > 8) return name();
            return null;
        }
    }

    // Owner 2026-10-19 (PurpleWave rule): one unstarted Pylon at a
    // time. A NOT_STARTED or STARTING Pylon construction for
    // ANOTHER order means supply is already on the way with no
    // unit to show for it yet - ordering a second one behind the
    // walking builder is how 30/48 slides toward 38/65. The new
    // row waits as a plain queued order instead. Own traces are
    // excluded, so this order replacing its own lost construction
    // still proceeds; PREPARING picks are someone's aimed future
    // (blocking on them could park an earlier need behind a later
    // row into a real block). Stuck sites unblock by themselves:
    // overdue cancels clear them and the count drops.
    private static class UnstartedPylonGate implements BuildGate {
        public String name() {
            return "unstarted-pylon";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (type.isPylon() && ProduceBuilding.anotherUnstartedPylonExists(order)) return name();
            return null;
        }
    }

    // Owner 2026-10-16: the order's building already warps - wait
    // for completion, do not site a second one. Warp-finish
    // releases the builder, so the guard above reads "builderless"
    // and re-requests the same order onto a fresh site (measured:
    // row #2 warped [16,48] at 1:20, re-requested [18,44] at
    // 1:48+1:52, two builders churned on it to game end). The link
    // is the order instance itself, so a sibling row of the same
    // type still proceeds - only this order's own warp blocks it.
    private static class WarpingLinkGate implements BuildGate {
        public String name() {
            return "warping-link";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (ProduceBuilding.orderHasWarpingBuilding(order, type)) return name();
            return null;
        }
    }

    // Earlier demand is reserved first (owner): a building may not
    // spend what an earlier-queued unfinished building still needs.
    // Earlier = smaller queue order id (own construction excluded,
    // orderless ones skipped - no sequence to compare). Check comes
    // before the wallet so a later row never eats an earlier row's
    // reserve, even when the bank looks affordable in isolation.
    // Only when the bank already covers this building alone: below
    // its own price the old aim-while-poor path decides (a walking
    // builder is free, the reserve question is moot).
    private static class EarlierReserveGate implements BuildGate {
        public String name() {
            return "earlier-reserve";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (order != null
                && A.canAfford(type.mineralPrice(), type.gasPrice())
                && !A.canAfford(
                    type.mineralPrice() + ProduceBuilding.earlierUnfinishedMinerals(order),
                    type.gasPrice() + ProduceBuilding.earlierUnfinishedGas(order))) return name();
            return null;
        }
    }

    // Wallet enforcement (NEXT #93): a fresh builder claim for a
    // board-owned type needs funding - otherwise the worker is held
    // for a building the bank cannot cover yet (the #53 shape:
    // held workers do not mine). Active-but-unfunded waits a frame;
    // inactive demand, goalless types and stale funding stay open.
    // Queue order outranks wallet funding (owner): a READY
    // build-order row that covers its own price plus earlier
    // unfinished demand is issued even when the wallet funds
    // something else (measured UMS 7th_rav: BO Core #5 PREPARING
    // from 2:00, bank 250-370, unfunded behind a dynamic Gateway
    // while the queue order sat). Dynamic demand still needs
    // funding; affordability (own + earlier reserve) is checked
    // first, so the #53 shape cannot return through this door.
    private static class FundingGate implements BuildGate {
        public String name() {
            return "funding";
        }

        public String check(AUnitType type, ProductionOrder order) {
            boolean queueOrdered = order != null && !order.isDynamic();
            boolean affordable = A.canAfford(
                type.mineralPrice() + ProduceBuilding.earlierUnfinishedMinerals(order),
                type.gasPrice() + ProduceBuilding.earlierUnfinishedGas(order));
            if (!GoalBoardFeeder.adoptionAllowed(type) && !(queueOrdered && affordable)) return name();
            return null;
        }
    }

    // Owner 2026-10-15: a waiting Core outranks Pylon spending
    // while contended (bounded, self-releasing - see
    // PriorityWaiters.pylonYieldsToCore). The 100 minerals stay
    // saved for the Core instead of going into a forward Pylon.
    private static class CorePriorityGate implements BuildGate {
        public String name() {
            return "core-priority";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (type.isPylon() && PriorityWaiters.pylonYieldsToCore()) return name();
            return null;
        }
    }

    // Owner 2026-10-19 (Wine: the At-22 Gateway's builder started
    // while its At-21 Pylon was still PREPARING, then the bank sat
    // below 100 for the Pylon while the Gateway held a worker on
    // unpowered ground): a power-needing building with no standing
    // Pylon waits until the bank covers the Pylon plus itself -
    // never send the worker before the power is paid for.
    private static class PowerNotFundedGate implements BuildGate {
        public String name() {
            return "power-not-funded";
        }

        public String check(AUnitType type, ProductionOrder order) {
            if (ProduceBuilding.powerProviderNotFunded(type)) return name();
            return null;
        }
    }
}
