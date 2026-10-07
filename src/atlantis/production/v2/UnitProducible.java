package atlantis.production.v2;

import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Producible} implementation over a real {@link AUnitType}.
 *
 * <p>
 * Cost, build time and prerequisites come from the unit type itself (the
 * engine is the source of truth per CONVENTIONS §9). The producer type id is
 * the {@code AUnitType.produces()} unit id, resolved by the facility registry
 * later - keeping this class free of {@code Select}/{@code Count} calls means
 * the scheduler can run on plain unit-type data in tests.
 * </p>
 */
public final class UnitProducible implements Producible {

    private final AUnitType type;

    public UnitProducible(AUnitType type) {
        this.type = type;
    }

    public static UnitProducible of(AUnitType type) {
        return new UnitProducible(type);
    }

    public AUnitType type() {
        return type;
    }

    @Override
    public String id() {
        return type.name();
    }

    @Override
    public ResourceCost cost() {
        int supply = Math.max(0, type.supplyNeeded());
        return ResourceCost.of(type.mineralPrice(), type.gasPrice(), supply);
    }

    @Override
    public int buildDurationFrames() {
        return type.totalTrainTime();
    }

    @Override
    public List<Producible> immediatePrerequisites() {
        // The engine's whatIsRequired is exactly the missing dependency:
        // Dragoon -> Cybernetics Core, Photon Cannon -> Forge, null when none.
        AUnitType required = type.whatIsRequired();
        List<Producible> result = new ArrayList<>();
        if (required != null) {
            result.add(UnitProducible.of(required));
        }
        return result;
    }

    @Override
    public String producerTypeId() {
        AUnitType producer = type.whatBuildsIt();
        return producer != null ? producer.name() : "Unknown";
    }

    @Override
    public boolean requiresPlacement() {
        return type.isABuilding();
    }
}
