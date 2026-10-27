package atlantis.game.event;

import atlantis.util.AConsole;
import atlantis.util.ClassScanner;
import atlantis.util.log.LogLevel;

public class AutoRegisterEventListeners {
    private static boolean initialized = false;

    public static void initializeListeners() {
        if (initialized) return;

        Class<?> currentClass = null;
        try {
            for (Class<?> listenerClass : ClassScanner.classesExtending(AutomaticListener.class)) {
                currentClass = listenerClass;
                AutomaticListener listener = (AutomaticListener) listenerClass.getDeclaredConstructor().newInstance();
                Events.register(listener.listensTo(), listener);
            }
        } catch (Exception e) {
            AConsole.errPrintln(LogLevel.HIGH, "AutoRegisterEventListeners Failed to initialize: " + currentClass);
        }

        initialized = true;

    }
}
