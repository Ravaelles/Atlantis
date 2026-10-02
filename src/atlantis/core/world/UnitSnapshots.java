package atlantis.core.world;

import atlantis.units.AUnit;

/**
 * Stage E: bridges the legacy {@code AUnit} entity to {@link UnitSnapshot}.
 * Transitional — dies when the {@code World} registry owns lifecycle.
 */
final class UnitSnapshots {

    private UnitSnapshots() {
    }

    static UnitSnapshot of(AUnit unit) {
        return new UnitSnapshot(
            unit.id(),
            unit.type(),
            unit.position(),
            unit.hp(),
            unit.shields()
        );
    }
}
