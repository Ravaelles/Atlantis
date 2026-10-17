package atlantis.units.select;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * Radius-based spatial filtering, extracted from {@link Selection} (NEXT
 * #10, slice 1): one-line delegates there keep every call site untouched,
 * the logic lives here. Pure selection plumbing - no game reads beyond
 * what the predicates already used.
 */
final class SelectionRadius {

    private SelectionRadius() {
    }

    static Selection inRadius(Selection selection, double maxDist, AUnit unit) {
        return Select.cache.get(
            selection.addToCachePath("inRadiusA:" + maxDist + ":" + unit.idWithHash()),
            0,
            () -> selection.cloneByRemovingIf(
                (u -> !u.hasPosition() || u.distTo(unit) > maxDist),
                "inRadiusA:" + maxDist + ":" + unit.idWithHash()
            )
        );
    }

    static Selection inRadius(Selection selection, double maxDist, HasPosition position) {
        return Select.cache.get(
            selection.addToCachePath("inRadiusB:" + maxDist + ":" + position),
            0,
            () -> selection.cloneByRemovingIf(
                (u -> u.distTo(position) > maxDist),
                "inRadiusB:" + maxDist + ":" + position
            )
        );
    }

    static Selection inGroundRadius(Selection selection, double maxDist, AUnit unit) {
        return Select.cache.get(
            selection.addToCachePath("inGroundRadius:" + maxDist + ":" + unit.idWithHash()),
            0,
            () -> selection.cloneByRemovingIf(
                (u -> u.groundDist(unit) > maxDist),
                "inGroundRadius:" + maxDist + ":" + unit.idWithHash()
            )
        );
    }

    static Selection notInRadius(Selection selection, double minDist, HasPosition position) {
        return Select.cache.get(
            selection.addToCachePath("notInRadius:" + minDist + ":" + position),
            0,
            () -> selection.cloneByRemovingIf(
                (u -> u.distTo(position) < minDist),
                "notInRadius:" + minDist + ":" + position
            )
        );
    }

    static Selection inRadius(Selection selection, double maxDist, Selection selectionOfUnits) {
        return Select.cache.get(
            selection.addToCachePath(CacheKey.create("inRadiusSelection", maxDist, selectionOfUnits)),
            1,
            () -> selection.cloneByRemovingIf(
                (u -> selectionOfUnits.nearestTo(u).distTo(u) > maxDist),
                "inRadius:" + maxDist + ":" + CacheKey.create(selectionOfUnits)
            )
        );
    }

    static int countInRadius(Selection selection, double maxDist, HasPosition unitOrPosition) {
        return Select.cacheInt.get(
            selection.addToCachePath("countInRadius:" + maxDist + ":" + unitOrPosition),
            0,
            () -> selection.cloneByRemovingIf(
                (u -> u.distTo(unitOrPosition) > maxDist),
                maxDist + ":" + unitOrPosition
            ).count()
        );
    }

    static Selection maxGroundDist(Selection selection, double maxDist, AUnit unit) {
        if (unit.isAir()) {
            return selection;
        }

        return Select.cache.get(
            selection.addToCachePath("maxGroundDist:" + maxDist + ":" + unit.idWithHash()),
            0,
            () -> selection.cloneByRemovingIf(
                (u -> u.groundDist(unit) > maxDist),
                ":" + unit.idWithHash()
            )
        );
    }
}
