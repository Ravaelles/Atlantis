package atlantis.cherryvis.simple;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

        // Union of manager-driven units and behavior-tagged units: an army
        // unit with no manager still gets its behavior rows, so the viewer
        // can show what it does (NEXT #221 - Task was None for all of them).
        // Gated like the grouped dump: the flap writes 100k+ rows per game.
        boolean withTooltips = atlantis.config.env.Env.cherryVisTooltips();
        Set<Integer> unitIds = new TreeSet<>(logger.unitsManagerLogs.keySet());
        if (withTooltips) unitIds.addAll(logger.unitsTooltips.keySet());
        int counterForIds = 0;
        for (int unitId : unitIds) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unitId + "\":{");

            Map<Integer, String> managerLogs = logger.unitsManagerLogs.get(unitId);
            Map<Integer, String> tooltips = withTooltips ? logger.unitsTooltips.get(unitId) : null;

            TreeSet<Integer> frames = new TreeSet<>();
            if (managerLogs != null) frames.addAll(managerLogs.keySet());
            if (tooltips != null) frames.addAll(tooltips.keySet());

            String carriedTooltip = "";
            int counterForFrames = 0;
            for (int frame : frames) {
                String manager = managerLogs == null ? "" : managerLogs.getOrDefault(frame, "");
                if (tooltips != null && tooltips.containsKey(frame)) {
                    carriedTooltip = tooltips.get(frame);
                }

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
