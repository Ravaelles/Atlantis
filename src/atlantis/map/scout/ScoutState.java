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
     * Current scout unit.
     */
    static final ArrayList<AUnit> scouts = new ArrayList<>();
    //    public boolean MAKE_CAMERA_FOLLOW_unit_AROUND_BASE = true;
    public static boolean MAKE_CAMERA_FOLLOW_unit_AROUND_BASE = false;
    public static Positions<ARegionBoundary> scoutingAroundBasePoints = new Positions<>();
    public static boolean anyScoutBeenKilled = false;
    protected static int unitingAroundBaseNextPolygonIndex = -1;
    protected static HasPosition unitingAroundBaseLastPolygonPoint = null;
    protected static boolean scoutingAroundBaseWasInterrupted = false;
    protected static boolean unitingAroundBaseDirectionClockwise = true;
    protected static APosition nextPositionToUnit = null;
    protected static ARegion enemyBaseRegion = null;
}
