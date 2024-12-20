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
