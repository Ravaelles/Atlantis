package atlantis.cherryvis.simple;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.TreeMap;
import java.util.Map;

public class ACherryVisUnitLogger {
    /** Unit ID -> frame -> Active manager */
    public Map<Integer, Map<Integer, String>> unitsManagerLogs = new TreeMap<>();
//    public Map<Integer, Map<Integer, String>> unitsTooltips = new TreeMap<>();

    protected void managerLog(String message, AUnit unit) {
        if (!unit.isOur()) return;

        // listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);

        unitsManagerLogs.computeIfAbsent(unit.id(), k -> new TreeMap<>()).put(A.now, message);

//        ArrayList<String> managerLogs = unitsManagerLogs.get(unit);
//        if (managerLogs == null) {
//            managerLogs = new ArrayList<>(100);
//
//            this.unitsManagerLogs.put(unit, managerLogs);
//        }
//
//        managerLogs.add(message);
    }

//    protected void tooltip(String tooltip, AUnit unit) {
//        if (!unit.isOur()) return;
//
//        unitsTooltips.computeIfAbsent(unit.id(), k -> new TreeMap<>()).put(A.now, tooltip);
//    }
}
