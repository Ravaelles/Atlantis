package atlantis.units.actions;

public class UnitAction {

    private String name;
    private final boolean attack;
    private boolean run;

    // =========================================================
    
    protected UnitAction() {
        this(false);
    }

    protected UnitAction(boolean attack) {
        this.attack = attack;
    }

    protected UnitAction(boolean attack, boolean run) {
        this.attack = attack;
        this.run = run;
    }
    
    // =========================================================

    @Override
    public String toString() {
        return name;
    }
    
    // =========================================================
    
    public boolean isAttacking() {
        return attack;
    }

    public boolean isRunningOrRetreating() {
        return run;
    }

    public String getName() {
        return name;
    }

    public UnitAction setName(String name) {
        this.name = name;
        return this;
    }
    
}
