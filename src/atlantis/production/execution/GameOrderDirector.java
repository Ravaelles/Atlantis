package atlantis.production.execution;
import atlantis.production.placement.PlacementReservation;
import atlantis.production.producibles.Producible;
import atlantis.production.scheduling.ProductionItem;
import atlantis.production.producibles.TechProducible;
import atlantis.production.producibles.UpgradeProducible;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.demand.*;
import atlantis.production.placement.*;
import atlantis.production.producibles.*;
import atlantis.production.scheduling.*;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.HashMap;
import java.util.Map;

/**
 * The live {@link OrderDirector}: the only place production-v2 touches the game.
 *
 * <p>
 * It exists because the redesign is adopted incrementally (strangler): buildings
 * still go through the legacy {@link Construction} pipeline, whose builder
 * manager already walks a worker to a tile, retries a blocked position and
 * abandons a site under attack. Production-v2 owns the <em>decision</em> (what,
 * when, where) and hands the <em>execution</em> to that pipeline. When the legacy
 * pipeline is deleted, this class is replaced by a self-contained builder and
 * nothing above it changes.
 * </p>
 *
 * <p>
 * Two contracts make it safe: {@link #buildAt} is idempotent (the same tile is
 * re-offered every frame while the construction is pending), and one facility
 * never receives two commands in one frame - the scheduler plans a whole
 * horizon, so two items of the same facility can be due at once and the second
 * waits for the next frame instead of being queued blindly.
 * </p>
 */
public class GameOrderDirector implements OrderDirector {

    private final Map<Integer, Integer> commandsThisFrame = new HashMap<>();

    public void startFrame() {
        commandsThisFrame.clear();
    }

    @Override
    public String trainFacility(String typeId, int producerId, Producible item) {
        AUnitType unitType = resolveUnitType(item.id());
        if (unitType == null) return "unknown unit";

        AUnit producer = resolveFacility(typeId, producerId);
        if (producer == null) return "no such facility";
        if (!producer.isCompleted()) return "facility unfinished #" + producer.id();
        if (commandsThisFrame.getOrDefault(producer.id(), 0) > 0) return "already commanded #" + producer.id();

        // The plan is recomputed every frame and the unit is still in the build
        // queue on the next one: the same order must not be queued twice. A
        // dedup hit issues no command, so it reports false - the interface
        // promises true only "when a command was actually issued", and every
        // "trained" row the dispatcher logs inflates the order count the
        // next diagnosis reads (measured: 20 V2 train rows for +7 net
        // Probes, most of them this hit). The facility stays marked used:
        // a second same-frame item must still wait instead of appending
        // behind the queue head.
        if (producer.isTraining(unitType)) {
            markUsed(producer);

            return "already training " + unitType.name() + " #" + producer.id();
        }

        if (!producer.trainForced(unitType)) return trainRefused(producer, unitType);

        markUsed(producer);

        return null;
    }

    /**
     * The facility snapshot that tells busy from stuck: idle flag, whether
     * anything trains, queue depth and head. Read-only, never decides.
     */
    /**
     * Names the engine refusal at the door that is actually closed: no
     * free supply first (the engine refuses any train while capped, and
     * without this the refusal reads as a facility problem), then the
     * facility snapshot that tells busy from stuck, then the live bank.
     */
    private static String trainRefused(AUnit producer, AUnitType unitType) {
        if (atlantis.game.A.supplyFree() < Math.max(0, (unitType.ut().supplyRequired() + 1) / 2)) {
            return "supply blocked";
        }

        return "engine refused #" + producer.id() + facilityState(producer)
            + " bank=" + atlantis.game.A.minerals() + "/" + atlantis.game.A.gas();
    }

