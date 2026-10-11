package atlantis.production.constructions.builders;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.units.AUnit;
import bwapi.TilePosition;

/**
 * Diagnostics for the build command itself: whether the engine accepted it, and under what
 * preconditions.
 *
 * <p>
 * <b>The per-frame builder trace was removed on 2026-10-11, once it had done its job.</b> It
 * printed one line per builder per frame (the {@code BUILDER @...} line) and existed to
 * separate "the order is never issued" from "the order is issued and refused". Measured with
 * it, the refusal turned out to be an accepted command whose <em>status</em> never advanced,
 * which was fixed in {@code ProtossWarping} and {@code ConstructionStatusChanger}
 * ({@code NEXT.md} #53). With that closed the trace was 10 283 lines of noise costing frame
 * budget on every run, so it is gone rather than left switched off. What stays is the one line
 * the fix depended on - {@link #logBuildAttempt} - which fires only when a build is actually
 * attempted and so is cheap and decisive.
 * </p>
 */
public class BuilderDiagnostics {

    /**
     * The result of the actual `unit.build(...)` call, printed at the moment it is
     * made - the one fact a per-frame trace cannot show, because `build()` may be
     * attempted once and refused.
     *
     * <p>
     * This is the line that answers "the builder is standing on a valid, explored tile
     * and can afford the building, so why is there no Pylon": the per-frame trace used
     * to prove the preconditions; this proves what the engine answered. Enabled by
     * {@code BUILDER_LOG=1}.
     * </p>
     *
     * <p>
     * Deliberately called from {@code IssueBuildOrder} (the production side) and not
     * from {@code AUnitOrders} (the core): core may not depend on its consumers -
     * {@code ArchitectureBoundaryTest.coreMustNotDependOnOtherContexts} caught exactly
     * that when this was first wired the other way (measured 2026-10-10). The debounce
     * exit inside {@code AUnitOrders.build} is therefore visible here only as a missing
     * attempt, not as its own line.
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
}
