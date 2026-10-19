package atlantis.production.constructions;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.builders.GetOptimalBuilder;
import atlantis.production.constructions.cancelling.CancelConstruction;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;
import atlantis.units.workers.gather.GatherResources;
import atlantis.util.AConsole;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

/**
 * Represents construction of a building, including ones not yet started.
 */
public class Construction implements Comparable<Construction> {
    private static Cache<Object> cache = new Cache<>();

    private static int _firstFreeId = 1;
    private final int ID = _firstFreeId++;
    private final int timeOrdered;
    private int timeBecameInProgress;
    /**
     * Frame of the last engine build attempt for this construction. The
     * demote rule must not punish the pre-attempt wait: a worker that stood
     * 250 frames saving up and then issued will otherwise demote a few frames
     * later on the same standstill clock (measured: demote 7f after an
     * accepted attempt).
     */
    private int lastBuildAttemptFrame = -999999;
    private AUnitType buildingType;
    private AUnit buildingUnit;
    private AUnit builder;
    /**
     * Owner 2026-10-18: the worker picked at PREPARING time. Inert marker
     * only - nothing claims on it (BuilderManager reads {@link #builder}),
     * so the worker keeps mining until adoption. Adoption prefers this
     * worker when still free and alive, else searches optimally. It names
     * the intent early without holding anyone.
     */
    private AUnit reservedBuilder;
    private APosition positionToBuild;
    private HasPosition near;
    private double maxDistance;
    private ProductionOrder productionOrder;
    private ConstructionOrderStatus status;

    // =========================================================

    public Construction(AUnitType buildingType) {
        this.buildingType = buildingType;

        status = ConstructionOrderStatus.NOT_STARTED;
        timeOrdered = AGame.now();
    }

    // =========================================================

    /**
     * If it's impossible to build in given position (e.g. occupied by units), find new position.
     */
    public APosition findPositionForNewBuilding() {
        return (APosition) cache.get(
            "findPositionForNewBuilding:" + genericCacheKey(),
            57,
            () -> APositionFinder.findPositionForNew(builder, buildingType, this)
        );
    }

    private String genericCacheKey() {
        String cacheKey = "";

        if (buildingType != null) cacheKey += buildingType.id() + ",";
        if (builder != null) cacheKey += builder.id() + ",";
        if (buildingUnit != null) cacheKey += buildingUnit.position();

        return cacheKey;
    }

    /**
     * In order to find a tile for building, one worker must be assigned as builder. We can assign any worker
     * and we're cool, bro.
     */
    protected void assignRandomBuilderForNow() {
        // Never a scout, never another row's builder, never a repairer or
        // a choke blocker: "random for the time being" draws only from
        // workers truly free of serious duties (owner: always
        // FreeWorkers.get(), never a hand-rolled filter chain - units can
        // hold many jobs at once and every chain risks forgetting one).
        // A double-linked builder starves one of its sites the same way
        // a scouted-off builder never builds.
        if (near != null) {
            builder = FreeWorkers.get().nearestTo(near);
        }
        else {
            builder = FreeWorkers.get().first();
        }
//        if (near != null) {
//            builder = FreeWorkers.get().nearestTo(near);
//        }
//        else {
//            builder = FreeWorkers.get().first();
//        }
    }

    /**
     * Assigns optimal builder for this building. Worker closest to this place.
     *
     * <p>Owner: a dispatched builder with supply met travels and waits -
     * never recalled for minerals (measured: a Core warping first turned
     * the walking Pylon builder back to minerals, the Assimilator jumped
     * the queue, the Pylon landed last). A live, unstuck builder on a
     * supply-met row is kept, full stop - the walk overlaps earning
     * instead of queuing behind it, so delaying the building for a
     * pseudo-optimization is not an option. Below-min-supply early prep
     * stays fluid (reselection is its point), and a wedged builder is
     * still replaceable (stuck, not a bank dip).
     *
     * @return AUnit for convenience it returns
     */
    public AUnit assignOptimalBuilder() {
        AUnit oldBuilder = builder;

        if (oldBuilder != null && oldBuilder.isAlive() && !oldBuilder.isStuck()
            && !oldBuilder.isScout() && supplyMetFor(productionOrder)) {
            return oldBuilder;
        }

        AUnit optimalBuilder = GetOptimalBuilder.forPosition(this, productionOrder);

        if (optimalBuilder != null) {
            builder = optimalBuilder;
            if (oldBuilder != null && !oldBuilder.equals(optimalBuilder)) {
//                oldBuilder.stop("OldBuilder");
                (new GatherResources(oldBuilder)).forceHandle();
            }
        }
        else ErrorLog.printMaxOncePerMinute("No optimal builder for " + buildingType);

        return builder;
    }

