package atlantis.cherryvis.simple;

import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.units.AUnit;

import java.util.*;

public class ACherryVis_UnitFirstSeen extends AutomaticListener {
    private static Set<Integer> knownUnitIds = new HashSet<>();
    private static Map<Integer, List<Map<String, Integer>>> unitsByFrame = new TreeMap<>();

    /**
     * "10203": [
     * {
     * "id": 246,
     * "type": 37,
     * "x": 3744,
     * "y": 3795
     * }
     * ],
     * "10278": [
     * {
     * "id": 249,
     * "type": 35,
     * "x": 3525,
     * "y": 2864
     * }
     * ],
     */
    public static String get() {
        StringBuilder result = new StringBuilder();
        result.append("{");

        boolean firstFrame = true;
        for (Integer frame : unitsByFrame.keySet()) {
            if (!firstFrame) {
                result.append(",");
            }
            result.append("\"").append(frame).append("\":[");

            List<Map<String, Integer>> units = unitsByFrame.get(frame);
            for (int i = 0; i < units.size(); i++) {
                Map<String, Integer> props = units.get(i);
                result.append("{");
                result.append("\"id\":").append(props.get("id")).append(",");
                result.append("\"type\":").append(props.get("type")).append(",");
                result.append("\"x\":").append(props.get("x")).append(",");
                result.append("\"y\":").append(props.get("y"));
                result.append("}");
                if (i < units.size() - 1) {
                    result.append(",");
                }
            }
            result.append("]");
            firstFrame = false;
        }

        result.append("}");

        return result.toString();
    }

    // =========================================================

    @Override
    public Event listensTo() {
        return Event.UNIT_DISCOVERED;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        AUnit unit = (AUnit) data[0];

        if (!unit.isNeutral() && !knownUnitIds.contains(unit.id())) {
            knownUnitIds.add(unit.id());

            Map<String, Integer> props = new HashMap<>();
            props.put("id", unit.id());
            props.put("type", unit.type().id());
            props.put("x", unit.x());
            props.put("y", unit.y());

            int now = A.now;
            if (!unitsByFrame.containsKey(now)) {
                unitsByFrame.put(now, new ArrayList<>());
            }
            unitsByFrame.get(now).add(props);
        }
    }
}
