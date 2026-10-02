package atlantis.architecture;

import atlantis.game.AGame;
import atlantis.game.AtlantisGameCommander;

import java.util.Arrays;

public abstract class BaseCommander {
    protected Commander[] commanderObjects = new Commander[0];
    protected boolean shouldProfile;
    protected int lastFrameInvoked = -1;

    public BaseCommander() {
        shouldProfile = Arrays.asList(AtlantisGameCommander.topLevelSubcommanders()).contains(this.getClass());

        initChildren(subcommanders());
    }

    protected abstract CommanderFactory[] subcommanders();

    /**
     * Stage C: builds child commanders from explicit constructor references,
     * without reflection. Order of {@code factories} is the execution order.
     * A failing constructor ends the game, same as the old reflective path
     * (which called {@code AGame.exit()} on any instantiation failure).
     */
    protected final void initChildren(CommanderFactory... factories) {
        Commander[] created = new Commander[factories.length];

        int index = 0;
        for (CommanderFactory factory : factories) {
            try {
                Commander commander = factory.create();
                if (commander == null) {
                    System.err.println("COMMANDER INIT null for " + factory);
                    AGame.exit();
                }

                created[index++] = commander;
            } catch (Exception e) {
                System.err.println("Exception /" + e.getClass() + "/ trying to init commander");
                AGame.exit();
            }
        }

        commanderObjects = created;
    }

    // =========================================================

    protected static CommanderFactory[] mergeCommanders(CommanderFactory[] raceSpecific, CommanderFactory[] generic) {
        CommanderFactory[] merged = new CommanderFactory[raceSpecific.length + generic.length];
        System.arraycopy(raceSpecific, 0, merged, 0, raceSpecific.length);
        System.arraycopy(generic, 0, merged, raceSpecific.length, generic.length);
        return merged;
    }

    public boolean shouldProfile() {
        return shouldProfile;
    }
}
