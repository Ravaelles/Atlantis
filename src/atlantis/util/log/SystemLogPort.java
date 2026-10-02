package atlantis.util.log;

/**
 * Live {@link LogPort}: the process console. This is the only class in the bot
 * that is allowed to touch {@code System.out}/{@code System.err} directly.
 */
public class SystemLogPort implements LogPort {

    @Override
    public void print(Object message) {
        (System.out).print(message);
    }

    @Override
    public void println(Object message) {
        (System.out).println(message);
    }

    @Override
    public void printError(Object message) {
        (System.err).println(message);
    }

    @Override
    public void printStackTrace(String message) {
        if (message != null) {
            System.err.println("### " + message + " ##########");
        }
        Thread.dumpStack();
    }
}
