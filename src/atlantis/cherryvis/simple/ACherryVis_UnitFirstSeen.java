package atlantis.cherryvis.simple;

import atlantis.cherryvis.ACherryVis;
import atlantis.game.A;
import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.units.AUnit;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ACherryVis_UnitFirstSeen extends AutomaticListener {
    //    private static Set<Integer> knownUnitIds = new HashSet<>();
//    private static Map<Integer, List<Map<String, Integer>>> unitsByFrame = new TreeMap<>();
    private static Map<Integer, Integer[]> unitsSeenAt = new TreeMap<>();

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

        boolean firstFrame = true;
        int i = 0;
        int total = unitsSeenAt.size();
        Set<Integer> frames = unitsSeenAt.keySet();
        for (Integer frame : frames) {
            if (!firstFrame) {
                result.append(",");
            }
            firstFrame = false;

            result.append("\"").append(frame).append("\":");

//            List<Map<String, Integer>> units = unitsSeenAt.get(frame);
//            for (int i = 0; i < units.size(); i++) {
//                Map<String, Integer> props = unitsSet.get(i);
            Integer[] record = unitsSeenAt.get(i);
            result.append("{");
            result.append("\"id\":").append(record[0]).append(",");
            result.append("\"type\":").append(record[1]).append(",");
            result.append("\"x\":").append(record[2]).append(",");
            result.append("\"y\":").append(record[3]);
            result.append("}");
//            if (i < total - 1) {
//                result.append(",");
//            }
//            }
//            result.append("}");
        }

        return result.toString();
    }

    // =========================================================

    @Override
    public Event listensTo() {
        return Event.UNIT_DISCOVERED;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        if (!ACherryVis.isEnabled()) return;

        AUnit unit = (AUnit) data[0];

        if (!unit.isNeutral() && !unitsSeenAt.containsKey(unit.id())) {
//            Map<String, Integer> props = new HashMap<>();
//            props.put("id", unit.id());
//            props.put("type", unit.type().id());
//            props.put("x", unit.x());
//            props.put("y", unit.y());

//            int now = A.now;
//            if (!unitsByFrame.containsKey(now)) {
//                unitsByFrame.put(now, new ArrayList<>());
//            }
//            unitsByFrame.get(now).add(props);

            unitsSeenAt.put(A.now, new Integer[]{unit.id(), unit.type().id(), unit.x(), unit.y()});
        }
    }
}
