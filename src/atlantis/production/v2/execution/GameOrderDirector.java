package atlantis.production.v2.execution;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UpgradeProducible;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;
import bwapi.UpgradeType;

/**
 * The live {@link OrderDirector}: the only place production-v2 touches the game.
 *
 * <p>
 * It exists because the redesign must be adopted incrementally (strangler):
 * buildings still go through the legacy {@link Construction} pipeline, whose
 * {@code builders/BuilderManager} already knows how to walk a builder to a
 * tile, retry a blocked position, and abandon a site under attack.
 * Production-v2 owns the <em>decision</em> (what, when, where) and hands the
 * <em>execution</em> to that pipeline. When the legacy pipeline is finally
 * deleted, this class is replaced by a self-contained builder and nothing above
 * it changes - which is the point of the interface.
 * </p>
 *
 * <p>
 * Idempotence is the contract that makes this safe: {@link #buildAt} is called
 * on the same tile every frame while the construction is pending, so it must
 * create the request exactly once and return the existing one thereafter.
 * </p>
 */
public class GameOrderDirector implements OrderDirector {

    @Override
    public boolean trainFacility(String typeId, Producible item) {
        AUnitType producerType = resolveUnitType(typeId);
        AUnitType unitType = resolveUnitType(item.id());
        if (producerType == null || unitType == null) return false;

        AUnit producer = Select.ourFree(producerType).first();
        if (producer == null) return false;

        // Must not queue the same thing twice: the plan is recomputed every
        // frame and the unit is still in the build queue on the next one.
        if (producer.isTraining(unitType)) return true;

        return producer.trainForced(unitType);
    }

    @Override
    public boolean buildAt(Producible building, PlacementReservation placement) {
        AUnitType buildingType = resolveUnitType(building.id());
        if (buildingType == null || !placement.isSuccessful()) return false;

        // Idempotence: the same tile is offered every frame; only the first
        // offer creates a request.
        for (Construction construction : ConstructionRequests.constructions) {
            if (construction.buildingType() == buildingType
                    && construction.buildPosition() != null
                    && construction.buildPosition().tx() == placement.tileX()
                    && construction.buildPosition().ty() == placement.tileY()) {
                return true;
            }
        }

        Construction construction = new Construction(buildingType);
        construction.setPositionToBuild(APosition.create(placement.tileX(), placement.tileY()));
        construction.setNearTo(homePosition());
        construction.setMaxDistance(12);
        construction.assignOptimalBuilder();

        ConstructionRequests.constructions.add(construction);
        return true;
    }

    @Override
    public boolean researchOrUpgrade(String typeId, Producible item) {
        AUnitType facilityType = resolveUnitType(typeId);
        if (facilityType == null) return false;

        // Same guard as train: the engine throws (rather than returning false)
        // when a building is asked for work it cannot take, and a facility that
        // is still being built has no research queue at all.
        AUnit facility = Select.ourFree(facilityType).first();
        if (facility == null) return false;
        if (!facility.isCompleted()) return false;

        if (item instanceof TechProducible) {
            TechType tech = ((TechProducible) item).tech();
            if (facility.isResearching()) return true;

            return facility.research(tech);
        }

        if (item instanceof UpgradeProducible) {
            UpgradeType upgrade = ((UpgradeProducible) item).upgrade();
            if (facility.isUpgrading()) return true;

            return facility.upgrade(upgrade);
        }

        return false;
    }

    private HasPosition homePosition() {
        AUnit base = Select.ourBases().first();
        return base != null ? base : Select.ourBuildings().first();
    }

    private AUnitType resolveUnitType(String id) {
        if (id == null || id.isEmpty()) return null;
        return AUnitType.getByName(id);
    }
}
