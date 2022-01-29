package atlantis.util.log;

import atlantis.game.A;

import java.util.ArrayList;

public class Log {

    private ArrayList<LogMessage> messages = new ArrayList<>();
    private int expireAfterFrames;
    private int limit = 20;

    // =========================================================

    public Log(int expireAfterFrames, int limit) {
        this.expireAfterFrames = expireAfterFrames;
        this.limit = limit;
    }

    // =========================================================

    public void addMessage(String message) {
        messages.add(new LogMessage(message, expireAfterFrames));

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

    private void removeOldMessages() {
        messages.removeIf(LogMessage::expired);
    }
}
