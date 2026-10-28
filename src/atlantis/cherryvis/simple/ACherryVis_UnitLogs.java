package atlantis.cherryvis.simple;

import java.util.Map;
import java.util.Set;

public class ACherryVis_UnitLogs {
    /**
     * "units_logs": {
     * "123": [{"frame":4500,"message":"Build issued"}, ...],
     * "124": [...]
     * }
     *
     * <p>Unit ids sort ascending (TreeMap); entries keep insertion (frame) order.</p>
     */
    public static String buildUnitLogs(ACherryVisUnitLogger logger) {
        StringBuilder result = new StringBuilder();

        boolean firstUnit = true;
        for (Map.Entry<Integer, java.util.List<ACherryVisUnitLogger.UnitLogEntry>> unit
            : logger.unitsFreetextLogs.entrySet()) {
            if (!firstUnit) {
                result.append(",");
            }
            firstUnit = false;

            result.append("\"").append(unit.getKey()).append("\":[");

            boolean firstEntry = true;
            for (ACherryVisUnitLogger.UnitLogEntry entry : unit.getValue()) {
                if (!firstEntry) {
                    result.append(",");
                }
                firstEntry = false;
                result.append(entry.toJson());
            }
            result.append("]");
        }

        return result.toString();
    }

    /**
     * "units_updates": {
     * "123": {
     * "4500": {
     * "manager": "CombatManager",
     * "tooltip": "Kiting",
     * "state": "Kiting",
     * "target_unit": 567
     * }
     * }
     */
    public static String build(ACherryVisUnitLogger logger, ACherryVis_Strings strings) {
        StringBuilder result = new StringBuilder();

        Set<Integer> unitIds = logger.unitsManagerLogs.keySet();
        int counterForIds = 0;
        for (int unitId : unitIds) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unitId + "\":{");

            Map<Integer, String> managerLogs = logger.unitsManagerLogs.get(unitId);

            int counterForFrames = 0;
            for (int frame : managerLogs.keySet()) {
                String manager = managerLogs.get(frame);

                // Map strings to integer IDs
                int managerId = strings.get(manager);

                String commaForFrames = counterForFrames == 0 ? "" : ",";
                String frameString = commaForFrames
                    + "\"" + frame + "\":"
                    + "{\"manager\":" + managerId + "}";
                unitString.append(frameString);

                counterForFrames++;
            }

            unitString.append("}");
            result.append(unitString);

            counterForIds++;
        }

        return result.toString();
    }

    /**
     * "units_tooltips": {"123":[[4500,10001],[4600,10002]]} - one
     * [frame, tooltipId] pair per behavior transition (NEXT #221, owner:
     * grouped-by-key JSON, minimal info). The legend resolves the ids
     * through the same runtime table as managers. Change-only upstream
     * ({@code ACherryVisUnitLogger.tooltip} dedups per unit), gated by
     * {@code CHERRYVIS_TOOLTIPS} (default off: flapping callers wrote 114k
     * rows in one game).
     */
    public static String buildTooltips(ACherryVisUnitLogger logger, ACherryVis_Strings strings) {
        StringBuilder result = new StringBuilder();

        int counterForIds = 0;
        for (Map.Entry<Integer, Map<Integer, String>> unit
            : logger.unitsTooltips.entrySet()) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unit.getKey() + "\":[");

            int counterForFrames = 0;
            for (Map.Entry<Integer, String> row : unit.getValue().entrySet()) {
                String commaForFrames = counterForFrames == 0 ? "" : ",";
                unitString.append(commaForFrames
                    + "[" + row.getKey() + "," + strings.get(row.getValue()) + "]");
                counterForFrames++;
            }

            unitString.append("]");
            result.append(unitString);

            counterForIds++;
        }

        return result.toString();
    }
}
