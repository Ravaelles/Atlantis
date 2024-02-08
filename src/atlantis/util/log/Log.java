package atlantis.util.log;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;

public class Log {
    /**
     * Write every tooltip to logs/units/unit_file.txt, so it's possible to debug things.
     */
    public static final int SAVE_UNIT_LOGS_TO_FILES = 0; // 0 - Off
//    public static final int SAVE_UNIT_LOGS_TO_FILES = 1; // 1 - Log our combat units

    /**
     * Helpful for logging of <b>unitAction</b> changes. Very helpful to get human-readable unit reasoning.
     */
    public static boolean logUnitActionChanges = false;
//    public static boolean logUnitActionChanges = true;

    public static final int UNIT_LOG_SIZE = 3;
    public static final int UNIT_LOG_EXPIRE_AFTER_FRAMES = 4;

    private ArrayList<LogMessage> messages = new ArrayList<>();
    private int expireAfterFrames;
    private int limit;

    // =========================================================

    public Log(int expireAfterFrames, int limit) {
        this.expireAfterFrames = expireAfterFrames;
        this.limit = limit;
    }

    // =========================================================

    public void addMessage(String message, AUnit unit) {
        messages.add(new LogMessage(message, expireAfterFrames));
        if (SAVE_UNIT_LOGS_TO_FILES > 0) {
            LogUnitsToFiles.saveUnitLogToFile(message, unit);
        }


        if (messages.size() > limit) {
            messages.remove(0);
        }
    }

    public ArrayList<LogMessage> messages() {
        if (A.everyNthGameFrame(expireAfterFrames)) {
            removeOldMessages();
        }

        return messages;
    }

    public boolean lastMessageWas(String message) {
        return messages.size() > 0 && lastMessage().message().equals(message);
    }

    private LogMessage lastMessage() {
        if (messages.isEmpty()) {
            return null;
        }

        return messages.get(messages.size() - 1);
    }

    public boolean isNotEmpty() {
        return !messages.isEmpty();
    }

    // =========================================================

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Log{\n");

        for (LogMessage logMessage : messages) {
            result.append("    ").append(logMessage.messageWithTime()).append(",\n");
        }

        return result + "}";
    }

    // =========================================================

    private void removeOldMessages() {
        messages.removeIf(LogMessage::expired);
    }
}
