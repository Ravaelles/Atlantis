package atlantis.cherryvis.simple;

import atlantis.cherryvis.CV;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.Map;
import java.util.TreeMap;

/**
 * Engine-order narrative for CherryVis unit logs (owner ask): every frame,
 * each own non-building unit's ENGINE order ({@code getOrder}:
 * MiningMinerals, Move, PlayerGuard, PlaceBuilding, Nothing, ...) is compared
 * with the previous frame; a change lands in the unit's log as
 * {@code Move@1128}.
 *
 * <p>Deliberately NOT a manager: it only observes and never takes control, so
 * it cannot perturb the chain it reports on (a manager in the chain would
 * decide the unit it watches). Runs only while tracing, same population as
 * the manager mirror. Order changes are rare, so a traced game pays ~30
 * getOrder polls per frame and logs almost nothing.</p>
 */
public class ACherryVis_OrderWatcher {

    private static final Map<Integer, String> lastOrders = new TreeMap<>();

    public static void update() {
        for (AUnit unit : Select.ourUnitsWithUnfinishedList()) {
            if (unit == null || unit.isABuilding() || unit.u() == null) continue;

            Object raw = unit.u().getOrder();
            String order = raw == null ? "None" : raw.toString();
            if (!order.equals(lastOrders.get(unit.id()))) {
                lastOrders.put(unit.id(), order);
                CV.unitLog(order + "@" + A.now(), unit);
            }
        }
    }

    /** Static maps leak between games in one JVM, so clear it per game. */
    public static void clear() {
        lastOrders.clear();
    }
}
