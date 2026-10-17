package atlantis.map;

import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.WalkPosition;
import bwem.BWEM;
import bwem.ChokePoint;

/**
 * Ground (walking) distance between two positions, from BWEM area paths.
 *
 * <p>
 * Ported body-for-body from the one live {@code jbweb} helper this project
 * still used ({@code JBWEB.getGroundDistance} + its three private helpers):
 * choke-node walk along {@code BWEM.getMap().getPath}, same fallbacks, same
 * numbers. What changed is ownership: stateless pure functions taking
 * ({@code game}, {@code bwem}) explicitly instead of a static hub with
 * game-wide mutable grids, so the caller - not a library - owns the inputs.
 * Unreachable pairs answer {@code Double.MAX_VALUE}, exactly as before;
 * callers keep their Euclidean fallback.
 * </p>
 */
public final class GroundDistance {

    private GroundDistance() {
    }

    public static double between(Game game, BWEM bwem, Position s, Position e) {
        Position start = new Position(s);
        Position end = new Position(e);
        double dist = 0.0;
        Position last = start;

        if (!start.isValid(game) || !end.isValid(game)) {
            return Double.MAX_VALUE;
        }

        if (bwem.getMap().getArea(new WalkPosition(start)) == null) {
            start = validatePoint(game, bwem, new WalkPosition(start));
        }
        if (bwem.getMap().getArea(new WalkPosition(end)) == null) {
            end = validatePoint(game, bwem, new WalkPosition(end));
        }

        if (!start.isValid(game) || !end.isValid(game) || bwem.getMap().getArea(new WalkPosition(start)) == null ||
            bwem.getMap().getArea(new WalkPosition(end)) == null ||
            !bwem.getMap().getArea(new WalkPosition(start)).isAccessibleFrom((bwem.getMap().getArea(new WalkPosition(end))))) {
            return Double.MAX_VALUE;
        }

        boolean first = true;
        for (ChokePoint cpp : bwem.getMap().getPath(start, end)) {
            boolean large = cpp.getNodePosition(ChokePoint.Node.END1).getDistance(cpp.getNodePosition(ChokePoint.Node.END2)) > 40;
            Position next = first && !large ? accurateClosestNode(bwem, cpp, start) : fastClosestNode(cpp, last);
            dist += next.getDistance(last);
            last = next;
            first = false;
        }

        return dist + last.getDistance(end);
    }

    private static Position validatePoint(Game game, BWEM bwem, WalkPosition w) {
        double distBest = 0.0;
        Position posBest = new Position(w);
        for (int x = w.x - 1; x < w.x + 1; x++) {
            for (int y = w.y - 1; y < w.y + 1; y++) {
                WalkPosition w2 = new WalkPosition(x, y);
                if (!w.isValid(game) || bwem.getMap().getArea(w2) == null) {
                    continue;
                }

                Position p = new Position(w);
                double dist = p.getDistance(bwem.getMap().getCenter());
                if (dist > distBest) {
                    distBest = dist;
                    posBest = p;
                }
            }
        }
        return posBest;
    }

    private static Position fastClosestNode(ChokePoint cp, Position last) {
        Position n1 = new Position(cp.getNodePosition(ChokePoint.Node.END1));
        Position n2 = new Position(cp.getNodePosition(ChokePoint.Node.END2));
        Position n3 = new Position(cp.getCenter());

        double d1 = n1.getDistance(last);
        double d2 = n2.getDistance(last);
        double d3 = n3.getDistance(last);

        return d1 < d2 ? (d1 < d3 ? n1 : n3) : (d2 < d3 ? n2 : n3);
    }

    private static Position accurateClosestNode(BWEM bwem, ChokePoint cp, Position start) {
        return getClosestChokeTile(bwem, cp, start);
    }

    private static Position getClosestChokeTile(BWEM bwem, ChokePoint cp, Position start) {
        double best = Double.MAX_VALUE;
        Position posBest = Position.Invalid;
        for (WalkPosition geo : cp.getGeometry()) {
            if (bwem.getMap().getArea(geo) == null) continue;
            TilePosition tile = geo.toTilePosition();
            Position p = new Position(tile.toPosition().x + 16, tile.toPosition().y + 16);
            double dist = p.getDistance(start);
            if (dist < best) {
                posBest = p;
                best = dist;
            }
        }
        return posBest;
    }
}
