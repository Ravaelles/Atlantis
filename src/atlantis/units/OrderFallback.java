package atlantis.units;

/**
 * Where an order goes when the unit it was issued to has no engine object.
 *
 * <p>{@code AUnitOrders} has three methods of the shape "if the engine has this
 * unit, process the order; otherwise record it somewhere". The "somewhere" was
 * {@code tests.fakes.FakeUnitData}, imported by production code, so the game jar
 * carried the test harness. That is now a port, and the recording lives with the
 * fakes.</p>
 *
 * <p>In a game this is unreachable: orders are issued to units the engine owns,
 * and the branches that consult it are guarded by {@code u() != null}. The
 * default therefore answers {@code false} - an order the game never received was
 * not issued - which is also the truth if a unit without an engine object ever
 * reaches these methods outside a test.</p>
 */
public class OrderFallback {
    public interface Sink {
        /**
         * @return whether the order counts as issued.
         */
        boolean train(AUnitType unitToTrain);

        /**
         * @return whether the order counts as issued.
         */
        boolean cancelConstruction(AUnit unit);
    }

    private static Sink sink = null;

    public static void useSink(Sink newSink) {
        sink = newSink;
    }

    public static void useNoSink() {
        sink = null;
    }

    public static boolean train(AUnitType unitToTrain) {
        return sink != null && sink.train(unitToTrain);
    }

    public static boolean cancelConstruction(AUnit unit) {
        return sink != null && sink.cancelConstruction(unit);
    }
}