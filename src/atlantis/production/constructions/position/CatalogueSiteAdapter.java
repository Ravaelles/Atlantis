package atlantis.production.constructions.position;

import atlantis.config.env.Env;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.constructions.position.modifier.ModifierAnchor;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.placement.PlacementPlanner;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.map.ReachableTiles;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.HashSet;
import java.util.Set;

/**
 * Legacy site search answered from the catalogue planner: two entries
 * (queue rows with orders, sited requests without), one shared core.
 *
 * <p>
 * The legacy pipeline ({@code Construction} → builders → refresh) keeps
 * owning builders, statuses and retries; only the "which tile" question is
 * delegated, through the same {@code PlacementPlanner} seam the v2
 * scheduler consumes. Anything out of scope - or any catalogue failure -
 * declines with null and the caller falls back to {@code FindPosition}, so
 * a refused catalogue answer can never do worse than today. Selected by
 * {@code PLACEMENT=catalogue}, default off.
 * </p>
 *
 * <p>
 * Scope (one place: {@code SiteScope}): Protoss generic buildings.
 * Diagnostics (one place: {@code SiteTwin}): every answer, decline and
 * perf sample as {@code CATALOGUE_SITE} rows.
 * </p>
 */
public final class CatalogueSiteAdapter {

    /**
     * One planner per game frame, shared by every site search in that frame.
     * A fresh planner rebuilds the whole-map grid + catalogue at
     * construction (measured 2026-10-18: per-search rebuilds made capped
     * runs unfinishable, 35 s wall for 208 game-s vs 6 s legacy). The
     * frame's first search pays one rebuild; later searches reuse the pass -
     * which additionally keeps two constructions off one tile, the same pass
     * semantics the v2 scheduler relies on. Same static-state shape as the
     * {@code APositionFinder} cache next door; single-threaded game loop.
     */
    private static CataloguePlacementPlanner sharedPlanner;
    private static int sharedPlannerFrame = -1;
    private CatalogueSiteAdapter() {
    }

    private static synchronized PlacementPlanner plannerForFrame(int frame) {
        if (sharedPlanner == null || sharedPlannerFrame != frame) {
            // The constructor already builds the grid + catalogue, so no
            // startPass here (it would build everything twice per frame).
            sharedPlanner = new CataloguePlacementPlanner();
            sharedPlannerFrame = frame;
            SiteTwin.addRebuild();
        }
        return sharedPlanner;
    }

    /**
     * A catalogue tile for this construction, or null when the catalogue
     * declines and the legacy search must answer. Uses the frame's shared
     * planner. Flag-gated inside: with the flag off this answers null and
     * the legacy path runs exactly as before (one home for the flag, both
     * entries, so the callers never branch on it).
     */
    public static APosition siteFor(AUnit builder, AUnitType building, Construction construction) {
        if (!Env.placementCatalogue()) return null;

        return answerWithTwin(siteForInner(plannerForFrame(AGame.now()), builder, building, construction),
            building, "funnel");
    }

    /**
     * Same, with the planner injected: the seam the tests use to hand over a
     * synthetic grid instead of the live map.
     */
    public static APosition siteFor(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        planner.startPass();
        return answerWithTwin(siteForInner(planner, builder, building, construction),
            building, "funnel");
    }

    /**
     * Sited requests ({@code RequestBuildingNear}: cannons, forward pylons)
     * search with no order and no construction yet. Same propose + validate
     * + range discipline, same null-means-legacy contract, selected by the
     * same flag. The caller keeps its validations, queueing and
     * construction afterwards, unchanged.
     */
    public static APosition siteForSited(AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        if (!Env.placementCatalogue()) return null;

        return siteForSited(plannerForFrame(AGame.now()), builder, building, nearTo, maxDistance);
    }

    /**
     * Same, with the planner injected: the seam the tests use.
     */
    public static APosition siteForSited(PlacementPlanner planner, AUnit builder, AUnitType building, HasPosition nearTo, double maxDistance) {
        String scope = SiteScope.declineReason(building);
        if (scope != null) {
            SiteTwin.declined(building, scope, "sited");

            return null;
        }
        if (building == null) return null;

        planner.startPass();
        TargetPlacement constraint = constraintForSited(nearTo, maxDistance);
        PlacementReservation reservation = propose(planner, builder, building, constraint, "sited",
            new HashSet<String>(), site -> withinRange(site, nearTo, maxDistance));
        if (reservation == null) {
            return null;
        }
        APosition site = APosition.create(reservation.tileX(), reservation.tileY());
        return answerWithTwin(site, building, "sited", reservation, constraint);
    }

