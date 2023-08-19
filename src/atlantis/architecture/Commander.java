package atlantis.architecture;

public class Commander extends BaseAbstractCommander {
    /**
     * All sub-commanders. Order matters.
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{};
    }

    public boolean applies() {
        return true;
    }

    public void invoke() {
//        System.err.println("INVOKE " + getClass().getSimpleName());

        if (applies()) {
            handle();
        }

        handleSubcommanders();
    }

    private void invokeFromParent(Commander parentCommander) {
//        System.err.println("-- invoke " + getClass().getSimpleName()
//            + "\n            from " + parentCommander.getClass().getSimpleName());

        if (applies()) {
            handle();
        }

        handleSubcommanders();
    }

    protected void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderObjects) {
            commander.invokeFromParent(this);
        }
    }
}
