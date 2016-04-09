package atlantis.debug.tooltip;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import java.util.HashMap;

public class TooltipManager {

    private static HashMap<AUnit, Tooltip> tooltips = new HashMap<>();

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
    public static void setTooltip(AUnit unit, String tooltip) {
        tooltips.put(unit, new Tooltip(unit, tooltip));
//        if (unit.getType().equals(AUnitType.Terran_Marine)) {
//            System.out.println("--set: " + tooltip + " // " + tooltips.get(unit)); //TODO debug
//        }
    }

    /**
     * Returns the tooltip string associated with a given unit
     *
     * @param unit
     * @return
     */
    public static String getTooltip(AUnit unit) {
        if (!tooltips.containsKey(unit)) {
            return null;
        }

        if (unit.getType().equals(AUnitType.Terran_Marine)) {
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
    public static Tooltip getTooltipObject(AUnit unit) {
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
    public static void removeTooltip(AUnit unit) {
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
    public static boolean hasTooltip(AUnit unit) {
        if (!tooltips.containsKey(unit)) {
            return false;
        }

        return tooltips.get(unit).hasTooltip();
    }
}
