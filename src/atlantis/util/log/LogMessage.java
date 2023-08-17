package atlantis.util.log;

import atlantis.game.A;
import bwapi.Color;

public class LogMessage {

    /**
     * In real seconds.
     */
    private static final int TIME_TO_LIVE = 6;

    private String message;
    private int expireAfterFrames;
    private boolean expireAfterRealSeconds;
    private int createdAtFrames;
    private long createdAtRealTime;

    public LogMessage(String message) {
        this.message = message;
        this.expireAfterRealSeconds = true;
        this.createdAtRealTime = A.realSecondsNow();
    }

    public LogMessage(String message, int expireAfterFrames) {
        this.message = message;
        this.expireAfterRealSeconds = false;
        this.expireAfterFrames = expireAfterFrames;
        this.createdAtFrames = A.now();
    }

    public String message() {
        return message;
    }

    public int createdFramesAgo() {
        return A.ago(createdAtFrames);
    }

    public long createdRealSecondsAgo() {
        return A.realSecondsNow() - createdAtRealTime;
    }

    public boolean expired() {
        if (!expireAfterRealSeconds) {
            return (createdFramesAgo() >= expireAfterFrames);
        }

        return (createdRealSecondsAgo() >= TIME_TO_LIVE);
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

    public int createdAtFrames() {
        return createdAtFrames;
    }

    @Override
    public String toString() {
        return message;
    }
}
