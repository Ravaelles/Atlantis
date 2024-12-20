package atlantis.game.event;

import atlantis.game.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
    private static final Map<String, List<Listener>> listeners = new HashMap<>();

    public static void dispatch(String event, Object... data) {
//        System.err.println(A.minSec() + " EVENT DISPATCHED " + event);

        listenersFor(event).forEach(listener -> listener.onEvent(event, data));
    }

    public static void register(String event, Listener listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    private static List<Listener> listenersFor(String event) {
        return Events.listeners.getOrDefault(event, new ArrayList<>());
    }
}
