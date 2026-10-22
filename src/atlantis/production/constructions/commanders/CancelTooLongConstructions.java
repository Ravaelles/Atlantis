package atlantis.production.constructions.commanders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.cancelling.CancelNotStarted;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class CancelTooLongConstructions {
    public static void cancelCauseTakingTooLongIfNeeded(
        Construction constr, AUnitType type, AUnitType buildingType
    ) {
        // PREPARING waits by design (site picked early, no builder yet) -
        // cancelling it for age destroys the reservation and, with the
        // requeue vetoed by untriggerable rows, deadlocks the game (9/9
        // with no Pylon order left, measured game 200018).
        if (constr.status().isWaiting()) {
            return;
        }

        // A builder on foot is progress, not a stall (Wine: #5 walked 48 s
        // to a 10-tile site, got cancelled for age, yet the engine accepted
        // his order 5 s later). The timeout prices distance but not a slow
        // walk; an alive, moving builder converges. Fleeing reads moving
        // too - accepted gap, death still cancels.
        atlantis.units.AUnit assigned = constr.builder();
        if (assigned != null && assigned.isAlive() && assigned.isMoving()) {
            return;
        }

        if (
            tookTooLong(constr)
                && A.canAfford(type)
                && A.hasMinerals(type.mineralPrice() + 16)
                && (type.gasPrice() == 0 || A.hasGas(type.gasPrice() + 8))
        ) {

            APosition buildPosition = constr.buildPosition();

            // Replace-before-cancel (owner's ask): a stuck construction may sit
            // on a site that will never work (unpowered, crowded) while free
            // tiles exist nearby - blindly cancelling it loses the worker
            // assignment and the requeue may land on the same tile. So first
            // send a fresh request with the old construction still alive (the
            // duplicate guard inside sees it): a genuinely different site
            // replaces the old one, anything else keeps it.
            if (tryReplaceWithFreshSite(constr, type, buildPosition)) return;

            constr.cancel(type + " took too long (" + tookSeconds(constr) + "s)");
            ErrorLog.printMaxOncePerMinute(
                "Cancel constr of " + type
                    + " (Took too long)"
                    + " buildable:" + buildPosition.isBuildableIncludeBuildings()
                    + " (Supply " + A.supplyUsed() + "/" + A.supplyTotal() + ")"
            );

            constructionCancelledRequestAgainBecauseItsImportant(buildingType, buildPosition);
        }
    }


    /**
     * Sends a fresh construction request for the same building while the stuck
     * one is still alive, and cancels the old one only when the fresh request
     * lands somewhere else.
     *
     * <p>
     * The old construction is the duplicate guard's reference point: a fresh
     * search that can do no better returns the same tile (or nothing), the
     * request path cancels the duplicate itself, and the old construction
     * survives untouched - "placement blocks us" keeps the status quo, exactly
     * as specified. A different site means the new construction owns a builder
     * already, so releasing the old worker is safe.
     * </p>
     *
     * @return true when the old construction was replaced.
     */
    public static boolean tryReplaceWithFreshSite(
        Construction old, AUnitType type, APosition oldPosition
    ) {
        java.util.Set<Construction> before = new java.util.HashSet<>(
            atlantis.production.constructions.ConstructionRequests.constructions);

        // A freestanding probe order: the search refuses to run without one
        // (no order -> no position, by construction), and it never enters the
        // queue - it only carries the request. If the replace succeeds, the
        // old construction's real queue order is transferred to the fresh one.
        atlantis.production.orders.production.queue.order.ProductionOrder probeOrder =
            new atlantis.production.orders.production.queue.order.ProductionOrder(
                type, oldPosition, 0);

        atlantis.production.constructions.NewConstructionRequest.requestConstructionOf(
            type, oldPosition, probeOrder);

        // Only what the request above ADDED counts: a pre-existing sibling at
        // a different site must never be mistaken for the replacement (it has
        // its own builder and its own life).
        Construction fresh = null;
        for (Construction construction : atlantis.production.constructions.ConstructionRequests.constructions) {
            if (before.contains(construction)) continue;
            if (!construction.buildingType().equals(type)) continue;
            if (construction.buildPosition() == null) continue;
            fresh = construction;
        }
        if (!shouldReplace(oldPosition, fresh, type)) {
            if (fresh != null) {
                fresh.cancel(type + " re-place landed on the same site, keeping the original");
            }
            return false;
        }

        atlantis.production.orders.production.queue.order.ProductionOrder oldOrder = old.productionOrder();
        if (oldOrder != null) {
            old.setProductionOrder(null);
            fresh.setProductionOrder(oldOrder);
        }
        old.cancel(type + " re-placed to a fresh site at " + fresh.buildPosition());
        ErrorLog.printMaxOncePerMinute(
            "REPLACED " + type.name() + " " + oldPosition + " -> " + fresh.buildPosition());

        return true;
    }

    /**
     * Replace only with a live replacement at a genuinely different site. A
     * missing replacement (placement blocks us) or the same site keeps the
     * original: cancelling without something better in hand is strictly worse.
     * Pure decision, pinned directly.
     */
    public static boolean shouldReplace(APosition oldPosition, Construction fresh, AUnitType type) {
        if (fresh == null || fresh.buildPosition() == null) return false;

        return isDifferentSite(oldPosition, fresh.buildPosition(), type);
    }

    /**
     * Two sites are the same request when they overlap the building's own
     * footprint - anything closer would have been caught by the duplicate
     * guard, and anything farther is a genuinely different placement.
     * Package-visible as a seam so the replace rule is assertable.
     */
    public static boolean isDifferentSite(APosition oldPosition, APosition freshPosition, AUnitType type) {
        if (oldPosition == null || freshPosition == null) return false;

        double minSeparation = Math.max(type.getTilesWidth(), type.getTilesHeights());

        return oldPosition.distTo(freshPosition) > minSeparation;
    }

    private static boolean tookTooLong(Construction constr) {
        int timeoutSeconds = timeoutInSeconds(constr);

        return tookFrames(constr) / 30 > timeoutSeconds;
    }

    private static int timeoutInSeconds(Construction constr) {
        AUnitType type = constr.buildingType();
        AUnit main = Select.main();

        int base = 14;
        int bonus = 0;

        if (We.protoss()) {
            if (A.hasMinerals(250)) {
                bonus += 6;
            }
        }

        int timeoutSeconds = base + bonus + (
                + (type.isBase() || type.isCombatBuilding() ? 20 : 0)
                + ((int) (2 * constr.buildPosition().groundDistanceTo(main != null ? main : constr.builder())))
        );



        return timeoutSeconds;
    }

    private static int tookFrames(Construction constr) {
        return AGame.now() - constr.timeOrdered();
    }

    private static int tookSeconds(Construction constr) {
        return tookFrames(constr) / 30;
    }

    private static boolean constructionCancelledRequestAgainBecauseItsImportant(AUnitType type, APosition oldPosition) {
        ProductionOrder newOrder = null;

        if (We.protoss()) {
            // CANCEL FIRST, then decide whether to re-request.
            //
            // The guard below used to run BEFORE this cancel, and that inverted its meaning:
            // it asked "do we already have the maximum not-started ones?" while still counting
            // the very construction this method exists to replace. With
            // `maxAllowedAtOnce(Gateway) = 2`, a plan legitimately holding two not-started
            // Gateways read as "we have enough", the method returned early, the construction
            // stayed cancelled, and the requeue never ran at all (measured in a full game:
            // `### FAILED ### Requesting` = 0 and `Taking too long, re-try` = 0, ending with 25
            // Probes, 834 minerals and zero Gateways).
            //
            // Cancelling is exactly what frees the slot the guard is asking about, so the
            // order has to be: remove, then count, then decide. `CancelNotStarted.cancel`
            // cancels the not-started ones of this type (including a stale sibling), which is
            // what the earlier code was reaching for three lines too late.
            CancelNotStarted.cancel(type, "Taking too long, re-try");

            // Covered tech needs no rescue (measured: three standing Cores
            // after stale duplicates kept dying overdue - each death
            // re-ordered while a sibling stood or warped). The truly lost
            // case still requeues below; a dropped warp is re-ordered by
            // the pump itself within frames. Builder stays freed either way.
            if (techAlreadyCovered(type)) {
                return false;
            }

            if (shouldVetoRequeue(type)) {
                return false;
            }

            newOrder = AddToQueue.withHighPriority(type);

            // A rescue is not new demand: the dedup wall refused it while
            // the game had no Pylon at all (Wine: queued rows present,
            // newOrder null, no veto). Force it in directly - flood is still
            // capped because the rescue row counts as triggerable for the
            // next veto.
            if (newOrder == null) {
                newOrder = forceRescueOrder(type);
            }

            ErrorLog.printMaxOncePerMinute(
                "### FAILED ### Requesting " + type + " as it got cancelled\n" +
                "New requested order: " + newOrder
            );

            if (newOrder == null && type.isProtossImportantTechBuilding()) {
                newOrder = AddToQueue.withTopPriority(type);

                ErrorLog.printMaxOncePerMinute(
                    "### IMPORTANT ### Re-request failed again " + type + "\n" +
                    "Re-requested: " + newOrder
                );
            }
        }

        return newOrder != null;
    }

    /**
     * A singleton-tech building already standing or warping covers the
     * demand - re-ordering it builds a duplicate. Multi-want types
     * (Pylon, Gateway, army) never take this exit. Public as a test seam.
     */
    public static boolean techAlreadyCovered(AUnitType type) {
        if (!type.isProtossImportantTechBuilding()) return false;

        return Select.countOurOfTypeWithUnfinished(type) > 0;
    }

    private static boolean shouldVetoRequeue(AUnitType type) {
        int triggerable = countTriggerableNotStarted(type);
        if (triggerable >= maxAllowedAtOnce(type)) {
            // Diagnostic (owner's Wine game: a Nexus cancelled after 130s
            // was never re-requested, and the log could not say whether the
            // producer was gated out or the request was refused). Names the
            // veto with its counts.
            ErrorLog.printMaxOncePerMinute(
                "REQUEUE_VETO " + type.name()
                    + " notStartedInQueue=" + CountInQueue.countNotStarted(type)
                    + " triggerable=" + triggerable
                    + " maxAllowedAtOnce=" + maxAllowedAtOnce(type));

            return true;
        }
        return false;
    }

    /**
     * Only rows that could convert now count toward the requeue cap (game
     * 201239: four Pylon rows at 9/9 supply vetoed the one rebuild that
     * could unblock it - rows at 13/20/27 can never trigger while supply
     * is frozen, so counting them deadlocks). Public as a test seam.
     */
    public static int countTriggerableNotStarted(AUnitType type) {
        if (Queue.get() == null) return 0;

        int total = 0;
        for (ProductionOrder order : Queue.get().notStarted().ofType(type).list()) {
            if (order.minSupply() <= atlantis.game.A.supplyUsed()) total++;
        }
        return total;
    }

    private static int maxAllowedAtOnce(AUnitType type) {
        return (type.isGateway() || type.isPylon())
            ? 2
            : 1;
    }

    /**
     * Post-cancel rescue bypassing PreventDuplicateOrders: the dedup wall
     * fires over queued rows while the game has zero such buildings, which
     * is exactly when the rescue must go through (Wine first Pylon).
     * Public as a test seam.
     */
    public static ProductionOrder forceRescueOrder(AUnitType type) {
        ProductionOrder rescue = new ProductionOrder(type, atlantis.game.A.supplyUsed());
        rescue.setPriority(atlantis.production.orders.production.queue.order.ProductionOrderPriority.HIGH);
        atlantis.production.orders.production.queue.Queue.get().addNew(0, rescue);

        return rescue;
    }
}
