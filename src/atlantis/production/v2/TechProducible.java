package atlantis.production.v2;

import atlantis.units.AUnitType;
import bwapi.TechType;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Producible} over a researchable tech (a Dragoon's Singularity
 * Charge,
 * a Zealot's Leg Enhancements).
 *
 * <p>
 * 01_PRODUCTION.md §2 smell 2 lists tech as one of the three recipes that must
 * go through the same interface as units and buildings, so the scheduler never
 * branches on the kind of thing it is planning. This is the tech half of that:
 * it answers cost, duration, prerequisites and facility from {@link TechType}
 * itself, which is the authoritative source (CONVENTIONS §9).
 * </p>
 *
 * <p>
 * Pure domain: it holds a {@code TechType} and reads numbers off it, but the
 * scheduler only ever sees a {@link Producible}, so the timeline and the
 * scheduling math stay testable without a game.
 * </p>
 */
public final class TechProducible implements Producible {

    private final TechType tech;

    public TechProducible(TechType tech) {
        this.tech = tech;
    }

    public static TechProducible of(TechType tech) {
        return new TechProducible(tech);
    }

    public TechType tech() {
        return tech;
    }

    @Override
    public String id() {
        return tech.toString();
    }

    @Override
    public ResourceCost cost() {
        // Tech costs no supply: it is researched, not fielded.
        return ResourceCost.of(tech.mineralPrice(), tech.gasPrice(), 0);
    }

    @Override
    public int buildDurationFrames() {
        return tech.researchTime();
    }

    /**
     * The building that researches this, if it is not already up. Empty when the
     * tech resolves no facility - the scheduler then treats it as unproducible
     * here rather than planning a building for it.
     */
    @Override
    public List<Producible> immediatePrerequisites() {
        List<Producible> result = new ArrayList<>();
        if (tech.whatResearches() == null)
            return result;

        AUnitType facility = AUnitType.from(tech.whatResearches());
        if (facility != null)
            result.add(UnitProducible.of(facility));

        return result;
    }

    /**
     * The facility that researches it - the same building the prerequisites
     * check, because a tech has no other producer.
     */
    @Override
    public String producerTypeId() {
        if (tech.whatResearches() == null)
            return "Unknown";

        AUnitType facility = AUnitType.from(tech.whatResearches());
        return facility != null ? facility.name() : "Unknown";
    }

    @Override
    public boolean requiresPlacement() {
        return false;
    }

    @Override
    public String toString() {
        return "Tech{" + id() + "}";
    }
}
