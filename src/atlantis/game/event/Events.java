package atlantis.game.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
    private static final Map<Event, List<AutomaticListener>> listeners = new HashMap<>();

    public static void dispatch(Event event, Object... data) {
//        System.err.println(A.minSec() + " EVENT DISPATCHED " + event);

        listenersFor(event).forEach(listener -> listener.onEvent(event, data));
    }

    public static void register(Event event, AutomaticListener listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    private static List<AutomaticListener> listenersFor(Event event) {
        return Events.listeners.getOrDefault(event, new ArrayList<>());
    }
}
