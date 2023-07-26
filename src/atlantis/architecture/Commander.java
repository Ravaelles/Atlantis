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

    public void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderObjects){
            if (commander.applies()) {
//                System.out.println("Handling COMMANDER: " + commander.getClass());
                commander.handle();
            }
        }
    }
}
