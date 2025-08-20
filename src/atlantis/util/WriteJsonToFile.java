package atlantis.util;

import atlantis.game.A;

import java.io.FileWriter;
import java.io.IOException;

public class WriteJsonToFile {

    /**
     * Writes aligned JSON to a file with pretty printing and key alignment.
     *
     * @param filename   Path of the output file
     * @param keys       Array of JSON keys
     * @param values     Array of JSON values (same order/length as keys)
     * @param colWidth   Minimal width of key column (e.g. 32)
     * @param useTabs    Whether to use tabs (true) or spaces (false) for indentation
     */
    public static boolean writeOrAppend(String filename, String[] keys, String[] values,
                                        int colWidth, boolean useTabs) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values must have the same length");
        }

        String indent = useTabs ? "\t" : "    "; // 1 tab or 4 spaces

        boolean append = A.fileExists(filename) && A.fileSize(filename) > 0;

        try (FileWriter writer = new FileWriter(filename, true)) {
            if (append) {
                writeOrAppend(writer, ",\n", append);
            }

            writeOrAppend(writer, "{\n", append);

            for (int i = 0; i < keys.length; i++) {
                String key = "\"" + keys[i] + "\"";

                // Escape quotes inside values if it's a string-like
                String val = values[i];
                if (!isNumeric(val) && !val.equals("true") && !val.equals("false") && !val.equals("null")) {
                    val = "\"" + val.replace("\"", "\\\"") + "\"";
                }

                String line = String.format(
                    indent + "%-" + colWidth + "s : %s",
                    key, val
                );

                if (i < keys.length - 1) {
                    line += ",";
                }
                line += "\n";

                writeOrAppend(writer, line, append);
            }
            writeOrAppend(writer, "}", append);

            return true;
        }
        catch (IOException e) {
            A.errPrintln("Error writing to file: " + filename);
        }

        return false;
    }

    private static void writeOrAppend(FileWriter writer, String str, boolean append) throws IOException {
        if (append) writer.append(str);
        else writer.write(str);
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}