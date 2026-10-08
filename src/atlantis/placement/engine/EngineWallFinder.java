package atlantis.placement.engine;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.placement.core.ForgeGatewayWall;
import atlantis.placement.core.TileAvailabilityGrid;

import java.util.ArrayList;
import java.util.List;

/**
 * The engine side of the wall strategy: builds a
 * {@link ForgeGatewayWall.Choke} from the map's chokes and asks the core for a
 * wall (`_AI/redesign/03_PLACEMENT.md` §5.4 S4).
 *
 * <p>
 * The choke's own tiles come from its geometry - the tiles it spans - so the
 * wall
 * always includes the ground it exists to seal, not just the area around it.
 * </p>
 */
public final class EngineWallFinder {

    private final TileAvailabilityGrid grid;
    private final ForgeGatewayWall wall;

    public EngineWallFinder(TileAvailabilityGrid grid) {
        this.grid = grid;
        this.wall = new ForgeGatewayWall(grid);
    }

    /** The main choke's wall, or null when none fits. */
    public ForgeGatewayWall.Tiles forMainChoke() {
        return wall.find(choke(Chokes.mainChoke()));
    }

    /** The natural's wall, or null when none fits. */
    public ForgeGatewayWall.Tiles forNaturalChoke() {
        return wall.find(choke(Chokes.natural()));
    }

    private static ForgeGatewayWall.Choke choke(AChoke choke) {
        if (choke == null || choke.center() == null)
            return null;

        List<int[]> tiles = new ArrayList<>();
        for (int x = choke.tx() - 2; x <= choke.tx() + 2; x++) {
            for (int y = choke.ty() - 2; y <= choke.ty() + 2; y++) {
                tiles.add(new int[] { x, y });
            }
        }

        return new ForgeGatewayWall.Choke(choke.tx(), choke.ty(), tiles);
    }
}
