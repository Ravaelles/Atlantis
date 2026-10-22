package atlantis.units.select;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.map.position.APosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    static APosition center(Selection selection) {
        return units(selection).average();
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

    static Selection add(Selection selection, Selection otherSelection) {
        return selection.cloneByAdding(otherSelection.data, null);
    }

    static Selection add(Selection selection, Collection<? extends AUnit> otherUnits) {
        return selection.cloneByAdding(otherUnits, null);
    }

    static Selection add(Selection selection, AUnit addUnit) {
        ArrayList<AUnit> list = new ArrayList<>();
        list.add(addUnit);

        return selection.cloneByAdding(list, null);
    }

    static Selection exclude(Selection selection, AUnit unitToExclude) {
        if (unitToExclude == null) return new Selection(new ArrayList<>(selection.data), null);

        List<AUnit> newData = new ArrayList<>(selection.data);
        newData.remove(unitToExclude);
        return new Selection(newData, "exclude:" + unitToExclude.id());
    }

    static Selection exclude(Selection selection, Collection unitsToExclude) {
        List<AUnit> newData = new ArrayList<>(selection.data);
        newData.removeAll(unitsToExclude);

        return new Selection(newData, null);
    }

    static Selection exclude(Selection selection, Selection otherSelection) {
        return exclude(selection, otherSelection.list());
    }

    static Selection reverse(Selection selection) {
        Collections.reverse(selection.data);

        return selection;
    }

    static Selection removeDuplicates(Selection selection) {
        List<AUnit> newData = selection.data.stream().distinct().collect(Collectors.toList());
        return new Selection(newData, selection.currentCachePath + ":removeDuplicates");
    }

    static Selection limit(Selection selection, int limit) {
        if (selection.data.isEmpty()) return selection;

        List<AUnit> newData = new ArrayList<>();
        int max = Math.min(limit, selection.data.size());
        for (int i = 0; i < max; i++) {
            newData.add(selection.data.get(i));
        }

        return new Selection(newData, null);
    }

    static int countOfType(Selection selection, AUnitType... types) {
        int total = 0;
        for (AUnit unit : selection.data) {
            boolean typeMatches = false;
            for (AUnitType type : types) {
                if (unit.is(type) || (unit.is(AUnitType.Zerg_Egg) && type.equals(unit.buildType()))) {
                    typeMatches = true;
                    break;
                }
            }
            if (typeMatches) {
                total++;
            }
        }

        return total;
    }

    static int totalHp(Selection selection) {
        return selection.data.stream()
            .map(AUnit::hp)
            .reduce(0, Integer::sum);
    }

    static int totalHits(Selection selection) {
        return selection.data.stream()
            .map(AUnit::totalHitCount)
            .reduce(0, Integer::sum);
    }

    static int totalKills(Selection selection) {
        return selection.data.stream()
            .map(AUnit::killCount)
            .reduce(0, Integer::sum);
    }

    static int averageShields(Selection selection) {
        if (selection.data.isEmpty()) return 0;

        int totalShields = 0;
        for (AUnit unit : selection.data) {
            totalShields += unit.shields();
        }

        return totalShields / selection.data.size();
    }
}
