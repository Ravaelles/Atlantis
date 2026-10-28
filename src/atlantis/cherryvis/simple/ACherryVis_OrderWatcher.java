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
                mirrorBehaviorAsTask(unit);
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

    /** "/<behavior>" - the active behavior tag the bot last gave this unit.
     * Reads the unit's behavior field first (set by whoever drives it -
     * a manager or an order tag), falling back to the display tooltip so
     * paths that only set tooltips keep working. Never bare while the unit
     * has done anything nameable. */
    private static String behaviorTag(AUnit unit) {
        String behavior = unit.behavior();
        if (behavior == null || behavior.isEmpty()) behavior = unit.tooltip();

        return behavior == null || behavior.isEmpty() ? "" : "/" + behavior;
    }

    /**
     * Task for managerless units (NEXT #221 follow-up): the viewer reads
     * Task from {@code units_updates[].manager}, which only manager-driven
     * units ever wrote - every army row read None. On a behavior change
     * the watcher mirrors the behavior there, so Task names what the unit
     * does. Manager-driven units are untouched (their manager rows own
     * the slot; overwriting them would flap Task). Change-only like the
     * log row above, so no new volume.
     */
    private static void mirrorBehaviorAsTask(AUnit unit) {
        String behavior = unit.behavior();
        if (behavior == null || behavior.isEmpty()) behavior = unit.tooltip();
        if (behavior == null || behavior.isEmpty()) return;
        if (!(unit.manager() instanceof atlantis.battle.generic.DoNothing)) return;

        CV.unitManager(behavior, unit);
    }

    /**
     * Static maps leak between games in one JVM, so clear it per game.
     */
    public static void clear() {
        lastOrders.clear();
    }
}
