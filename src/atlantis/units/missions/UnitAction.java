package atlantis.units.missions;

public class UnitAction {
    
    private boolean attack;
    private boolean run;

    // =========================================================
    
    public UnitAction() {
        this(false);
    }

    public UnitAction(boolean attack) {
        this.attack = attack;
    }

    public UnitAction(boolean attack, boolean run) {
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
