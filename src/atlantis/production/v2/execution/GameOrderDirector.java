package atlantis.production.v2.execution;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.UpgradeProducible;
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
    public boolean trainFacility(String typeId, int producerId, Producible item) {
        AUnitType unitType = resolveUnitType(item.id());
        if (unitType == null) return false;

        AUnit producer = resolveFacility(typeId, producerId);
        if (producer == null || !producer.isCompleted()) return false;
        if (commandsThisFrame.getOrDefault(producer.id(), 0) > 0) return false;

        // The plan is recomputed every frame and the unit is still in the build
        // queue on the next one: the same order must not be queued twice.
        if (producer.isTraining(unitType)) {
            markUsed(producer);
            return true;
        }

        if (!producer.trainForced(unitType)) return false;
        markUsed(producer);
        return true;
    }

    @Override
    public boolean buildAt(Producible building, PlacementReservation placement) {
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

        Construction construction = createConstruction(buildingType, placement, building);
        if (construction == null) return false;

        ConstructionRequests.constructions.add(construction);
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
            // must not make this goal look satisfied.
            if (facility.isResearching()) return tech.equals(facility.whatIsResearching());
            if (!facility.research(tech)) return false;
            markUsed(facility);
            return true;
        }

        if (item instanceof UpgradeProducible) {
            UpgradeType upgrade = ((UpgradeProducible) item).upgrade();
            if (facility.isUpgrading()) return upgrade.equals(facility.whatIsUpgrading());
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
     * Creates the legacy construction request and attaches an order to it: the
     * position finders read {@code productionOrder().getModifier()}, and a
     * construction without one threw a null pointer once its tile was blocked or
     * it had to be cancelled.
     */
    private Construction createConstruction(AUnitType buildingType, PlacementReservation placement,
            Producible building) {
        Construction construction = new Construction(buildingType);
        construction.setPositionToBuild(APosition.create(placement.tileX(), placement.tileY()));
        construction.setNearTo(homePosition());
        construction.setMaxDistance(12);
        construction.setProductionOrder(orderFor(buildingType));
        construction.assignOptimalBuilder();
        return construction;
    }

    /**
     * A synthetic order carrying the plan's intent. It is never enqueued in the
     * legacy {@code Queue} - only the construction reads it - so the v2 path
     * stays the only policy.
     */
    private ProductionOrder orderFor(AUnitType type) {
        ProductionOrder order = new ProductionOrder(type, 0);
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
