package atlantis.architecture;

public class Commander extends BaseAbstractCommander {
    public Commander() {
        initializeCommanderInstances();
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
