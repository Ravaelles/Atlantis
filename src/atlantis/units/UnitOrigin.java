package atlantis.units;

/**
 * Is this unit a stand-in the test harness put in the world?
 *
 * <p>Three production sites need to know, and until recently each of them asked
 * the class itself:</p>
 * <ul>
 *   <li>{@code AUnit.cacheType()} - a stand-in carries the type the test gave it
 *       (a drone that was morphed into a creep colony), so the wrapper must not
 *       overwrite it from the engine object, which does not exist;</li>
 *   <li>{@code AUnit.init()} - the same reason, one step earlier;</li>
 *   <li>{@code AtlantisJfap.isValidUnit} - a stand-in has no engine object, but
 *       it is still a unit to simulate a fight with.</li>
 * </ul>
 *
 * <p>Asking the class meant {@code src/atlantis} imported
 * {@code tests.fakes.FakeUnit}, which is why the game jar had to ship the test
 * harness. The question itself is legitimate - "is this a simulated unit?" - so
 * it stays, and the class that can answer it moves behind this port.</p>
 *
 * <p>In a game the answer is always no: nothing in a real game is simulated.
 * With no source installed that is what this class says, so production reads the
 * engine exactly as before.</p>
 */
public class UnitOrigin {
    public interface Source {
        boolean isSimulated(AUnit unit);
    }

    private static Source source = null;

    public static void useSource(Source newSource) {
        source = newSource;
    }

    public static void useGameUnits() {
        source = null;
    }

    public static boolean isSimulated(AUnit unit) {
        return source != null && source.isSimulated(unit);
    }
}