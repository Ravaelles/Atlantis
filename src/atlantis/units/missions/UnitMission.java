package atlantis.units.missions;

public class UnitMission {
    
    private boolean attack;
    private boolean run;

    // =========================================================
    
    public UnitMission() {
        this(false);
    }

    public UnitMission(boolean attack) {
        this.attack = attack;
    }

    public UnitMission(boolean attack, boolean run) {
        this.attack = attack;
        this.run = run;
    }
    
    // =========================================================

    public boolean isAttacking() {
        return attack;
    }

    public boolean isRunningOrRetreating() {
        return run;
    }
    
}
