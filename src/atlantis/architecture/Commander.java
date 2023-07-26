package atlantis.architecture;

public abstract class Commander extends BaseAbstractCommander {
    public Commander() {
        this.commanderInstances = initializeCommanderInstances();
    }

    // =========================================================

    /**
     * All sub-commanders. Order matters.
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {};
    }

    public boolean applies() {
        return true;
    }

    public void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderInstances){
            if (commander.applies()) {
                commander.handle();
            }
        }
    }
}
