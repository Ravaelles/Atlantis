package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.player.Enemy;
import atlantis.map.analysis.WallAnchors;
import atlantis.map.analysis.WallPieces;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.orders.build.BuildOrderOwnsGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

import java.util.List;

/**
 * Wall-in at the natural: Pylon + Gateway + cannons on choke anchors.
 *
 * <p>
 * The shape Stardust and PurpleWave both play, firing after the core is up
 * against Zerg (the rush window), never while the opening owns the game.
 * Pieces come from {@code WallPieces} (fit-checked, door excluded); each is
 * requested near its anchor tile through the sited funnel, so coverage
 * ranking and the twin apply. Idempotent from live state: a piece already
 * standing or coming near its tile is skipped, so cancellations re-fire
 * correctly and duplicates never queue. A choke too wide to plug refuses
 * inside {@code WallPieces} - then cannons-as-spots (fortification) are the
 * answer, never a gappy wall.
 * </p>
 */
public class ProduceNaturalWallIn {

    public static boolean produce() {
        if (!We.protoss()) return false;
        // The board owns the WHEN (WallInGoal): fresh rankings decide, stale
        // ones fall back to the legacy trigger below. Opening ownership stays
        // an execution guard either way - rows are cheap, builders are not.
        if (atlantis.production.GoalBoardFeeder.boardFresh()
            && !atlantis.production.GoalBoardFeeder.wantedGoal("wall-in")) return false;
        if (BuildOrderOwnsGame.owns()) return false;
        if (!Enemy.zerg()) return false;
        if (!Have.cyberneticsCore()) return false;

        AChoke choke = Chokes.natural();
        APosition natural = DefineNaturalBase.natural();
        if (choke == null || natural == null) return false;
        if (choke.firstPoint() == null || choke.lastPoint() == null) return false;

        WallAnchors.AnchorRow row = WallAnchors.slots(choke.firstPoint(), choke.lastPoint());
        if (row.slots().isEmpty() || row.door() == null) return false;

        List<WallPieces.Piece> pieces = WallPieces.plan(
            row.slots(), row.door(), natural, choke.width(), 2);
        if (pieces.isEmpty()) return false;

        boolean ordered = false;
        for (WallPieces.Piece piece : pieces) {
            if (Count.existingOrPlannedBuildingsNear(piece.type(), 6, piece.tile()) > 0) continue;
            if (requestExact(piece)) ordered = true;
        }
        return ordered;
    }

    /**
     * The piece tile is already decided ({@code WallPieces}); searching
     * again would only move it. Queue it exact - a free tile is honored
     * verbatim, an occupied one falls back to a fresh search downstream
     * ({@code DefineExactPositionForNewConstruction}), same as any exact
     * order. Flows through the funnel (adapter EXACT constraint +
     * validation + power + twin), so the tile stays attributable.
     */
    private static boolean requestExact(WallPieces.Piece piece) {
        if (!piece.tile().isBuildableIncludeBuildings()) return false;
        ProductionOrder order = AddToQueue.withTopPriority(piece.type(), piece.tile());
        if (order == null) return false;
        order.setAroundPosition(piece.tile());
        order.markAsUsingExactPosition();
        order.setMinSupply(0);
        return NewConstructionRequest.requestConstructionOf(piece.type(), piece.tile(), order) != null;
    }
}
