package atlantis.debug.tooltip;

import java.util.HashMap;

import atlantis.util.UnitUtil;
import bwapi.Unit;
import bwapi.UnitType;

public class TooltipManager {

    private static HashMap<Unit, Tooltip> tooltips = new HashMap<>();

    // =========================================================
    
    private TooltipManager() {
    }

    // =========================================================
    
    /**
     * Sets the tooltip for a given unit
     *
     * @param unit
     * @param tooltip
     * @return
     */
    public static void setTooltip(Unit unit, String tooltip) {

        tooltips.put(unit, new Tooltip(unit, tooltip));
        if (unit.getType().equals(UnitType.Terran_Marine)) {
            System.out.println("--set: " + tooltip + " // " + tooltips.get(unit)); //TODO debug
        }

    }

    /**
     * Returns the tooltip string associated with a given unit
     *
     * @param unit
     * @return
     */
    public static String getTooltip(Unit unit) {
        if (!tooltips.containsKey(unit)) {
            return null;
        }

        if (unit.getType().equals(UnitType.Terran_Marine)) {
            System.out.println("--get: " + tooltips.get(unit)); //TODO debug
        }

        return tooltips.get(unit).getTooltip();

    }

    /**
     * Returns the Tooltip object associated with a given unit
     *
     * @param unit
     * @return
     */
    public static Tooltip getTooltipObject(Unit unit) {
        if (!tooltips.containsKey(unit)) {
            return null;
        }
        //System.out.println("--get: " + tooltips.get(unit)); //TODO debug
        return tooltips.get(unit);
    }

    /**
     * Removes the tooltip associated with a unit
     *
     * @param unit
     * @return
     */
    public static void removeTooltip(Unit unit) {
        if (!tooltips.containsKey(unit)) {
            return;
        }
        //System.out.println("--remove: " + tooltips.get(unit)); //TODO debug
        tooltips.get(unit).removeTooltip();

    }

    /**
     *
     * @param unit
     * @return
     */
    public static boolean hasTooltip(Unit unit) {
        if (!tooltips.containsKey(unit)) {
            return false;
        }

        return tooltips.get(unit).hasTooltip();
    }
}
