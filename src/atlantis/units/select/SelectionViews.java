package atlantis.units.select;

import atlantis.units.AUnit;
import atlantis.units.Units;

import java.util.List;

/**
 * Container views, extracted from {@link Selection} (NEXT #10, slice 6):
 * one-line delegates there keep every call site untouched, the logic lives
 * here. Pure selection plumbing - materialization and cardinality answers
 * straight off the backing list, null/empty-safe by construction.
 */
final class SelectionViews {

    private SelectionViews() {
    }

    static List<AUnit> list(Selection selection) {
        return selection.data;
    }

    static Units units(Selection selection) {
        return new Units(selection.data);
    }

    static int count(Selection selection) {
        return selection.data.size();
    }

    static int size(Selection selection) {
        return selection.data.size();
    }

    static boolean isEmpty(Selection selection) {
        return selection.data.size() == 0;
    }

    static boolean empty(Selection selection) {
        return selection.data.size() == 0;
    }

    static boolean isNotEmpty(Selection selection) {
        return selection.data.size() > 0;
    }

    static boolean notEmpty(Selection selection) {
        return selection.data.size() > 0;
    }

    static boolean anyExists(Selection selection) {
        return !selection.data.isEmpty();
    }

    static boolean atLeast(Selection selection, double min) {
        return selection.data.size() >= min;
    }

    static boolean atMost(Selection selection, double max) {
        return selection.data.size() <= max;
    }
}
