package atlantis.units.actions;

/**
 * Specific unit action.
 * @see Actions
 */
public class Action {

    private String name;
    private String toString;
    private final boolean attack;
    private boolean run;

    // =========================================================
    
    protected Action() {
        this(false);
    }

    protected Action(boolean attack) {
        this(attack, false);
    }

    protected Action(boolean attack, boolean run) {
        this.attack = attack;
        this.run = run;
    }

    // =========================================================

    public boolean isRunning() {
        return name.startsWith("RUN_") || equals(Actions.MOVE_SAFETY);
    }

    // =========================================================

    @Override
    public String toString() {
        return toString;
    }

    // =========================================================

    public boolean isAttacking() {
        return attack;
    }

//    public boolean isRunningOrRetreating() {
//        return run;
//    }

    public String name() {
        return name;
    }

    public Action setName(String name) {
        this.name = name;
        this.toString = name.replace("MOVE_", "");
        return this;
    }
}
