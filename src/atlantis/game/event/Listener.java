package atlantis.game.event;

public abstract class Listener {
    public Listener() {
//        Events.register(listensTo(), getClass());
//        Events.register(listensTo(), this);
    }

    public abstract String listensTo();

    public abstract void onEvent(String event, Object... data);
}
