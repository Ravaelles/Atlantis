package atlantis.units;

/**
 * Is this unit a stand-in the test harness put in the world?
 *
 * <p>Two production sites need to know, and both asked the class itself
 * ({@code AtlantisJfap.isValidUnit} and {@code JfapCombatEvaluator.addFriends}):
 * a stand-in has no engine object, but it is still a unit to simulate a fight
 * with. Asking the class meant {@code src/atlantis} imported
 * {@code tests.fakes.FakeUnit}, which is why the game jar had to ship the test
 * harness. The question itself is legitimate - "is this a simulated unit?" - so
 * it stays, and the class that can answer it moves behind this port.</p>
 *
 * <p>In a game the answer is always no: nothing in a real game is simulated. With
 * no source installed that is what this class says, so production reads the engine
 * exactly as before.</p>
 *
 * <p><b>Contract:</b> a test that builds units must install the harness's
 * sources, which every test extending {@code AbstractTestWithUnits} does. Without
 * one, this class answers "not simulated" and a stand-in is invisible to the
 * combat evaluator. {@code AUnit} deliberately does <b>not</b> ask: its own
 * question ("is there an engine object?") is answerable from the unit, and an
 * answer that depends on the class order is not an answer.</p>
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