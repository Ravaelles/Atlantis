package atlantis.map.scout;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;

import java.util.ArrayList;

public class ScoutState {
    /**
     * Current scout units.
     */
    public static final ArrayList<AUnit> scouts = new ArrayList<>();

    /**
     * Which worker number is allowed to become a scout, from the build order's
     * {@code SCOUT_IS_NTH_WORKER} setting.
     *
     * <p>Scouting asks for this every frame, so the number is read once by the
     * game root ({@code OnGameStarted}) instead of by Scouting from the
     * production package - the boundary test forbids {@code map.scout..} from
     * depending on {@code production..}, and routing the lookup through
     * {@code Strategy} would just move the edge into another rule's baseline.</p>
     */
    public static int scoutIsNthWorker = 9;

    /**
     * Builds the per-unit manager for one scout. The implementation is Combat's
     * ({@code atlantis.combat.squad.positioning.scout.ScoutUnitManagers}); the
     * game root sets it once, because Scouting must not name a Combat class.
     *
     * @see ScoutUnitManager
     */
    public static ScoutUnitManager scoutUnitManager = unit -> {
        throw new IllegalStateException("ScoutUnitManager was never wired - is the game root running?");
    };

    //    public boolean MAKE_CAMERA_FOLLOW_unit_AROUND_BASE = true;
    public static boolean MAKE_CAMERA_FOLLOW_unit_AROUND_BASE = false;

    public static Positions<ARegionBoundary> scoutingAroundBasePoints = new Positions<>();
    public static int scoutsKilledCount = 0;
    public static int unitingAroundBaseNextPolygonIndex = -1;
    public static HasPosition unitingAroundBaseLastPolygonPoint = null;
    public static boolean scoutingAroundBaseWasInterrupted = false;
    public static boolean unitingAroundBaseDirectionClockwise = true;
    public static HasPosition nextPositionToScout = null;
    public static HasPosition nextPositionToScout2 = null;
    public static ARegion enemyBaseRegion = null;
}
