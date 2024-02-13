package atlantis.production.orders.production.queue.add;

import atlantis.game.A;
import atlantis.util.cache.Cache;

import java.util.ArrayList;

public class History {
    private Cache<Integer> cacheInt = new Cache<>();
    private ArrayList<String> allEvents = new ArrayList<>();

    public void addNow(String event) {
        cacheInt.set(event, -1, A.now());
        allEvents.add(event);
    }

    public int countEvents(String event) {
        int count = 0;
        for (String e : allEvents) {
            if (e.equals(event)) {
                count++;
            }
        }
        return count;
    }

    public int lastHappenedAgo(String event) {
        if (cacheInt.get(event) == null) return 99998765;

        return A.ago(lastHappenedAt(event));
    }

    private int lastHappenedAt(String event) {
        return cacheInt.get(event);
    }

    public int size() {
        return allEvents.size();
    }

    public String get(int i) {
        return allEvents.get(i);
    }

    public String last() {
        if (allEvents.isEmpty()) return null;

        return allEvents.get(allEvents.size() - 1);
    }
}