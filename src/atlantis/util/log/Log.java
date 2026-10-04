package atlantis.util.log;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;
import atlantis.debug.tools.LogUnitsToFiles;

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

    public static final int UNIT_LOG_SIZE = 8;
    public static final int UNIT_LOG_EXPIRE_AFTER_FRAMES = 10;

    private ArrayList<LogMessage> messages = new ArrayList<>();
    private int expireAfterFrames;
    private int limit;

    // =========================================================

    public Log(int expireAfterFrames, int limit) {
        this.expireAfterFrames = expireAfterFrames;
        this.limit = limit;
    }

    // =========================================================

    /**
     * {@code createdAtFrames} is passed in rather than read here: reading the
     * clock is what kept {@code LogMessage} (and through it this class) pointing
     * at {@code atlantis.debug} and {@code atlantis.game}. Callers are unit and
     * construction code, which already know the frame.
     */
    public void addMessage(String message, AUnit unit, int createdAtFrames) {
        messages.add(new LogMessage(message, expireAfterFrames, createdAtFrames));

        if (SAVE_UNIT_LOGS_TO_FILES > 0 && unit != null) LogUnitsToFiles.saveUnitLogToFile(message, unit);

        if (messages.size() > limit) messages.remove(0);

//        if (ACherryVis.isEnabled() && unit != null) {
//            ACherryVisLogUnit.logUnitData(unit, message);
//        }
    }

    public ArrayList<LogMessage> messages(int nowFrames, long nowRealSeconds) {
        if (A.everyNthGameFrame(expireAfterFrames)) {
            removeOldMessages(nowFrames, nowRealSeconds);
        }

        return messages;
    }

    public boolean lastMessageWas(String message) {
        return messages.size() > 0
            && lastMessage() != null
            && lastMessage().message() != null
            && lastMessage().message().equals(message);
    }

    public LogMessage lastMessage() {
        if (messages.isEmpty()) {
            return null;
        }

        return messages.get(messages.size() - 1);
    }

    public void replaceLastWith(String replaceWith, AUnit unit, int createdAtFrames) {
        if (messages.isEmpty()) {
            addMessage(replaceWith, unit, createdAtFrames);
            return;
        }

        messages.remove(messages.size() - 1);
        addMessage(replaceWith, unit, createdAtFrames);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
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

    private void removeOldMessages(int nowFrames, long nowRealSeconds) {
        if (expireAfterFrames == -1) return;

        messages.removeIf(message -> message.expired(nowFrames, nowRealSeconds));
    }

    public void print() {
        print(null);
    }

    public void print(String string) {
        if (string != null) System.out.println("--- " + string + " ---");

        for (LogMessage message : messages) {
            System.out.println(message.messageWithTime());
        }
    }

    public int countMessage(String message) {
        int count = 0;

        for (LogMessage logMessage : messages) {
            if (logMessage.message().equals(message)) {
                count++;
            }
        }

        return count;
    }
}
