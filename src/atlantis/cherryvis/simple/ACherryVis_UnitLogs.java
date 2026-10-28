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
     * }
     *
     * <p>One row per frame where the manager OR the behavior tag changed
     * (union, ascending); the tooltip carries forward, so the Task slot
     * always shows the current behavior, including for army units that
     * never have a manager row. Empty keys are omitted (never a ""
     * legend entry) - a tooltip-only row is just {"tooltip":id}.</p>
     */
    public static String build(ACherryVisUnitLogger logger, ACherryVis_Strings strings) {
        StringBuilder result = new StringBuilder();

        Set<Integer> unitIds = new java.util.TreeSet<>();
        unitIds.addAll(logger.unitsManagerLogs.keySet());
        unitIds.addAll(logger.unitsTooltips.keySet());
        int counterForIds = 0;
        for (int unitId : unitIds) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unitId + "\":{");

            Map<Integer, String> managerLogs = logger.unitsManagerLogs.get(unitId);
            Map<Integer, String> tooltips = logger.unitsTooltips.get(unitId);

            java.util.TreeSet<Integer> frames = new java.util.TreeSet<>();
            if (managerLogs != null) frames.addAll(managerLogs.keySet());
            if (tooltips != null) frames.addAll(tooltips.keySet());

            String carriedTooltip = "";
            int counterForFrames = 0;
            for (int frame : frames) {
                String manager = managerLogs == null ? "" : managerLogs.getOrDefault(frame, "");
                if (tooltips != null && tooltips.containsKey(frame)) {
                    carriedTooltip = tooltips.get(frame);
                }

                // Map strings to integer IDs; empty keys are omitted so the
                // legend never carries a "" entry (pinned by the legend test).
                StringBuilder row = new StringBuilder(
                    (counterForFrames == 0 ? "" : ",") + "\"" + frame + "\":{");
                boolean needsComma = false;
                if (!manager.isEmpty()) {
                    row.append("\"manager\":").append(strings.get(manager));
                    needsComma = true;
                }
                if (!carriedTooltip.isEmpty()) {
                    if (needsComma) row.append(",");
                    row.append("\"tooltip\":").append(strings.get(carriedTooltip));
                }
                row.append("}");
                unitString.append(row);

                counterForFrames++;
            }

            unitString.append("}");
            result.append(unitString);

            counterForIds++;
        }

        return result.toString();
    }
}
