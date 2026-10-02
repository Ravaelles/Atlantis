package atlantis.util.log;

import atlantis.util.AFile;

public class LogToFile {
    public static void info(String text) {
        AFile.appendToFile("debug-log.txt", text);
    }
}
