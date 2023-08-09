package atlantis.architecture;

public class Commander extends BaseAbstractCommander {
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

    public void invoke() {
        if (applies()) {
            handle();
        }

        handleSubcommanders();
    }

    protected void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderObjects){
            if (commander.applies()) {
                commander.handle();
            }
        }
    }
}
