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
    /** 1-based level this recipe researches. */
    private final int level;

    public UpgradeProducible(UpgradeType upgrade) {
        this(upgrade, 1);
    }

    public UpgradeProducible(UpgradeType upgrade, int level) {
        this.upgrade = upgrade;
        this.level = Math.max(1, level);
    }

    public static UpgradeProducible of(UpgradeType upgrade) {
        return new UpgradeProducible(upgrade);
    }

    public static UpgradeProducible of(UpgradeType upgrade, int level) {
        return new UpgradeProducible(upgrade, level);
    }

    public UpgradeType upgrade() {
        return upgrade;
    }

    public int level() {
        return level;
    }

    @Override
    public String id() {
        return level == 1 ? upgrade.toString() : upgrade + " L" + level;
    }

    @Override
    public ResourceCost cost() {
        return ResourceCost.of(upgrade.mineralPrice(level), upgrade.gasPrice(level), 0);
    }

    @Override
    public int buildDurationFrames() {
        return upgrade.upgradeTime(level);
    }

    /** The facility, plus the level's own requirement (+2 weapons: Templar Archives). */
    @Override
    public List<Producible> immediatePrerequisites() {
        List<Producible> result = new ArrayList<>();
        AUnitType facility = facility();
        if (facility != null) result.add(UnitProducible.of(facility));

        AUnitType required = AUnitType.from(upgrade.whatsRequired(level));
        if (required != null && !required.equals(facility))
            result.add(UnitProducible.of(required));

        if (level > 1) result.add(of(upgrade, level - 1));
        return result;
    }

    @Override
    public String producerTypeId() {
        AUnitType facility = facility();
        return facility != null ? facility.name() : "Unknown";
    }

    private AUnitType facility() {
        return upgrade.whatUpgrades() == null ? null : AUnitType.from(upgrade.whatUpgrades());
    }

    @Override
    public boolean requiresPlacement() {
        return false;
    }

    @Override
    public boolean becomesFacility() {
        return false;
    }

    @Override
    public Producible nthOccurrence(int n) {
        return of(upgrade, n);
    }

    /** How many levels this upgrade has - the ceiling a goal can ask for. */
    public int levels() {
        return upgrade.maxRepeats();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UpgradeProducible && ((UpgradeProducible) o).upgrade == upgrade
                && ((UpgradeProducible) o).level == level;
    }

    @Override
    public int hashCode() {
        return upgrade.hashCode() * 31 + level;
    }

    @Override
    public String toString() {
        return "Upgrade{" + id() + "}";
    }
}
