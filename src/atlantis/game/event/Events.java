package atlantis.game.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
    private static final Map<String, List<AutomaticListener>> listeners = new HashMap<>();

    public static void dispatch(String event, Object... data) {
//        System.err.println(A.minSec() + " EVENT DISPATCHED " + event);

        listenersFor(event).forEach(listener -> listener.onEvent(event, data));
    }

    public static void register(String event, AutomaticListener listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    private static List<AutomaticListener> listenersFor(String event) {
        return Events.listeners.getOrDefault(event, new ArrayList<>());
    }
}
