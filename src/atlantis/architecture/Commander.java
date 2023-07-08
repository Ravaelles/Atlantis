package atlantis.architecture;

import atlantis.game.A;

public abstract class Commander {

//    private Class<? extends Commander>[] subcommanders = new Class[] {};

    private Commander[] commanderInstances;

    // =========================================================

    public Commander() {
        this.commanderInstances = initializeCommanderInstances();
    }

    // =========================================================

    /**
     * All sub-commanders. Order matters.
     */
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {};
    }

    public void handle() {
        handleSubcommanders();
    }

    public void handleSubcommanders() {
        for (Commander commander : commanderInstances){
            commander.handle();
        }
    }

    private Commander[] initializeCommanderInstances() {
        Class<? extends Commander>[] subcommanders = subcommanders();

        commanderInstances = new Commander[subcommanders.length];

        int index = 0;
        for (Class<? extends Commander> classObject : subcommanders){
            Commander commander = instantiateCommander(classObject);

            commanderInstances[index++] = commander;
        }

        return null;
    }

    private Commander instantiateCommander(Class<? extends Commander> classObject) {
        try {
            return classObject.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getMessage());
            return null;
        }
    }
    
}
