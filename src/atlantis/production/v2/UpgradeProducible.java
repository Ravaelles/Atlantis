package atlantis.production.v2;

import atlantis.units.AUnitType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Producible} over an upgrade (Zealot Leg Enhancements, +1 Weapons).
 *
 * <p>
 * The upgrade half of the recipe interface described in 01_PRODUCTION.md §2
 * smell 2. An upgrade has a level count rather than a single instance, which is
 * why {@link #levels()} is exposed: the scheduler schedules one level per goal
 * and the goal generator decides how many it wants, instead of the recipe
 * quietly expanding into an unknown number of productions.
 * </p>
 *
 * <p>
 * Pure domain, like its tech sibling: numbers come from {@link UpgradeType},
 * which is the engine's own answer (CONVENTIONS §9).
 * </p>
 */
public final class UpgradeProducible implements Producible {

    private final UpgradeType upgrade;

    public UpgradeProducible(UpgradeType upgrade) {
        this.upgrade = upgrade;
    }

    public static UpgradeProducible of(UpgradeType upgrade) {
        return new UpgradeProducible(upgrade);
    }

    public UpgradeType upgrade() {
        return upgrade;
    }

    @Override
    public String id() {
        return upgrade.toString();
    }

    @Override
    public ResourceCost cost() {
        // One level: the goal decides how many levels it asks for.
        return ResourceCost.of(upgrade.mineralPrice(), upgrade.gasPrice(), 0);
    }

    @Override
    public int buildDurationFrames() {
        return upgrade.upgradeTime();
    }

    @Override
    public List<Producible> immediatePrerequisites() {
        List<Producible> result = new ArrayList<>();
        if (upgrade.whatUpgrades() == null)
            return result;

        AUnitType facility = AUnitType.from(upgrade.whatUpgrades());
        if (facility != null)
            result.add(UnitProducible.of(facility));

        return result;
    }

    @Override
    public String producerTypeId() {
        if (upgrade.whatUpgrades() == null)
            return "Unknown";

        AUnitType facility = AUnitType.from(upgrade.whatUpgrades());
        return facility != null ? facility.name() : "Unknown";
    }

    @Override
    public boolean requiresPlacement() {
        return false;
    }

    /** How many levels this upgrade has - the ceiling a goal can ask for. */
    public int levels() {
        return upgrade.maxRepeats();
    }

    @Override
    public String toString() {
        return "Upgrade{" + id() + "}";
    }
}
