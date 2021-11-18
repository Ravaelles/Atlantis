package atlantis.log;

import atlantis.util.A;
import bwapi.Color;

public class LogMessage {

    /**
     * In real seconds.
     */
    private static final int TIME_TO_LIVE = 6;

    private String message;
    private long createdAt;

    public LogMessage(String message) {
        this.message = message;
        this.createdAt = A.realSecondsNow();
    }

    public String message() {
        return message;
    }

    public long createdRealSecondsAgo() {
        return A.realSecondsNow() - createdAt;
    }

    public boolean expired() {
        return createdRealSecondsAgo() >= TIME_TO_LIVE;
    }

    public Color color() {
        long secondsAgo = createdRealSecondsAgo();

        if (secondsAgo <= 1) {
            return Color.Green;
        }
        else if (secondsAgo <= 2) {
            return Color.Yellow;
        }
        else if (secondsAgo <= 4) {
            return Color.White;
        }
        else if (secondsAgo <= 5) {
            return Color.Grey;
        }
        else {
            return Color.Black;
        }
    }
}
