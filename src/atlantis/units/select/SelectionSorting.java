package atlantis.units.select;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Ordering extracted from {@link Selection} (NEXT #10, slice 3).
 *
 * <p>Distance/health sorts mutate the selection data in place; {@code Selection}
 * keeps one-line delegates so every call site is untouched.</p>
 */
final class SelectionSorting {

    private SelectionSorting() {
    }

    static List<AUnit> sortDataByGroundDistanceTo(Selection selection, final HasPosition position,
                                                  final boolean nearestFirst) {
//        if (position == null) {
//            return null;
//        }

        List<AUnit> data = selection.data;
        if (data.size() != 1) {
            Collections.sort(data, new Comparator<HasPosition>() {
                @Override
                public int compare(HasPosition p1, HasPosition p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = p1.groundDist(position);
                    double distance2 = p2.groundDist(position);

                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                }
            });
        }

        return data;
    }

    static List<AUnit> sortDataByDistanceTo(Selection selection, final HasPosition position,
                                            final boolean nearestFirst) {
        List<AUnit> data = selection.data;
        if (position == null) {
            return data;
        }

        if (data.size() != 1) {
            Collections.sort(data, new Comparator<HasPosition>() {
                @Override
                public int compare(HasPosition p1, HasPosition p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = p1.distTo(position);
                    double distance2 = p2.distTo(position);

                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                }
            });
        }

        return data;
    }

    static List<AUnit> sortDataByDistanceTo(Selection selection, final AUnit unit, final boolean nearestFirst) {
        List<AUnit> data = selection.data;
        if (data.size() != 1) {
            Collections.sort(data, new Comparator<AUnit>() {
                @Override
                public int compare(AUnit p1, AUnit p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = unit.distTo(p1);
                    double distance2 = unit.distTo(p2);

                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                }
            });
        }

        return data;
    }

    static Selection sortByNearestTo(Selection selection, final AUnit unit) {
        List<AUnit> data = selection.data;
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        if (data.size() >= 2) {
            data.sort(new Comparator<AUnit>() {
                @Override
                public int compare(AUnit p1, AUnit p2) {
                    if (unit == null) return 1;

                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = unit.distTo(p1);
                    double distance2 = unit.distTo(p2);

                    return Double.compare(distance1, distance2);
                }
            });
        }

        return selection;
    }

    static Selection sortByHealth(Selection selection) {
        List<AUnit> data = selection.data;
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        if (data.size() != 1) {
            data.sort(Comparator.comparingDouble(AUnit::hpPercent));
        }

        return selection;
    }

    static Selection sortByWound(Selection selection) {
        List<AUnit> data = selection.data;
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        if (data.size() != 1) {
            data.sort(Comparator.comparingDouble(AUnit::woundOrder));
        }

        return selection;
    }
}
