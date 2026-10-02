package atlantis.util;

import java.util.Collection;

/**
 * Console writers used across the bot, extracted from the {@code A} god utility
 * (REVIEW §4, Stage H).
 *
 * <p>Output formatting is infrastructure, not game state, so it no longer
 * belongs next to the resource and clock facades. Behaviour is unchanged:
 * these are thin {@code System.out}/{@code System.err} wrappers, and they
 * return {@code true} in several places purely because call sites use them as
 * one-line guards - kept as-is so this commit stays behaviour-neutral.</p>
 *
 * <p>A future logging port (Stage J) can replace this class without touching
 * call sites.</p>
 */
public class AConsole {

    /**
     * Prints the list of the given argument, separated with commas.
     */
    public static void print(Object... args) {
        (System.out).print(args[0]);

        if (args.length > 1) {
            (System.out).print(", ");
        }

        for (int i = 1; i < args.length - 1; i++) {
            (System.out).print(args[i] + ", ");
        }

        (System.out).println(args[args.length - 1]);
    }

    /**
     * @return exception stack converted to String (each trace in new line)
     */
    public static String convertStackToString(StackTraceElement[] stackTrace) {
        return convertStackToString(stackTrace.length, stackTrace);
    }

    /**
     * @param maxLines maximum number of lines of result String
     * @return exception stack converted to String (each trace in new line)
     */
    public static String convertStackToString(int maxLines, StackTraceElement[] stackTrace) {
        String result = "";

        for (int i = 0; i < stackTrace.length && i < maxLines; i++) {
            result += stackTrace[i];

            if (i != stackTrace.length - 1) {
                result += "\n";
            }
        }
        // for (int i = stackTrace.length - 1; i >= 0; i--) {
        // result += stackTrace[i];
        //
        // if (i != 0)
        // result += "\n";
        // }

        return result;
    }

    public static void printList(Collection<?> list) {
        println("List (" + list.size() + ")");
        for (Object o : list) {
            println("- " + o);
        }
    }

    public static void printStackTrace() {
        printStackTrace(null);
    }

    public static void printStackTrace(String message) {
        if (message != null) {
            System.err.println("### " + message + " ##########");
        }
        Thread.dumpStack();
    }

    public static boolean printErrorAndReturnTrue(String text) {
        println(text);
        return true;
    }

    public static void println() {
        (System.out).println("");
    }

    public static boolean println(Object string) {
        (System.out).println(string);
        return true;
    }

    public static boolean errPrintln(Object string) {
        (System.err).println(string);
        return true;
    }

    public static void print(Object string) {
        (System.out).print(string);
    }
}
