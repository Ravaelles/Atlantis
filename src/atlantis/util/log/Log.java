package atlantis.util.log;

import atlantis.game.A;

import java.util.ArrayList;

public class Log {

    private ArrayList<LogMessage> messages = new ArrayList<>();
    private int expireAfterFrames;

    // =========================================================

    public Log(int expireAfterFrames) {
        this.expireAfterFrames = expireAfterFrames;
    }

    // =========================================================

    public void addMessage(String message) {
        messages.add(new LogMessage(message, expireAfterFrames));
    }

    public ArrayList<LogMessage> messages() {
        if (A.everyNthGameFrame(expireAfterFrames)) {
            removeOldMessages();
        }

        return messages;
    }

    public boolean isNotEmpty() {
        return !messages.isEmpty();
    }

    // =========================================================

    private void removeOldMessages() {
        messages.removeIf(LogMessage::expired);
    }
}
