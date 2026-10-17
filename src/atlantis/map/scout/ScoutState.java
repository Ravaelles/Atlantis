package atlantis.map.scout;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.units.AUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
     * ({@code atlantis.battle.scout.ScoutUnitManagers}); the
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

    /**
     * Roam timeout (NEXT #78 follow-up): last distance to the current roam
     * point and when progress was last seen. The polygon index used to advance
     * only on arrival (within 1.8) - a scout that can never get there (blocked
     * point, chased loop) returned the same target every frame, and
     * {@code move()} suppresses re-issues to an unchanged target, so it stood
     * still holding a move order. -1 = no point tracked (re-armed on advance).
     */
    public static double roamLastDistToPoint = -1;
    public static int roamLastProgressFrame = 0;

    /**
     * Baseless-visit memory (ScoutEnemyBasesWithoutBase repair, Wine game:
     * the scout re-issued Move to the same base location for 100+ frames -
     * the stateless nearest-pick rotates only by arrival, and a site the
     * scout can stand next to without ever reaching (dist stuck above 3)
     * never counts as arrived). Tile keys the scout has stood on or given
     * up on; the pick skips them, so coverage terminates instead of
     * oscillating A-&gt;B-&gt;A. Per-game, cleared in {@link #reset()} with
     * the roam state. Single scout assumed - same as the roam tracker
     * above (the build order fields exactly one).
     */
    public static final Set<String> baselessVisited = new HashSet<>();
    public static String baselessTrackedKey = null;
    public static int baselessTrackedSince = 0;

    /**
     * Terminal-roam rotation (idle-scout repair): index into the enemy
     * start locations, advanced on every roam pick. Early game (enemy
     * never discovered) the scout cycles 1st/2nd/3rd enemy base in fixed
     * order instead of standing idle; the increment is monotonic per game
     * and cleared here with the rest of the scout state.
     */
    public static int terminalRoamIndex = 0;

    /**
     * Per-game reset for the roam state added with the scout repair.
     * Called from the game root alongside the other per-game resets, so a
     * stale roam target never leaks into the next game in the same JVM. The
     * scouts list itself stays with its existing lifecycle (dead scouts are
     * removed per frame).
     */
    public static void reset() {
        roamLastDistToPoint = -1;
        roamLastProgressFrame = 0;
        baselessVisited.clear();
        baselessTrackedKey = null;
        baselessTrackedSince = 0;
        terminalRoamIndex = 0;
    }
}