    private static String facilityState(AUnit producer) {
        try {
            String head = "?";
            java.util.List<AUnitType> queue = producer.trainingQueue();
            if (queue != null && !queue.isEmpty() && queue.get(0) != null) {
                head = queue.get(0).name();
            }

            return " idle=" + producer.isIdle()
                + " training=" + producer.isTrainingAnyUnit()
                + " queue=" + (queue == null ? -1 : queue.size())
                + " head=" + head
                + " remaining=" + producer.remainingTrainTime();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    @Override
    public boolean buildAt(Producible building, PlacementReservation placement, int minSupply) {
        AUnitType buildingType = resolveUnitType(building.id());
        if (buildingType == null || placement == null || !placement.isSuccessful()) return false;

        for (Construction construction : ConstructionRequests.constructions) {
            if (construction.buildingType() == buildingType
                && construction.buildPosition() != null
                && construction.buildPosition().tx() == placement.tileX()
                && construction.buildPosition().ty() == placement.tileY()) {
                return true;
            }
        }

        // Cross-order footprint overlap (owner 2026-11-08): same-tile
        // dedupe misses different tiles with shared footprint columns
        // (measured live: Gateway [92,103] minted on Pylon row [93,103],
        // 110 s of refused Builds on a banked supply block). Refuse, the
        // demand retries elsewhere.
        if (ConstructionRequests.overlapsInFlightRow(
            APosition.create(placement.tileX(), placement.tileY()),
            buildingType.getTilesWidth(), buildingType.getTilesHeights())) {
            twinRefused(buildingType, "overlap-row");

            return false;
        }

        // Single-demand tech mints once: a second concurrent construction
        // of a tech building is always a duplicate (one Core per tech
        // path), never legit concurrency like Pylons or Gateways. The
        // queue-side wall (OnlyOneAllowedOfType) cannot see V2 mints, so
        // the mint door owns it. A stuck row still heals: overdue cancels
        // remove it and the next frame mints again.
        if (isSingleDemandTech(buildingType) && hasLiveTechDemandCovered(buildingType)) {
            return true;
        }

        // One Pylon in flight below 40 supply (two above): the legacy
        // concurrency cap, consulted through the single implementation
        // instead of a second idea of "too many". Without it the same
        // supply block mints overlapping Pylons every pass (measured:
        // three concurrent warps out of one block at 25 supply), since
        // carriedOver only re-offers not-started tiles and a walking
        // builder's tile looks free again.
        if (buildingType.isPylon()
            && atlantis.production.requests.produce.ProduceBuilding.tooManyPylonsInFlight()) {
            return true;
        }

        // Below 40 supply never more than 8 free (owner rule, ex legacy
        // PylonOverstockGate): a Pylon mint that would push free past
        // that waits - the demand retries next frame. Same guard name
        // in the twin, so the logs read one doctrine across the door
        // migration.
        if (buildingType.isPylon()
            && atlantis.game.A.supplyUsed() < 40 && atlantis.game.A.supplyFree() > 8) {
            twinRefused(buildingType, "pylon-overstock");

            return false;
        }

        // Below minSupply only price+50 builds (owner rule, ex legacy
        // BelowMinSupplyGate): a poor early mint waits for the bank,
        // a funded one still aims. Same guard name in the twin.
        if (atlantis.game.A.supplyUsed() < minSupply
            && !atlantis.game.A.canAfford(
            buildingType.mineralPrice() + 50, buildingType.gasPrice())) {
            twinRefused(buildingType, "below-min-supply-and-poor");

            return false;
        }

        // Power before workers (owner rule, ex legacy PowerNotFundedGate):
        // sending a builder for a power-needing building while no Pylon
        // stands spends its price and then holds the worker on unpowered
        // ground while the bank refills. Wait until the bank covers both.
        if (atlantis.production.requests.produce.ProduceBuilding.powerProviderNotFunded(buildingType)) {
            twinRefused(buildingType, "power-not-funded");

            return false;
        }

        // A waiting Core outranks Pylon spending while contended (owner
        // rule, ex legacy CorePriorityGate): the 100 minerals stay saved
        // for the Core instead of going into a forward Pylon. Started
        // (warping) Cores and rich banks never block. Only when the Core
        // is actually affordable: yielding while the bank cannot cover
        // the Core parks the Pylon behind a demand that cannot move
        // either (measured sim: 40 refusals into a supply stall - the
        // legacy door never saw this because its queue rows converted;
        // V2 mints the Core itself, so an unaffordable Core must not
        // veto the unblocker). Same guard name.
        if (buildingType.isPylon()
            && atlantis.production.orders.production.queue.PriorityWaiters.pylonYieldsToCore()
            && atlantis.game.A.canAfford(AUnitType.Protoss_Cybernetics_Core.mineralPrice(),
            AUnitType.Protoss_Cybernetics_Core.gasPrice())) {
            twinRefused(buildingType, "core-priority");

            return false;
        }

        // Never mint what the bank does not cover (ex legacy wallet
        // funding): the scheduler plans affordable frames, but the bank
        // moves under it. Dispatch checks reality; the demand retries.
        if (!atlantis.game.A.canAfford(buildingType.mineralPrice(), buildingType.gasPrice())) {
            twinRefused(buildingType, "unaffordable");

            return false;
        }

        // Earlier rows reserve first (ex legacy EarlierReserveGate): an
        // earlier unfinished building still needs its price, so a later
        // mint must cover both (warping rows paid at warp start and never
        // count). V2 has no queue sequence - every other unpaid row
        // counts. Same guard name in the twin.
        if (!atlantis.game.A.canAfford(
            buildingType.mineralPrice() + unpaidUnfinishedMinerals(),
            buildingType.gasPrice() + unpaidUnfinishedGas())) {
            twinRefused(buildingType, "earlier-reserve");

            return false;
        }

        Construction construction = createConstruction(buildingType, placement, building, minSupply);
        if (construction == null) return false;

        ConstructionRequests.addConstruction(construction);

        return true;
    }

    @Override
    public boolean researchOrUpgrade(String typeId, int producerId, Producible item) {
        AUnit facility = resolveFacility(typeId, producerId);
        if (facility == null || !facility.isCompleted()) return false;
        if (commandsThisFrame.getOrDefault(facility.id(), 0) > 0) return false;

        if (item instanceof TechProducible) {
            TechType tech = ((TechProducible) item).tech();
            // Only our own job counts as success: an unrelated active research
            // must not make this goal look satisfied. A hit issues no command,
            // so it reports false like the train twin - "researched" rows
            // must measure accepts, not dispatches. The facility stays marked
            // used: nothing else may take its frame either.
            if (facility.isResearching()) {
                markUsed(facility);

                return false;
            }
            if (!facility.research(tech)) return false;

            markUsed(facility);

            return true;
        }

        if (item instanceof UpgradeProducible) {
            UpgradeType upgrade = ((UpgradeProducible) item).upgrade();
            if (facility.isUpgrading()) {
                markUsed(facility);

                return false;
            }
            if (!facility.upgrade(upgrade)) return false;

            markUsed(facility);

            return true;
        }

        return false;
    }

    // =========================================================

    private void markUsed(AUnit facility) {
        commandsThisFrame.merge(facility.id(), 1, Integer::sum);
    }

    /**
     * The facility the plan named, or the first free one of the type when the
     * plan could not name it (a building that is only starting now).
     */
    private AUnit resolveFacility(String typeId, int producerId) {
        AUnitType type = resolveUnitType(typeId);
        if (type == null) return null;

        if (producerId != ProductionItem.NO_PRODUCER) {
            for (AUnit unit : Select.ourWithUnfinished().ofType(type).list()) {
                if (unit.id() == producerId) return unit;
            }
        }
        return Select.ourFree(type).first();
    }

    /**
     * The engine throws (rather than returning false) when a building is asked
     * for work it cannot take, so every path checks the facility first.
     */

    /**
     * Tech buildings wanted at most once at a time (mirrors the queue-side
     * {@code OnlyOneAllowedOfType} list, minus Robotics: a second Robo while
     * the first warps is rare enough that waiting for the warp beats
     * risking a duplicate pair, and the wait is bounded by completion).
     * Pylons, Gateways, Cannons, bases and gas never take this exit -
     * legit concurrency (two Gateways, Pylon pairs) must keep minting.
     */
    private static boolean isSingleDemandTech(AUnitType type) {
        if (type.isPylon() || type.isGateway() || type.isCannon()
            || type.isBase() || type.isGasBuilding()) return false;

        return type.equals(AUnitType.Protoss_Forge)
            || type.equals(AUnitType.Protoss_Cybernetics_Core)
            || type.equals(AUnitType.Protoss_Citadel_of_Adun)
            || type.equals(AUnitType.Protoss_Observatory)
            || type.equals(AUnitType.Protoss_Robotics_Support_Bay)
            || type.equals(AUnitType.Protoss_Fleet_Beacon)
            || type.equals(AUnitType.Protoss_Templar_Archives)
            || type.equals(AUnitType.Terran_Engineering_Bay)
            || type.equals(AUnitType.Terran_Armory);
    }

    /**
     * True when this tech already has a live row or a standing/warping
     * building: a second mint now would be the duplicate, not the demand.
     */
    private static boolean hasLiveTechDemandCovered(AUnitType type) {
        if (Select.countOurOfTypeWithUnfinished(type) > 0) return true;

        return !ConstructionRequests.notFinishedOfType(type).isEmpty();
    }

    /**
     * Creates the legacy construction request and attaches an order to it: the
     * position finders read {@code productionOrder().getModifier()}, and a
     * construction without one threw a null pointer once its tile was blocked or
     * it had to be cancelled.
     */
    private Construction createConstruction(AUnitType buildingType, PlacementReservation placement,
                                            Producible building, int minSupply) {
        Construction construction = new Construction(buildingType);
        construction.setPositionToBuild(APosition.create(placement.tileX(), placement.tileY()));
        construction.setNearTo(homePosition());
        // No maxDistance stamp (owner order 2026-11-07): bounding every
        // V2 row's funnel re-site to home r=12 trapped mid-game demand
        // in a full home area (measured live: 100+ candidates skipped
        // out-of-radius every pass, Gateway/Nexus no-placement loops)
        // and could never reach a third base. The ranker already
        // condenses (same-area first, availability first) - the bound
        // only made sense for the legacy ring search. Sited callers
        // keep their explicit bounds (siteForSited); this door stays
        // unbounded and ranked.
        construction.setProductionOrder(orderFor(buildingType, minSupply));
        construction.setDemand(atlantis.production.demand.Demand.of(buildingType));
        construction.assignOptimalBuilder();

        // No builder, no mint (owner order 2026-11-04): dispatching into
        // an overrun with no free builders minted-and-cancelled the same
        // Pylon tile 170 times in 7 seconds live (no builder ever
        // assigned, "looks bugged" cancelling the order, de-dup guards
        // blind because each twin died instantly). The demand survives
        // the refusal and re-dispatches when builders exist. Mirrors the
        // legacy mint door, which refuses on a null random builder.
        if (construction.builder() == null) {
            twinNoBuilder(buildingType);

            return null;
        }

        // The legacy issue path twins every construction it mints; V2-direct
        // orders bypassed it, so a warp-less construction was invisible -
        // 44 issued Nexus dispatches with zero twin rows anywhere (live).
        // Same row, same contract (once per construction, builder may be null).
        atlantis.production.requests.produce.ProduceBuilding.noteStarted(
            construction, construction.builder(), construction.buildPosition(),
            construction.productionOrder());

        return construction;
    }

    /**
     * Test seam: the refusal throttle is per-JVM state, like every twin
     * gate with a heartbeat.
     */
    public static void clearForTests() {
        lastNoBuilderTwin.clear();
        lastRefusedTwin.clear();
    }

    /**
     * A refused mint names its guard (same names as the legacy door, one
     * doctrine across the migration), throttled like the no-builder twin
     * so a waiting demand does not shout every frame.
     */
    private static void twinRefused(AUnitType buildingType, String guard) {
        try {
            String id = (buildingType == null ? "-" : buildingType.name()) + ":" + guard;
            Integer last = lastRefusedTwin.get(id);
            int now = atlantis.game.A.now();
            if (last != null && now - last < 600) return;
            lastRefusedTwin.put(id, now);

            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.BUILD_REFUSED)
                .param("type", buildingType == null ? "-" : buildingType.name())
                .param("guard", guard)
                .param("supplyUsed", atlantis.game.A.supplyUsed())
                .param("minerals", atlantis.game.A.minerals())
                .log(now, atlantis.game.A.minSec());
        } catch (Exception | Error ignored) {
        }
    }

    private static final java.util.Map<String, Integer> lastRefusedTwin = new java.util.HashMap<>();

    private static final java.util.Map<String, Integer> lastNoBuilderTwin = new java.util.HashMap<>();

    /**
     * Unpaid price of every other unfinished row (minerals): a warping
     * row paid at warp start (its building unit lives), so only rows
     * with no live building unit still need money. The mint door's
     * earlier-reserve math - no queue sequence, every row counts.
     */
    private static int unpaidUnfinishedMinerals() {
        int sum = 0;
        for (atlantis.production.constructions.Construction other :
            atlantis.production.constructions.ConstructionRequests.notFinished()) {
            if (other == null || other.buildingType() == null) continue;
            atlantis.units.AUnit building = other.buildingUnit();
            if (building != null && building.isAlive()) continue;
            sum += other.buildingType().mineralPrice();
        }

        return sum;
    }

    private static int unpaidUnfinishedGas() {
        int sum = 0;
        for (atlantis.production.constructions.Construction other :
            atlantis.production.constructions.ConstructionRequests.notFinished()) {
            if (other == null || other.buildingType() == null) continue;
            atlantis.units.AUnit building = other.buildingUnit();
            if (building != null && building.isAlive()) continue;
            sum += other.buildingType().gasPrice();
        }

        return sum;
    }

    private static void twinNoBuilder(AUnitType buildingType) {
        try {
            String id = buildingType == null ? "-" : buildingType.name();
            Integer last = lastNoBuilderTwin.get(id);
            int now = atlantis.game.A.now();
            if (last != null && now - last < 600) return;
            lastNoBuilderTwin.put(id, now);

            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.BUILD_REFUSED)
                .param("type", id)
                .param("guard", "no-builder")
                .param("bank", atlantis.game.A.minerals())
                .log(now, atlantis.game.A.minSec());
        } catch (Exception | Error ignored) {
        }
    }

    /**
     * A synthetic order carrying the plan's intent. It is never enqueued in the
     * legacy {@code Queue} - only the construction reads it - so the v2 path
     * stays the only policy.
     */
    private ProductionOrder orderFor(AUnitType type, int minSupply) {
        ProductionOrder order = new ProductionOrder(type, minSupply);
        order.markAsNotDynamic();

        return order;
    }

    private HasPosition homePosition() {
        AUnit base = Select.ourBases().first();
        HasPosition fallback = base != null ? base : Select.ourBuildings().first();
        if (fallback == null) ErrorLog.printMaxOncePerMinute("Production v2: no base for a build position");

        return fallback;
    }

    private AUnitType resolveUnitType(String id) {
        if (id == null || id.isEmpty()) return null;

        return AUnitType.getByName(id);
    }
}
