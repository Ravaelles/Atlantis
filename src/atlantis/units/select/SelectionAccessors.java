package atlantis.units.select;

import atlantis.game.ARandom;
import atlantis.units.AUnit;

import java.util.Random;

/**
 * Element accessors, extracted from {@link Selection} (NEXT #10, slice 5):
 * one-line delegates there keep every call site untouched, the logic lives
 * here. Pure selection plumbing - positional answers by list order, null
 * on empty selections, never an exception.
 */
final class SelectionAccessors {

    private SelectionAccessors() {
    }

    static AUnit first(Selection selection) {
        return selection.data.isEmpty() ? null : (AUnit) selection.data.get(0);
    }

    static AUnit second(Selection selection) {
        return selection.data.size() < 2 ? null : (AUnit) selection.data.get(1);
    }

    static AUnit randomWithSeed(Selection selection, int seed) {
        if (selection.data.isEmpty()) return null;
        if (selection.data.size() == 1) return selection.data.get(0);

        Random rand = new Random(seed);
        return selection.data.get(rand.nextInt(selection.data.size()));
    }

    static AUnit last(Selection selection) {
        return selection.data.isEmpty() ? null : (AUnit) selection.data.get(selection.data.size() - 1);
    }

    static AUnit random(Selection selection) {
        return (AUnit) ARandom.getRandomElement(selection.data);
    }
}