    /**
     * The owner's line for the builder race: supply met means the row is
     * due, and a due row's walking builder is never a savings account.
     */
    private static boolean supplyMetFor(ProductionOrder order) {
        return order != null && A.supplyUsed() >= order.minSupply();
    }

    /**
     * Fully delete this construction, remove the building if needed by cancelling it.
     */
    public void cancel(String reason) {
        CancelConstruction.cancel(this, reason);
    }

    // =========================================================

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.ID;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Construction other = (Construction) obj;

        if (buildingType == null || positionToBuild == null) return false;

        return this.ID == other.ID;
    }

    /**
     * Is this the same construction request as another one - i.e. one we already have?
     *
     * <p>
     * <b>The position is deliberately NOT part of this (NEXT.md #53).</b> It used to be,
     * requiring the two sites to be exactly equal, and that made the duplicate check
     * useless in the one case it exists for. Measured: two Gateways alive at the same time
     * at `[94,111]` and `[98,111]` - four tiles apart, the same type, the same supply gate -
     * and `sameAs` answered <b>false</b> because the tiles differed. One of them was built,
     * the other waited forever for a building that was already going up four tiles away,
     * holding its worker for the rest of the game.
     * </p>
     *
     * <p>
     * It was also self-contradictory with its only caller.
     * {@code ConstructionRequests.alreadyExists} requires {@code sameAs(...)} <b>and</b>
     * {@code positionToBuildCenter().distTo(...) <= 6} - so the distance test could never
     * do anything: if this method demanded identical tiles, the distance was always 0. The
     * caller's distance term is the "close enough to be the same request" rule, and it can
     * only work if this method lets near-but-not-identical sites through.
     * </p>
     *
     * <p>
     * Two constructions of the same type, for the same supply gate, within the caller's
     * six tiles ARE the same request: a building can only stand in one place, and the whole
     * point of the check is to notice that we are already building it.
     * </p>
     */
    public boolean sameAs(Construction other) {
        if (other == null || other.buildingType == null || this.buildingType == null) return false;

        // Null-safe supply gates (a construction without a queue order - e.g. from
        // the V2 director - used to NPE the whole duplicate check, and the crash
        // surfaced far away as a skipped request).
        int mySupply = this.productionOrder() != null ? this.productionOrder().minSupply() : 0;
        int otherSupply = other.productionOrder() != null ? other.productionOrder().minSupply() : 0;

        return this.buildingType.equals(other.buildingType)
            && (
            mySupply == otherSupply
                || (!this.buildingType.isCannon() && !other.buildingType.isCannon())
        );
    }

    @Override
    public int compareTo(Construction o) {
        return Integer.compare(ID, o.ID);
    }

    @Override
    public String toString() {
        return "Construction{" + "#" + ID + ", " + buildingType + ", build=" + buildingUnit + ", builder=" + builder + ", positionToBuild=" + positionToBuild + ", status=" + status + '}';
    }

    // =========================================================

    public APosition positionToBuildCenter() {
        APosition positionToBuild = buildPosition();
        if (positionToBuild != null) {
            return positionToBuild.translateByPixels(
                buildingType().dimensionLeftPixels(), buildingType().dimensionUpPixels()
            );
        }
        else {
            return null;
        }
    }

    public AUnitType buildingType() {
        return buildingType;
    }

    public void setBuildingType(AUnitType buildingType) {
        this.buildingType = buildingType;
    }

    public AUnit builder() {
        return builder;
    }

    public Construction setBuilder(AUnit builder) {
        this.builder = builder;
        return this;
    }

    public AUnit reservedBuilder() {
        return reservedBuilder;
    }

    public Construction setReservedBuilder(AUnit reservedBuilder) {
        this.reservedBuilder = reservedBuilder;
        return this;
    }

    public ConstructionOrderStatus status() {
        return status;
    }

    public void setStatus(ConstructionOrderStatus status) {
        this.status = status;

//        if (buildingType.isCyberneticsCore()) {
//            System.err.println("@@ STATUS " + status + " for " + this.buildingType);
//            AConsole.printStackTrace("---------");
//        }

        if (status.equals(ConstructionOrderStatus.IN_PROGRESS)) {
            timeBecameInProgress = A.now();
        }

        if (!status.equals(ConstructionOrderStatus.NOT_STARTED)) {
            AUnit building = buildingUnit();
            if (building != null && building.isProtoss()) setBuilder(null);
        }

        if (status.equals(ConstructionOrderStatus.FINISHED)) {
            ConstructionRequests.removeOrder(this);
        }
    }

    public APosition buildPosition() {
        return positionToBuild;
    }

    public void setPositionToBuild(APosition positionToBuild) {
        this.positionToBuild = positionToBuild;
    }

    public AUnit buildingUnit() {
        return buildingUnit;
    }

    public void setBuildingUnit(AUnit buildingBeingBuilt) {
        this.buildingUnit = buildingBeingBuilt;

        assert buildingType.equals(buildingUnit().type());
    }

    public int id() {
        return ID;
    }

    public ProductionOrder productionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
        if (productionOrder != null) this.productionOrder.setConstruction(this);
    }

    public HasPosition nearTo() {
        return near;
    }

    public void setNearTo(HasPosition near) {
        this.near = near;
    }

    public double maxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int timeOrdered() {
        return timeOrdered;
    }

    public int startedAgo() {
        return A.ago(timeBecameInProgress);
    }

    public void setLastBuildAttemptNow() {
        lastBuildAttemptFrame = AGame.now();
    }

    public int lastBuildAttemptAgo() {
        return A.ago(lastBuildAttemptFrame);
    }

    /**
     * No build order was ever issued for this site (attempts stamp the
     * frame; the sentinel predates any game frame). Used to tell owed
     * work apart from worked work (NEXT #57 vs #53).
     */
    public boolean buildWasNeverAttempted() {
        return lastBuildAttemptFrame < 0;
    }

    public boolean hasStarted() {
        return !status().equals(ConstructionOrderStatus.NOT_STARTED) || buildingUnit() != null;
    }

    public boolean notStarted() {
        return ConstructionOrderStatus.NOT_STARTED.equals(status);
    }

    public boolean notFinished() {
        return !ConstructionOrderStatus.FINISHED.equals(status);
    }

    public static void clearCache() {
        cache.clear();
    }

    /**
     * Has this construction waited long enough to be considered stuck?
     *
     * <p>
     * <b>"Not started yet" is not the same as "stuck", and the difference is the whole of
     * this method.</b> It is called from {@code OverdueConstructions}, whose reaction is
     * destructive: cancel the construction, cancel every other not-started one, and re-queue -
     * so a false positive here does not cost one building, it flushes the plan.
     * </p>
     *
     * <p>
     * <b>What went wrong (measured 2026-10-11, owner's log):</b> the only states recognised as
     * "progressing" were {@code status != NOT_STARTED} and {@code buildingUnit() != null}. The
     * second is always null on OpenBW (the {@code buildUnit()} defect, {@code NEXT.md} #53), so
     * in practice the status carried the whole decision - and a construction whose builder is
     * still WALKING has {@code NOT_STARTED} by definition, because the status advances when the
     * worker starts building, not when it is dispatched. A worker sent across the map therefore
     * looked exactly like a worker that gave up:
     * </p>
     *
     * <pre>
     *   Cancel construction: Pylon       / Pylon is overdue                / buildable:true
     *   Cancel construction: Gateway     / Last resort - cancelling ALL not started
     *   Cancel construction: Nexus       / Last resort - cancelling ALL not started
     * </pre>
     *
     * <p>
     * <b>The fix is to ask whether anyone is still working on it</b>, which is what "overdue"
     * actually means. An assigned, living builder that has been getting closer is progress even
     * while the status says NOT_STARTED; nothing assigned is not.
     * </p>
     */
    public boolean isOverdue() {
        if (hasStarted()) return false;
        // PREPARING waits by design (site picked early, no builder yet).
        if (status == ConstructionOrderStatus.PREPARING) return false;
        if (buildingType != null && buildingType.isBase()) return false;
        if (!A.canAfford(buildingType)) return false;

        // A builder is on its way, or already here and about to start: give it the time.
        // Without this a long walk across the map is indistinguishable from a stall, and the
        // reaction to a stall is to cancel everything that has not started.
        // A builder stolen back to mining is NOT working on it (game 175707:
        // #117 gathered 4000+ frames while its Gateway sat NOT_STARTED with
        // no cancel and no finish). Walking/fighting builders still shield.
        if (builder != null && builder.isAlive()
            && !builder.isGatheringMinerals() && !builder.isGatheringGas()) {
            return false;
        }

//        int secondsSinceStarted = A.ago(timeOrdered) / 30;
//        if (secondsSinceStarted >= 12) {
//            ErrorLog.printMaxOncePerMinute("Overdueing (" + secondsSinceStarted + "s), minerals: " + A.minerals() + " / " + this);
//            CenterCamera.on(positionToBuild);
//        }

        return A.ago(timeOrdered) > 30 * 22;
    }

    public int startedSecondsAgo() {
        return A.ago(timeOrdered) / 30;
    }

    public int progressPercent() {
        if (buildingUnit == null) return 0;
        return (int) buildingUnit.hpPercent();
    }

    public void releaseReservedResources() {
        ProductionOrder order = productionOrder();
        if (order != null) order.releasedReservedResources();
    }

    public boolean hasValidBuilderAndHeIsConstructing() {
        return builder != null && builder.hp() >= 1 && builder.isConstructing();
    }
}
