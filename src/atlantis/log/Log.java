package atlantis.log;

import atlantis.util.A;

import java.util.ArrayList;

public class Log {

    private static ArrayList<LogMessage> messages = new ArrayList<>();

    public static void addMessage(String message) {
        messages.add(new LogMessage(message));
    }

    public static ArrayList<LogMessage> messages() {
        if (A.everyNthGameFrame(60)) {
            removeOldMessages();
        }

        return messages;
    }

    private static void removeOldMessages() {
        messages.removeIf(LogMessage::expired);
    }

}
