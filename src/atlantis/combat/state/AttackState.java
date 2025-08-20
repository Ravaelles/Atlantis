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

    public boolean starting() {
        return this == STARTING;
    }

//    public boolean atLeastStarting() {
////        return this == STARTING || this == PENDING || this == FINISHED;
//        return this == STARTING || this == PENDING;
//    }
}
