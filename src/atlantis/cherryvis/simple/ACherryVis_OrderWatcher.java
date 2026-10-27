package atlantis.cherryvis.simple;

import atlantis.cherryvis.CV;
import atlantis.game.A;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.Map;
import java.util.TreeMap;

/**
 * Engine-order narrative for CherryVis unit logs (owner ask): every frame,
 * each own non-building unit's LAST COMMAND ({@code lastCommandName}, the
 * engine's own getLastCommand: Move, Gather, Build, Train, Attack_Unit, ...)
 * is compared with the previous frame; a change lands in the unit's log as
 * {@code #Move@0:12} - hash, command, game time.
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
            if (unit == null || unit.isABuilding()) continue;

            // Owner spec: "#Move@0:12" from the unit's last command name (the
            // engine's own getLastCommand, null-safe inside).
            // NEXT #214: name WHAT the builder is building and WHY next to
            // the command, so a stuck builder (site-refresh chase, covered
            // tile) reads off one row instead of needing a WINE_DEBUG hunt.
            // Change-only on all three parts: a steady builder stays silent.
            String command = "#" + unit.lastCommandName() + buildingTag(unit) + behaviorTag(unit);
            if (!command.equals(lastOrders.get(unit.id()))) {
                lastOrders.put(unit.id(), command);
                CV.unitLog(command + "@" + A.minSec(), unit);
            }
        }
    }

    /** "/Pylon" while this builder holds a live construction of that type. */
    private static String buildingTag(AUnit unit) {
        Construction construction = unit.construction();
        if (construction == null || !construction.notFinished()) return "";

        // A cancelled order leaves the unit's link dangling (cancel removes
        // the order, never the pointer): only a construction the engine can
        // still place names the row, or every freed builder keeps advertising
        // its last job.
        if (!atlantis.production.constructions.ConstructionRequests.constructions.contains(construction)) {
            return "";
        }

        return "/" + construction.buildingType().name();
    }

    /** "/<tooltip>" - the behavior tag the bot last gave this unit. */
    private static String behaviorTag(AUnit unit) {
        String tooltip = unit.tooltip();

        return tooltip == null || tooltip.isEmpty() ? "" : "/" + tooltip;
    }

    /**
     * Static maps leak between games in one JVM, so clear it per game.
     */
    public static void clear() {
        lastOrders.clear();
    }
}
