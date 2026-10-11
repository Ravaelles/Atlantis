package atlantis.production.constructions.builders;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TilePosition;

/**
 * Per-frame trace of every unit that has a construction assigned to it: the
 * manager
 * currently handling the unit, the last command it actually issued, and where
 * that
 * command points.
 *
 * <p>
 * <b>Why this exists.</b> The failure it was written for looks like this in the
 * log:
 * </p>
 *
 * <pre>
 *   SUCCESS FOR Pylon at 113,15        &lt;- placement found a valid tile
 *   Builder diagnostic: unit=#118 Probe @[113,15] ... status=NOT_STARTED
 *   Cancel construction: Pylon took too long (44s) / at:[113,15] / buildable:true
 * </pre>
 *
 * <p>
 * Nothing throws, the tile is valid, the builder is assigned - and the building
 * never
 * starts. The two facts that separate the possible causes are not in any log
 * today:
 * <b>which manager is actually running the unit</b> (a manager that never
 * issues the
 * order keeps the unit "busy" forever, and looks identical to one that does)
 * and
 * <b>what the most recent command was and where it pointed</b> (an order issued
 * to the
 * wrong place looks the same as no order at all, from the outside).
 * </p>
 *
 * <p>
 * <b>Off by default.</b> {@code BUILDER_LOG=1} in ENV enables it. Printing this
 * every
 * frame for every builder is noisy and costs time in the frame budget, so it is
 * a
 * diagnostic switch, not a feature - the same shape as {@code PRODUCTION_V2}
 * and
 * {@code PLACEMENT}.
 * </p>
 *
 * <p>
 * Read the output as: a unit stuck on one manager with a stale or empty
 * {@code lastCommand} means the order is never issued (look at that manager); a
 * unit
 * whose {@code lastCommand} changes but points away from {@code nearestToBuild}
 * means the order is issued to the wrong place (look at how the target is
 * chosen).
 * </p>
 */
public class BuilderDiagnostics {

    /** Prints one line per builder with a construction, once per frame. */
    public static void logBuildersIfEnabled() {
        if (!Env.builderLog())
            return;

        for (AUnit unit : Select.ourWorkers().list()) {
            Construction construction = ConstructionRequests.constructionFor(unit);
            if (construction == null)
                continue;

            System.err.println(describe(unit, construction));
        }
    }

    /**
     * The result of the actual `unit.build(...)` call, printed at the moment it is
     * made - the one fact a per-frame trace cannot show, because `build()` may be
     * attempted once and refused.
     *
     * <p>
     * This is the line that answers "the builder is standing on a valid, explored tile
     * and can afford the building, so why is there no Pylon": the per-frame trace
     * proves the preconditions, this proves what the engine answered. Enabled by the
     * same {@code BUILDER_LOG=1}.
     * </p>
     *
     * <p>
     * Deliberately called from {@code IssueBuildOrder} (the production side) and not
     * from {@code AUnitOrders} (the core): core may not depend on its consumers -
     * {@code ArchitectureBoundaryTest.coreMustNotDependOnOtherContexts} caught exactly
     * that when this was first wired the other way (measured 2026-10-10). The debounce
     * exit inside {@code AUnitOrders.build} is therefore visible here only as a missing
     * attempt, not as its own line; the trace's two-frame rhythm shows it.
     * </p>
     */
    public static void logBuildAttempt(AUnit unit, Construction construction, TilePosition tile, boolean result) {
        if (!Env.builderLog()) return;

        APosition at = construction.buildPosition();
        System.err.println("BUILD_ATTEMPT @" + A.now()
            + " unit=#" + unit.id() + " " + unit.type().name()
            + " building=" + construction.buildingType().name()
            + " tile=[" + tile.getX() + "," + tile.getY() + "]"
            + " engineAccepted=" + result
            + " || canBuildHere=" + (at == null ? "no-position" : at.isBuildableIncludeBuildings())
            + " explored=" + exploredFootprint(construction)
            + " canAfford=" + A.canAfford(
                construction.buildingType().mineralPrice(), construction.buildingType().gasPrice())
            + " distToBuild=" + (at == null ? "-" : A.digit(unit.distTo(at)))
            + " isConstructing=" + unit.isConstructing()
            + " isMoving=" + unit.isMoving()
            + " action=" + unit.action().name()
            + " lastCommand=" + unit.lastCommandName()
            // The preconditions the ENGINE's own canIssueCommand checks before it looks
            // at the building type at all (bwapi/Unit.class: canCommand() is the first
            // gate in canIssueCommand). If build() still returns false with all of these
            // true, the refusal is in the type-specific branch, not the generic one.
            + " canCommand=" + unit.u().canCommand()
            + " isPowered=" + unit.u().isPowered()
            + " isLockedDown=" + unit.u().isLockedDown()
            + " getOrder=" + unit.u().getOrder()
            + " isCompleted=" + unit.isCompleted());
    }

