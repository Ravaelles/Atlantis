package atlantis.combat.state;

public enum AttackState {
    NONE,
    TARGET_ACQUIRED,
    STARTING,
    PENDING,
    FINISHED;

    public static AttackState getDefault() {
        return NONE;
    }

    public boolean finishedShooting() {
        return this == FINISHED;
    }

    public boolean pending() {
        return this == PENDING;
    }
}
