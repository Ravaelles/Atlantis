package atlantis.util;

import atlantis.util.log.LogPort;
import atlantis.util.log.SystemLogPort;

import java.util.Collection;

/**
 * Console writers used across the bot, extracted from the {@code A} god utility
 * (REVIEW §4, Stage H).
 *
 * <p>Output formatting is infrastructure, not game state, so it no longer
 * belongs next to the resource and clock facades. What gets written goes
 * through the {@link LogPort} port (ADR 0001); this class only decides how a
 * message is composed. Tests swap the port via {@link #usePort(LogPort)} and
 * assert on what was reported instead of scraping the console.</p>
 *
 * <p>The writers return nothing: the previous {@code boolean} returns existed
 * because callers used {@code return A.println(...)} as a one-line guard, and
 * the few sites that did are now written out explicitly. The one exception is
 * {@link #printErrorAndReturnTrue(String)}, whose name is the contract.</p>
 *
 * <p>A static port holder is a transitional seam, the same trade-off as
 * {@code AUnit.setOrderSink(...)}: constructor injection everywhere would mean
 * touching every class in the bot before the composition root exists
 * (Stage I). See {@code _AI/NEXT.md}.</p>
 */
public class AConsole {

    private static LogPort port = new SystemLogPort();

    /**
     * Replaces the destination of all console output. Intended for tests and
     * for the future composition root; passing {@code null} is a programming
     * error and restores the console rather than failing silently.
     */
    public static void usePort(LogPort newPort) {
        port = newPort == null ? new SystemLogPort() : newPort;
    }

    /**
     * @return the port currently receiving console output
     */
    public static LogPort port() {
        return port;
    }

    /**
     * Prints the list of the given argument, separated with commas.
     */
    public static void print(Object... args) {
        print(args[0]);

        if (args.length > 1) {
            print(", ");
        }

        for (int i = 1; i < args.length - 1; i++) {
            print(args[i] + ", ");
        }

        println(args[args.length - 1]);
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
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < stackTrace.length && i < maxLines; i++) {
            result.append(stackTrace[i]);

            if (i != stackTrace.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
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
        port.printStackTrace(message);
    }

    /**
     * Kept for the handful of call sites that read better as
     * {@code return printErrorAndReturnTrue(...)}.
     */
    public static boolean printErrorAndReturnTrue(String text) {
        println(text);
        return true;
    }

    public static void println() {
        port.println("");
    }

    public static void println(Object string) {
        port.println(string);
    }

    public static void errPrintln(Object string) {
        port.printError(string);
    }

    public static void print(Object string) {
        port.print(string);
    }
}
