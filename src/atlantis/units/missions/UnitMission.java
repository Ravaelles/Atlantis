package atlantis.units.missions;

public class UnitMission {
    
    private boolean attack;

    // =========================================================
    
    public UnitMission() {
        this(false);
    }

    public UnitMission(boolean attack) {
        this.attack = attack;
    }
    
    // =========================================================

    public boolean isAttacking() {
        return attack;
    }

}
