package atlantis.units.actions;

public class UnitAction {
    
    private boolean attack;
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
        return "UnitAction{" + "attack=" + attack + ", run=" + run + '}';
    }
    
    // =========================================================
    
    public boolean isAttacking() {
        return attack;
    }

    public boolean isRunningOrRetreating() {
        return run;
    }
    
}
