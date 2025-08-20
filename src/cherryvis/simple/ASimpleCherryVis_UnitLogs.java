package cherryvis.simple;

import atlantis.game.A;

import java.util.Map;
import java.util.Set;

public class ASimpleCherryVis_UnitLogs {
    /**
     * "units_updates": {
     *   "123": {
     *     "4500": {
     *       "manager": "CombatManager",
     *       "state": "Kiting",
     *       "target_unit": 567
     *     }
     *   }
     * }
     */
    public static String build(ASimpleCherryVisUnitLogger logger) {
        StringBuilder result = new StringBuilder();

        Set<Integer> unitIds = logger.unitsManagerLogs.keySet();
        int counterForIds = 0;
        for (int unitId : unitIds) {
            String commaForIds = counterForIds == 0 ? "" : ",";
            StringBuilder unitString = new StringBuilder(commaForIds + "\"" + unitId + "\":{");

            Map<Integer, String> managerLogs = logger.unitsManagerLogs.get(unitId);
            Map<Integer, String> tooltips = logger.unitsTooltips.get(unitId);

            int counterForFrames = 0;
            for (int frame : managerLogs.keySet()) {
                String manager = managerLogs.get(frame);
                String tooltip = tooltips == null ? "" : tooltips.getOrDefault(frame, "");

                String commaForFrames = counterForFrames == 0 ? "" : ",";
                String frameString = commaForFrames
                    + "\"" + frame + "\":"
                    + "{\"manager\":\"" + manager + "\",\"tooltip\":\"" + tooltip + "\"}";
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
