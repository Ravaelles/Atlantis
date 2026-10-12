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
     * Full decomposition of the engine's refusal, printed when a builder stands
     * still on a NOT_STARTED site (NEXT #59): the ordinary attempt line proves
     * preconditions, this one names WHICH {@code canBuild} term says no by
     * calling every level of the chain separately - the no-builder tile rule,
     * the with-builder reachability (patched terrain fallback vs raw engine),
     * our own port's answer on the same pair, and the full gate.
     *
     * <p>Same cost rules as {@link #logBuildAttempt}: {@code BUILDER_LOG=1} and
     * only on a 60+ frame standstill, so a healthy run never pays for it.</p>
     */
    public static void logStandstillBreakdown(AUnit unit, Construction construction) {
        if (!Env.builderLog()) return;
        if (!A.everyNthGameFrame(30)) return;
        if (unit == null || unit.u() == null) return;
        if (construction.buildPosition() == null) return;
        if (!unit.lastPositionChangedMoreThanAgo(60)) return;

        APosition at = construction.buildPosition();
        TilePosition tile = new TilePosition(at.tx(), at.ty());
        bwapi.Unit builder = unit.u();
        bwapi.UnitType type = construction.buildingType().ut();
        bwapi.Position siteCenter = new bwapi.Position(
            at.tx() * 32 + construction.buildingType().getTilesWidth() * 16,
            at.ty() * 32 + construction.buildingType().getTilesHeights() * 16
        );

        System.err.println("BUILD_BREAKDOWN @" + A.now()
            + " unit=#" + unit.id()
            + " building=" + construction.buildingType().name()
            + " tile=[" + tile.getX() + "," + tile.getY() + "]"
            + " stoodStillFor=" + unit.lastPositionChangedAgo() + "f"
            + " || worker: canCommand=" + builder.canCommand()
            + " isCompleted=" + builder.isCompleted()
            + " isLockedDown=" + builder.isLockedDown()
            + " isConstructing=" + builder.isConstructing()
            + " isMoving=" + builder.isMoving()
            + " isInterruptible=" + builder.isInterruptible()
            + " order=" + builder.getOrder()
            + " lastCommand=" + unit.lastCommandName()
            + " lastCommandIssuedAgo=" + unit.lastCommandIssuedAgo()
            + " distToBuild=" + A.digit(unit.distTo(at))
            + " || reachability: unitHasPath=" + builder.hasPath(siteCenter)
            + " gameHasPath=" + atlantis.Atlantis.game().hasPath(builder.getPosition(), siteCenter)
            + " ourPort=" + atlantis.map.ReachableTiles.between(unit, at)
            + " || buildability: noBuilder=" + atlantis.Atlantis.game().canBuildHere(tile, type)
            + " withBuilder=" + atlantis.Atlantis.game().canBuildHere(tile, type, builder)
            + " withBuilderExplored=" + atlantis.Atlantis.game().canBuildHere(tile, type, builder, true)
            + " canBuild=" + builder.canBuild(type, tile)
            + " canBuild000=" + builder.canBuild(type, tile, false, false, false)
            + " canBuild100=" + builder.canBuild(type, tile, true, false, false)
            + " canBuild010=" + builder.canBuild(type, tile, false, true, false)
            + " canBuild001=" + builder.canBuild(type, tile, false, false, true)
            + " explored=" + exploredFootprint(construction)
            + " canAfford=" + A.canAfford(
                construction.buildingType().mineralPrice(), construction.buildingType().gasPrice())
            + " minerals=" + A.minerals()
            + " supply=" + A.supplyUsed() + "/" + A.supplyTotal()
            + " || tileRuleSplit: hasPower=" + atlantis.Atlantis.game().hasPower(tile, type)
            + " unitsInRect=" + unitsInFootprint(builder, type, tile)
            + " terrainOnly=" + terrainFootprint(construction)
        );
    }

    /**
     * What stands on the building's pixel footprint, builder included: if this
     * is non-empty while the tile rule refuses, the blocker is occupancy (and
     * the first entry names it); if it is empty, the refusal is terrain, creep
     * or power. Format per unit: {@code id:typeName:BUILDER?}.
     */
    private static String unitsInFootprint(bwapi.Unit builder, bwapi.UnitType type, TilePosition tile) {
        bwapi.Position center = tile.toPosition().add(type.tileSize().toPosition().divide(2));
        java.util.List<bwapi.Unit> units = atlantis.Atlantis.game().getUnitsInRectangle(
            center.subtract(new bwapi.Position(type.dimensionLeft(), type.dimensionUp())),
            center.add(new bwapi.Position(type.dimensionRight(), type.dimensionDown())),
            u -> !u.isFlying() && !u.isLoaded()
        );

        StringBuilder result = new StringBuilder();
        result.append(units.size()).append("[");
        for (int i = 0; i < units.size(); i++) {
            bwapi.Unit unit = units.get(i);
            if (i > 0) result.append(",");
            result.append(unit.getID()).append(":").append(unit.getType())
                .append(unit == builder ? ":BUILDER" : "");
        }
        return result.append("]").toString();
    }

    /**
     * Terrain-only footprint answer (no units, no power): separates terrain
     * from everything the tile rule adds on top.
     */
    private static String terrainFootprint(Construction construction) {
        APosition at = construction.buildPosition();
        if (at == null) return "no-position";

        int bad = 0;
        StringBuilder which = new StringBuilder();
        int w = construction.buildingType().getTilesWidth();
        int h = construction.buildingType().getTilesHeights();

        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                APosition tile = APosition.create(at.tx() + dx, at.ty() + dy);
                if (!tile.isWalkable() || !atlantis.Atlantis.game().isBuildable(tile.tx(), tile.ty())) {
                    bad++;
                    which.append("[").append(tile.tx()).append(",").append(tile.ty()).append("]");
                }
            }
        }

        return (bad == 0 ? "ok" : "BAD:" + bad) + which;
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