    private static APosition siteForInner(PlacementPlanner planner, AUnit builder, AUnitType building, Construction construction) {
        if (construction == null || building == null) return null;

        String scope = SiteScope.declineReason(building);
        if (scope != null) {
            SiteTwin.declined(building, scope, "funnel");

            return null;
        }

        ProductionOrder order = construction.productionOrder();
        atlantis.production.demand.Demand demand = construction.demand();
        if (order == null && demand == null) {
            SiteTwin.declined(building, "no-order", "funnel");

            return null;
        }

        TargetPlacement constraint = demand != null
            ? constraintFor(demand, construction)
            : constraintFor(order, construction);

        // A construction that names a maxDistance hunts inside it: the
        // legacy ring search never left it, and an unbounded rank around
        // the anchor serves global optima the caller never asked for
        // (measured stub: base demand anchored at the natural served a
        // start location 110 tiles away). Non-positive means unbounded,
        // as in legacy - the V2 door and sited callers opt in by setting
        // it (V2 home radius, sited maxDistance, base neighbourhood).
        double maxDistance = construction.maxDistance();
        if (maxDistance > 0
            && constraint.mode() == TargetPlacement.Mode.NEIGHBOURHOOD) {
            constraint = TargetPlacement.inNeighbourhood(
                constraint.tileX(), constraint.tileY(), maxDistance);
        }

        // V3 S6 slice 2: the once-planned layout owns issuance - a free
        // reserved slot serves before the per-frame hunt, under the same
        // verdict, physics and reachability the hunted tile would face.
        // A refused slot falls through to the hunt below, never declines -
        // and joins that hunt's exclusion list, so the hunt cannot
        // re-serve on verdict grounds what the slot path just refused.
        Set<String> excluded = new HashSet<String>();
        APosition slot = firstFreeSlot(building);
        if (slot != null) {
            SitingVerdict slotVerdict = SitingVerdict.check(building, slot);
            if (!slotVerdict.accepted()) {
                SiteTwin.declined(building, slotVerdict.twin, "slot",
                    constraint.nearTileX(), constraint.nearTileY());
                if ("unpowered".equals(slotVerdict.twin)) {
                    queueSlotSupportPylon(building, slot);
                }
                excluded.add(tileKey(slot.tx(), slot.ty()));
            }
            else if (!CanPhysicallyBuildHere.check(builder, building, slot)) {
                SiteTwin.declined(building, "legacy-refused", "slot",
                    constraint.nearTileX(), constraint.nearTileY());
                excluded.add(tileKey(slot.tx(), slot.ty()));
            }
            else if (unreachableFromMain(slot)) {
                SiteTwin.declined(building, "unreachable", "slot",
                    constraint.nearTileX(), constraint.nearTileY());
                excluded.add(tileKey(slot.tx(), slot.ty()));
            }
            else {
                return answerWithTwin(slot, building, "slot", null, constraint);
            }
        }

        PlacementReservation reservation = propose(planner, builder, building, constraint, "funnel", excluded);
        if (reservation == null) return null;

        return answerWithTwin(
            APosition.create(reservation.tileX(), reservation.tileY()), building, "funnel", reservation,
            constraint);
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via) {
        return answerWithTwin(site, building, via, null, TargetPlacement.anywhere());
    }

    private static APosition answerWithTwin(APosition site, AUnitType building, String via,
                                            PlacementReservation reservation, TargetPlacement constraint) {
        SiteTwin.answered(building, site, via, reservation, constraint);

        return site;
    }

    /**
     * The first reserved slot of this type no live building covers
     * (V3 S6): standing and unfinished footprints both occupy, so a
     * warping Pylon and a parked row equally hold their slot. Single
     * implementation in {@link atlantis.placement.core.ReservedSlots};
     * this is just the call-site predicate (same overlap rule the
     * planner uses, so the two paths cannot disagree on occupancy).
     */
    private static APosition firstFreeSlot(AUnitType building) {
        return atlantis.placement.core.ReservedSlots.firstFreeSlotOfType(
            building,
            (tile, type) ->
                atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding
                    .check(tile, type)
                || atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding
                    .check(tile, type));
    }

