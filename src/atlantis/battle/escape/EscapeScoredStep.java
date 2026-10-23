package atlantis.battle.escape;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * One scored escape step for live callers (NEXT #172): the 8-direction
 * scored pick with the live context gathered behind guarded reads,
 * MoveAway stays the caller's fallback. The gathering lives here once
 * so every consumer scores the same world (minerals/geysers/buildings
 * as blockers, combat enemies as pressure); the no-arg decision reads
 * fail open (null walkability, empty lists) instead of refusing.
 */
public final class EscapeScoredStep {

    private EscapeScoredStep() {
    }

    /**
     * Scored destination for a step of this many tiles, or null when nothing scores.
     * Detector-first (NEXT #173): open ground behaves exactly like the plain
     * pick, walls deflect at full length instead of shuffling short.
     */
    public static APosition pickScored(AUnit unit, AUnit threat, int stepTiles) {
        if (unit == null || threat == null || stepTiles <= 0) return null;

        try {
            return EscapeDestination.pickEscape(unit, threat, stepTiles, walkability(), blockers(unit), foes(unit));
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    static EscapeRays.Walkability walkability() {
        return (x, y) -> {
            try {
                return APosition.create(x, y).isWalkable();
            } catch (Exception ignored) {
                return false;
            }
        };
    }

    public static List<HasPosition> blockers(AUnit unit) {
        List<HasPosition> blockers = new ArrayList<>();
        try {
            Selection minerals = Select.mineralsAndGeysers();
            if (minerals != null) blockers.addAll(minerals.inRadius(12, unit).list());
            Selection buildings = Select.ourBuildings();
            if (buildings != null) blockers.addAll(buildings.inRadius(12, unit).list());
        } catch (Exception ignored) {
        }
        return blockers;
    }

    static List<AUnit> foes(AUnit unit) {
        try {
            Selection enemies = unit.enemiesNear();
            if (enemies == null) return new ArrayList<>();

            return new ArrayList<>(enemies.combatUnits().list());
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }
}
