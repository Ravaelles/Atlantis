package atlantis.util.log;

import bwapi.Color;

/**
 * One line in a {@link Log}: the message plus the two timestamps it expires
 * against, and nothing else.
 *
 * <p>It used to live in {@code atlantis.debug.tools} and read the game clock
 * itself ({@code A.now()}, {@code A.realSecondsNow()}), which is why the logging
 * kernel in {@code atlantis.util} pointed sideways into {@code atlantis.debug}:
 * ten frozen violations, all of them {@code Log -> debug.tools.LogMessage}. The
 * clock reads are gone - "now" arrives from the caller, which is a class that
 * already reads it - so the class can sit next to the only thing that owns it.</p>
 *
 * <p>Two expiry modes, unchanged: a game-frame budget for unit tooltips, and a
 * real-seconds budget for everything else.</p>
 */
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

    /**
     * Expires {@code expireAfterFrames} game frames after {@code createdAtFrames}.
     */
    public LogMessage(String message, int expireAfterFrames, int createdAtFrames) {
        this.message = message;
        this.expireAfterRealSeconds = false;
        this.expireAfterFrames = expireAfterFrames;
        this.createdAtFrames = createdAtFrames;
    }

    /**
     * Expires {@link #TIME_TO_LIVE} real seconds after {@code createdAtRealSeconds}.
     */
    public LogMessage(String message, long createdAtRealSeconds) {
        this.message = message;
        this.expireAfterRealSeconds = true;
        this.createdAtRealTime = createdAtRealSeconds;
    }

    public String message() {
        return message;
    }

    public String messageWithTime() {
        return createdAtFrames + ": " + message;
    }

    public int createdFramesAgo(int nowFrames) {
        return nowFrames - createdAtFrames;
    }

    public long createdRealSecondsAgo(long nowRealSeconds) {
        return nowRealSeconds - createdAtRealTime;
    }

    public boolean expired(int nowFrames, long nowRealSeconds) {
        if (!expireAfterRealSeconds) {
            return createdFramesAgo(nowFrames) >= expireAfterFrames;
        }

        return createdRealSecondsAgo(nowRealSeconds) >= TIME_TO_LIVE;
    }

    public Color color(long nowRealSeconds) {
        long secondsAgo = createdRealSecondsAgo(nowRealSeconds);

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

    public void setMessage(String newMessage) {
        this.message = newMessage;
    }
}