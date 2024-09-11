package atlantis.production.orders.production.queue.add;

import atlantis.game.A;
import atlantis.util.cache.Cache;

import java.util.ArrayList;

public class History {
    private Cache<Integer> cacheInt = new Cache<>();
    private ArrayList<String> allEvents = new ArrayList<>();
    private ArrayList<Integer> allEventsTimestamps = new ArrayList<>();

    public void addNow(String event) {
        cacheInt.set(event, -1, A.now());
        allEvents.add(event);
        allEventsTimestamps.add(A.now());
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

    public void clear() {
        allEvents.clear();
        allEventsTimestamps.clear();
        cacheInt.clear();
    }

    public boolean lastHappenedLessThanAgo(String event, int maxFramesAgo) {
        return lastHappenedAgo(event) <= maxFramesAgo;
    }

    public boolean lastHappenedLessThanSecondsAgo(String event, int maxSecondsAgo) {
        return lastHappenedAgo(event) <= 30 * maxSecondsAgo;
    }

    public int countInLastSeconds(String event, int includeSecondsAgo) {
        int maxSecondsAgo = includeSecondsAgo * 30;
        int minHistoryIndex = Math.max(0, allEvents.size() - 40);

        int count = minHistoryIndex;
        for (int i = allEvents.size() - 1; i >= minHistoryIndex; i--) {
            String pastEvent = allEvents.get(i);

            if (pastEvent.equals(event)) {
                if (A.ago(allEventsTimestamps.get(i)) <= maxSecondsAgo) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        return count;
    }
}
