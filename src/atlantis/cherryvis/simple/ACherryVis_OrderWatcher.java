package atlantis.cherryvis.simple;

import atlantis.cherryvis.CV;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.Map;
import java.util.TreeMap;

/**
 * Engine-order narrative for CherryVis unit logs (owner ask): every frame,
 * each own non-building unit's LAST COMMAND ({@code lastCommandName}, the
 * engine's own getLastCommand: Move, Gather, Build, Train, Attack_Unit, ...)
 * is compared with the previous frame; a change lands in the unit's log as
 * {@code #Move@3910} - hash, command, frame.
 *
 * <p>Deliberately NOT a manager: it only observes and never takes control, so
 * it cannot perturb the chain it reports on (a manager in the chain would
 * decide the unit it watches). Runs only while tracing, same population as
 * the manager mirror. Command changes are rare, so a traced game pays ~30
 * polls per frame and logs almost nothing.</p>
 */
public class ACherryVis_OrderWatcher {

    private static final Map<Integer, String> lastOrders = new TreeMap<>();

    public static void update() {
        for (AUnit unit : Select.ourUnitsWithUnfinishedList()) {
            if (unit == null || unit.isABuilding() || unit.u() == null) continue;

            // Owner spec: "#Move@3910" from the unit's last command name (the
            // engine's own getLastCommand, null-safe inside).
            String command = "#" + unit.lastCommandName();
            if (!command.equals(lastOrders.get(unit.id()))) {
                lastOrders.put(unit.id(), command);
                CV.unitLog(command + "@" + A.now(), unit);
            }
        }
    }

    /** Static maps leak between games in one JVM, so clear it per game. */
    public static void clear() {
        lastOrders.clear();
    }
}
