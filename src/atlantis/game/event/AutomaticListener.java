package atlantis.game.event;

public abstract class AutomaticListener {

    /**
     * Event listeners are auto-registered using AutoRegisterEventListeners.
     */
    public AutomaticListener() {
    }

    /**
     * Event listeners are auto-registered using AutoRegisterEventListeners.
     */
    public abstract Event listensTo();

    /**
     * Event listeners are auto-registered using AutoRegisterEventListeners.
     */
    public abstract void onEvent(Event event, Object... data);
}
