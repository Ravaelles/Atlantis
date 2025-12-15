package atlantis.cherryvis.simple;

import java.util.Map;
import java.util.Set;

public class ACherryVis_UnitLogs {
    /**
     * "units_updates": {
     * "123": {
     * "4500": {
     * "manager": "CombatManager",
     * "state": "Kiting",
     * "target_unit": 567
     * }
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
//            Map<Integer, String> tooltips = logger.unitsTooltips.get(unitId);

            int counterForFrames = 0;
            for (int frame : managerLogs.keySet()) {
                String manager = managerLogs.get(frame);
//                String tooltip = tooltips == null ? "" : tooltips.getOrDefault(frame, "");

                // Map strings to integer IDs
                int managerId = strings.get(manager);
//                int tooltipId = strings.get(tooltip);

                String commaForFrames = counterForFrames == 0 ? "" : ",";
                String frameString = commaForFrames
                        + "\"" + frame + "\":"
//                        + "{\"manager\":" + managerId + ",\"tooltip\":" + tooltipId + "}";
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
}
