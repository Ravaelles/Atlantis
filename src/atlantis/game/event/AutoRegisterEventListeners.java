package atlantis.game.event;

import atlantis.game.A;
import atlantis.util.ClassScanner;

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
            A.errPrintln("AutoRegisterEventListeners Failed to initialize: " + currentClass);
        }

        initialized = true;

    }
}