    /**
     * One line: unit, its current manager, the last issued command, the command's
     * target, and where the construction it owns actually is.
     */
    public static String describe(AUnit unit, Construction construction) {
        AUnit target = unit.target();
        APosition targetPosition = unit.targetPosition();

        return "BUILDER @" + A.now()
                + " unit=#" + unit.id() + " " + unit.type().name() + " @[" + unit.tx() + "," + unit.ty() + "]"
                + " manager=" + currentManagerName(unit)
                + " lastCommand=" + unit.lastCommandName()
                + " targetPosition=" + (targetPosition == null
                    ? "none" : "[" + targetPosition.tx() + "," + targetPosition.ty() + "]")
                + " target=" + (target == null ? "none" : target.type().name() + "#" + target.id())
                + " || construction=" + construction.buildingType().name()
                + " status=" + construction.status()
                + " at=[" + construction.buildPosition().tx() + "," + construction.buildPosition().ty() + "]"
                + " buildable=" + construction.buildPosition().isBuildableIncludeBuildings()
                + " distToBuild=" + A.digit(unit.distTo(construction.buildPosition()))
                + " canAfford=" + A.canAfford(construction.buildingType().mineralPrice(), construction.buildingType().gasPrice())
                + " isMoving=" + unit.isMoving()
                + " isConstructing=" + unit.isConstructing()
                + " action=" + unit.action().name()
                + " lastActionAgo=" + unit.lastActionAgo(unit.action())
                + " lastCommandIssuedAgo=" + unit.lastCommandIssuedAgo()
                + " exploredOnFootprint=" + exploredFootprint(construction);
    }

    /**
     * Whether every footprint tile of the construction is explored, and which tiles are
     * not.
     *
     * <p>
     * Kept even though it answered its original question with a <b>no</b>: the command
     * path's `canBuildHere(..., true)` does require exploration
     * (`bwapi/Game.class`: the `checkExplored` term adds `isExplored` per tile), and the
     * placement search never asks it, so "is the finder seeing a tile the command will
     * refuse?" is a real question on a fogged map. Measured 2026-10-10: on the ordinary
     * first-Pylon case it is always `4/4`, because `TravelToConstruct` only issues
     * `build` after the builder arrives - so it is not the cause of that failure.
     * It stays as a one-word answer for the next time a build is refused somewhere the
     * bot has not looked yet.
     * </p>
     */
    private static String exploredFootprint(Construction construction) {
        APosition at = construction.buildPosition();
        if (at == null) return "no-position";

        int w = construction.buildingType().getTilesWidth();
        int h = construction.buildingType().getTilesHeights();
        int explored = 0;
        StringBuilder missing = new StringBuilder();

        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                APosition tile = APosition.create(at.tx() + dx, at.ty() + dy);
                if (tile.isExplored()) explored++;
                else missing.append("[").append(tile.tx()).append(",").append(tile.ty()).append("]");
            }
        }

        return explored + "/" + (w * h) + (missing.length() == 0 ? "" : " missing=" + missing);
    }

    /**
     * The manager that handled the unit this frame. {@code manager()} is the active
     * one; {@code runningManager().lastRunMode()} names the running behaviour when
     * the
     * unit is in the running subsystem, which is otherwise invisible here.
     */
    private static String currentManagerName(AUnit unit) {
        String manager = unit.manager() != null ? unit.manager().getClass().getSimpleName() : "null";
        String running = unit.runningManager() != null ? unit.runningManager().lastRunMode() : "null";

        return manager + "(running=" + running + ")";
    }

    
}
