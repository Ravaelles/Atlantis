package atlantis.game.util;

import atlantis.game.A;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How many of each of our units were EVER completed, and when the first one
 * appeared.
 *
 * <p>
 * The end-of-game summary could only report what is alive right now
 * ({@code Select.ourWithUnfinished(...).count()}), which loses the interesting
 * half: a game
 * that finished with 3 Pylons may have built 4 and lost one, and "Pylon=3/4"
 * says that at a
 * glance. Two facts are worth having and neither is recoverable from the live
 * board:
 * </p>
 *
 * <ul>
 * <li><b>built vs remaining</b> - the difference between them is what was
 * destroyed, which
 * is the only way a summary line distinguishes \"we never made one\" from \"we
 * made one and
 * lost it\";</li>
 * <li><b>the first completion of each type</b> - used to order the report so
 * the line reads
 * like the game's build progression (Pylon before Gateway before Cybernetics
 * Core), which is
 * more informative than any alphabetical or hardcoded order, and costs nothing
 * to keep.</li>
 * </ul>
 *
 * <p>
 * <b>Only completed units count</b>, by design and per the owner's ruling: a
 * unit that was
 * started and cancelled, or that is still under construction when the game
 * ends, is not
 * something the bot \"built\". {@code onUnitComplete} is therefore the only
 * entry point, not
 * {@code onUnitCreate}.
 * </p>
 *
 * <p>
 * Lives outside {@code AUnit}/{@code Select} on purpose: it is a record of
 * events, not game
 * state, and the classes that own game state are the ones this project is
 * trying to make
 * smaller. It is reset per game like every other cross-game accumulator
 * ({@code UnitsArchive}, {@code ReservedResources}).
 * </p>
 */
public final class CompletedUnitsHistory {

    /**
     * How many of each type we completed over the whole game.
     *
     * <p>
     * Keyed by the TYPE, not by its name, and that is not a style choice: {@code name()}
     * returns the display form ({@code CyberneticsC}) and {@code AUnitType.getByName(...)}
     * does not resolve it back, so a name-keyed map silently DROPPED types from the report
     * while still counting them. Found by a test that asserted the report order and came back
     * with an entry missing rather than merely mis-ordered.
     * </p>
     */
    private static final Map<AUnitType, Integer> completed = new LinkedHashMap<>();

    /** The frame each type was first completed on; the report's sort key. */
    private static final Map<AUnitType, Integer> firstCompletedAt = new LinkedHashMap<>();

    /**
     * Tie-break for types first completed on the same frame.
     *
     * <p>
     * Frame numbers alone are not enough: several units routinely finish together (a round of
     * Pylons, a Gateway and a Zealot can land on one frame), and without a second key the
     * order would follow whatever {@code AUnitType} iteration the runtime produced - which can
     * differ between two runs of the same game and makes the report incomparable. The sequence
     * each type was first recorded in is used instead: deterministic, and it is also the order
     * the events actually arrived.
     * </p>
     */
    private static final Map<AUnitType, Integer> firstRecordedSequence = new LinkedHashMap<>();

    private static int recordCounter = 0;

    private CompletedUnitsHistory() {
    }

    /**
     * Record one finished unit. Called from {@code OnUnitCompleted}, which BWAPI
     * fires once
     * per unit when construction or training finishes - so this counts the event,
     * not the
     * unit's presence, and cannot double-count a unit the way a per-frame scan
     * would.
     */
    public static void record(AUnitType type) {
        if (type == null)
            return;

        completed.merge(type, 1, Integer::sum);
        firstCompletedAt.putIfAbsent(type, A.now());
        firstRecordedSequence.putIfAbsent(type, recordCounter++);
    }

    /** How many of this type were completed in total; 0 when none ever were. */
    public static int completedCount(AUnitType type) {
        if (type == null)
            return 0;
        return completed.getOrDefault(type, 0);
    }

    /**
     * The frame the first one finished, or {@code Integer.MAX_VALUE} when none did.
     */
    public static int firstCompletedFrame(AUnitType type) {
        if (type == null)
            return Integer.MAX_VALUE;
        return firstCompletedAt.getOrDefault(type, Integer.MAX_VALUE);
    }

    /**
     * Every type we completed, ordered by when its FIRST unit finished.
     *
     * <p>
     * First-completion order is the report's order because it reads like the game did:
     * whatever the bot made first appears first, so a glance shows how the opening went.
     * Ties are broken by the sequence the types were first recorded in (see
     * {@link #firstRecordedSequence}) - deterministic across runs, unlike any ordering that
     * depends on how the runtime happens to iterate a set.
     * </p>
     */
    public static List<AUnitType> typesByFirstCompletion() {
        List<AUnitType> types = new ArrayList<>(completed.keySet());

        types.sort((a, b) -> {
            int byFrame = Integer.compare(
                    firstCompletedAt.getOrDefault(a, Integer.MAX_VALUE),
                    firstCompletedAt.getOrDefault(b, Integer.MAX_VALUE));
            if (byFrame != 0) return byFrame;

            return Integer.compare(
                    firstRecordedSequence.getOrDefault(a, Integer.MAX_VALUE),
                    firstRecordedSequence.getOrDefault(b, Integer.MAX_VALUE));
        });

        return types;
    }

    /** Per-game reset, like {@code UnitsArchive.reset()}. */
    public static void reset() {
        completed.clear();
        firstRecordedSequence.clear();
        recordCounter = 0;
        firstCompletedAt.clear();
        firstRecordedSequence.clear();
        recordCounter = 0;
    }
}
