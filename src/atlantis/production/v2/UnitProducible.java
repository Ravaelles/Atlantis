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
        return ResourceCost.of(type.mineralPrice(), type.gasPrice(), halvedSupply(type.ut().supplyRequired()));
    }

    @Override
    public int buildDurationFrames() {
        return type.totalTrainTime();
    }

    /**
     * Every required unit except the producer's own consumables (worker,
     * larva) - Dragoon needs Gateway AND Cybernetics Core. A building that
     * needs psi also needs a Pylon.
     */
    @Override
    public List<Producible> immediatePrerequisites() {
        List<Producible> result = new ArrayList<>();
        for (AUnitType required : type.requiredUnits().keys()) {
            if (required == null || required.isWorker() || required.isLarva()) continue;
            result.add(UnitProducible.of(required));
        }
        if (type.ut().requiresPsi()) {
            UnitProducible pylon = UnitProducible.of(AUnitType.Protoss_Pylon);
            if (!containsId(result, pylon.id())) result.add(pylon);
        }
        return result;
    }

    @Override
    public String producerTypeId() {
        AUnitType producer = type.whatBuildsIt();
        return producer != null ? producer.name() : "Unknown";
    }

    /** Only a building a worker builds needs a tile; addons and morphs do not. */
    @Override
    public boolean requiresPlacement() {
        if (!type.isABuilding()) return false;
        AUnitType producer = type.whatBuildsIt();
        return producer != null && producer.isWorker();
    }

    @Override
    public boolean becomesFacility() {
        return type.isABuilding();
    }

    @Override
    public boolean consumesProducer() {
        AUnitType producer = type.whatBuildsIt();
        return producer != null && producer.isLarva();
    }

    @Override
    public int supplyProvided() {
        return halvedSupply(type.ut().supplyProvided());
    }

    /** BWAPI counts supply doubled (Zergling = 1); A.supplyUsed() halves it. */
    static int halvedSupply(int raw) {
        return Math.max(0, (raw + 1) / 2);
    }

    private static boolean containsId(List<Producible> list, String id) {
        for (Producible p : list) if (p.id().equals(id)) return true;
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UnitProducible && ((UnitProducible) o).type.equals(type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return id();
    }
}