    /**
     * An unpowered slot orders its support the Stardust way: the power
     * Pylon is part of the layout, so the slot names it instead of
     * waiting for the supply pump to notice. Same high-priority queue
     * the planner drain uses (no new ordering rules); the queue dedup
     * still owns flood control, so a refused repeat twins nothing.
     * The hint is the free Pylon slot covering the Gateway slot - a
     * tile a siting Pylon can actually land on (a bare Gateway tile
     * 16 tiles from any power is not). Only power-needing buildings.
     */
    private static void queueSlotSupportPylon(AUnitType building, APosition slot) {
        if (building == null || slot == null || !building.needsPower()) return;
        try {
            APosition pylonSlot = atlantis.placement.core.ReservedSlots.supportPylonSlotFor(
                slot, AUnitType.Protoss_Pylon,
                (tile, type) ->
                    atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding
                        .check(tile, type)
                        || atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding
                        .check(tile, type));
            if (pylonSlot == null) return;

            ProductionOrder support = AddToQueue.withHighPriority(
                AUnitType.Protoss_Pylon, pylonSlot);
            if (support == null) return;

            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CATALOGUE_SITE)
                .param("type", building.name())
                .param("result", "supported")
                .param("tx", slot.tx())
                .param("ty", slot.ty())
                .param("via", "slot")
                .param("support", AUnitType.Protoss_Pylon.name())
                .param("supportTx", pylonSlot.tx())
                .param("supportTy", pylonSlot.ty())
                .log(AGame.now(), atlantis.game.A.minSec());
        } catch (Exception | Error ignored) {
        }
    }

    /**
     * The main base must reach the proposal by ground (owner
     * 2026-11-01). Shared by the hunted tile and the reserved slot -
     * the two paths cannot disagree on reachability.
     */
    private static boolean unreachableFromMain(APosition site) {
        AUnit main = Select.mainOrAnyBuilding();
        if (main == null || main.position() == null) return false;

        return !ReachableTiles.between(main.position(), site);
    }

    /**
     * How many tiles one funnel call may try at most: the first pick plus
     * two alternatives. Each extra attempt is a full catalogue hunt, so the
     * bound keeps a declined demand from re-scanning the map every frame
     * (sited demand re-fires until it is answered or cancelled) while still
     * giving the neighbourhood a second and third look when the ranker's
     * favourite fails the verdict. A demand with no servable tile answers
     * once and waits for the next frame like before.
     */
    private static final int FUNNEL_MAX_TRIES = 3;

    private static String tileKey(int tx, int ty) {
        return tx + ":" + ty;
    }

    /**
     * The shared core: builder check, catalogue proposal, legacy validation.
     * Returns the validated reservation or null (decline already twinned).
     * A tile the verdict, physics or reachability refuses is excluded and
     * the hunt is asked again (bounded above): the ranker's favourite is
     * not always servable, but a refused tile never is.
     */
    private static PlacementReservation propose(PlacementPlanner planner, AUnit builder, AUnitType building,
                                                TargetPlacement constraint, String via) {
        return propose(planner, builder, building, constraint, via, new HashSet<String>(), null);
    }

    private static PlacementReservation propose(PlacementPlanner planner, AUnit builder, AUnitType building,
                                                TargetPlacement constraint, String via,
                                                Set<String> excluded) {
        return propose(planner, builder, building, constraint, via, excluded, null);
    }

    /**
     * @param inRange null means no range gate (the queue funnel has no
     * maxDistance); the sited door passes its withinRange check so an
     * out-of-neighbourhood tile excludes-and-retries like any other
     * refusal instead of failing the whole demand after a success.
     */
    private static PlacementReservation propose(PlacementPlanner planner, AUnit builder, AUnitType building,
                                                TargetPlacement constraint, String via,
                                                Set<String> excluded,
                                                java.util.function.Predicate<APosition> inRange) {
        // The catalogue proposes, the legacy validator disposes: a tile the
        // builder-time checks refuse would start a builder, fail refresh and
        // re-request in a loop (measured 2026-10-18: 236 requests vs 15 on
        // the control for the same opening). Decline instead of serving it.
        if (builder == null || !builder.isAlive()) {
            SiteTwin.declined(building, "no-builder", via, constraint.nearTileX(), constraint.nearTileY());

            return null;
        }

        for (int attempt = 0; ; attempt++) {
            PlacementReservation reservation = SiteTwin.timed(() -> planner.reservePlacementExcluding(
                UnitProducible.of(building), constraint, AGame.now(), excluded
            ));
            for (atlantis.placement.core.SupportRequest support : planner.drainSupportRequests()) {
                AddToQueue.withHighPriority(
                    support.unitType(),
                    APosition.create(support.tileX(), support.tileY()));
            }
            if (reservation == null || !reservation.isSuccessful()) {
                // Only the first failure names no-tile: on later attempts the
                // declines above already told which tiles blocked the hunt.
                if (excluded.isEmpty()) {
                    SiteTwin.declined(building, "no-tile", via, constraint.nearTileX(), constraint.nearTileY());
                }

                return null;
            }

            APosition site = APosition.create(reservation.tileX(), reservation.tileY());
            // Cheapest gate first: the legacy ring search stayed within
            // maxDistance of near, so an out-of-neighbourhood tile
            // excludes-and-retries here instead of failing a served
            // demand downstream (measured stub: ranker's favourite
            // declined on edge, second tile declined too-far, demand
            // died while the legacy tile passed the verdict).
            if (inRange != null && !inRange.test(site)) {
                SiteTwin.declined(building, "too-far", via, constraint.nearTileX(), constraint.nearTileY());
                if (attempt + 1 >= FUNNEL_MAX_TRIES) return null;
                excluded.add(tileKey(site.tx(), site.ty()));

                continue;
            }
            // V3 S1: one verdict owns refused / power-at-footprint-center /
            // geyser-minerals / worker-cover, so the funnel can never serve a
            // tile the legacy spiral would decline. Physics (occupancy, engine
            // canBuildHere, early allowances) stays with CanPhysicallyBuildHere
            // below - the verdict answers "should we", physics answers "could we".
            SitingVerdict verdict = SitingVerdict.check(building, site);
            if (!verdict.accepted()) {
                SiteTwin.declined(building, verdict.twin, via, constraint.nearTileX(), constraint.nearTileY());
                if (attempt + 1 >= FUNNEL_MAX_TRIES) return null;
                excluded.add(tileKey(site.tx(), site.ty()));

                continue;
            }
            if (!CanPhysicallyBuildHere.check(builder, building, site)) {
                SiteTwin.declined(building, "legacy-refused", via, constraint.nearTileX(), constraint.nearTileY());
                if (attempt + 1 >= FUNNEL_MAX_TRIES) return null;
                excluded.add(tileKey(site.tx(), site.ty()));

                continue;
            }
            // Owner 2026-11-01: the main base must reach the site by ground.
            // Last and once per passing tile (reachability is the costly
            // query): a disconnected region (UMS/island pocket) declines
            // instead of stranding the builder. Skipped when no base
            // stands yet (first frame) - there is nothing to measure from.
            if (unreachableFromMain(site)) {
                SiteTwin.declined(building, "unreachable", via, constraint.nearTileX(), constraint.nearTileY());
                if (attempt + 1 >= FUNNEL_MAX_TRIES) return null;
                excluded.add(tileKey(site.tx(), site.ty()));

                continue;
            }

            return reservation;
        }
    }

    /**
     * The legacy ring search stays within maxDistance of near; a catalogue
     * answer beyond it would queue an exact order somewhere the caller never
     * asked for. Decline instead (negative means unbounded, as in legacy).
     * Public as a seam: pure range math, pinned directly.
     */
    public static boolean withinRange(APosition site, HasPosition nearTo, double maxDistance) {
        if (site == null || nearTo == null || maxDistance < 0) return true;

        return site.distTo(nearTo) <= maxDistance;
    }

    /**
     * The order's request as a catalogue constraint: an exact tile stays
     * exact (the planner re-validates it), a neighbourhood hint stays a
     * neighbourhood, and no hint means anywhere. Public as a seam: the
     * translation is pinned directly, without map physics.
     */
    public static TargetPlacement constraintFor(ProductionOrder order, Construction construction) {
        if (order.isUsingExactPosition() && order.aroundPosition() != null) {
            APosition exact = APosition.create(order.aroundPosition());

            return TargetPlacement.exactTile(exact.tx(), exact.ty());
        }
        // Migration A4b (owner #146): legacy order modifiers (@NATURAL,
        // @MAIN, choke names...) anchor the neighbourhood instead of
        // degrading to anywhere. The spiral used to re-derive these per
        // search; the catalogue ranks around the same anchor.
        // Legacy precedence (A4g): an explicitly set nearTo wins over the
        // generic modifier - the modifier names a neighbourhood class,
        // nearTo names the tile. Same order as the spiral used.
        HasPosition anchor = construction.nearTo() != null
            ? construction.nearTo()
            : ModifierAnchor.resolve(order.getModifier());
        if (anchor != null) {
            APosition centre = APosition.create(anchor);

            return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
        }
        return TargetPlacement.anywhere();
    }

    /**
     * The demand's request as a catalogue constraint (NEXT #228, WO-147):
     * same translation as the order above, read from the v2 carrier.
     * The funnel prefers this overload; the order overload stays for the
     * legacy path and its pins.
     */
    public static TargetPlacement constraintFor(atlantis.production.demand.Demand demand,
                                                Construction construction) {
        if (demand.isExact() && demand.around() != null) {
            APosition exact = APosition.create(demand.around());

            return TargetPlacement.exactTile(exact.tx(), exact.ty());
        }
        // Same modifier anchoring as the order overload (A4b), same
        // legacy precedence (A4g): explicit nearTo wins.
        HasPosition anchor = construction.nearTo() != null
            ? construction.nearTo()
            : ModifierAnchor.resolve(demand.modifier());
        if (anchor != null) {
            APosition centre = APosition.create(anchor);

            return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty());
        }
        return TargetPlacement.anywhere();
    }

    private static TargetPlacement constraintForSited(HasPosition nearTo, double maxDistance) {
        if (nearTo == null) return TargetPlacement.anywhere();

        APosition centre = APosition.create(nearTo);

        return TargetPlacement.inNeighbourhood(centre.tx(), centre.ty(), maxDistance);
    }
}
