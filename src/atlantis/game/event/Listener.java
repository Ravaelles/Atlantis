package atlantis.game.event;

public abstract class Listener {

    /**
     * Event listeners are auto-registered using AutoRegisterEventListeners.
     */
    public Listener() {
    }

    /**
     * Event listeners are auto-registered using AutoRegisterEventListeners.
     */
    public abstract String listensTo();

    /**
     * Event listeners are auto-registered using AutoRegisterEventListeners.
     */
    public abstract void onEvent(String event, Object... data);
}
