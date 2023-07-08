package atlantis.combat.squad;

public abstract class HasSquad {
    protected Squad squad;

    public HasSquad(Squad squad) {
        this.squad = squad;
    }
}
