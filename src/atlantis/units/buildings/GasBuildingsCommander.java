package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.AGame;

public class GasBuildingsCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            NumberOfGasWorkersCommander::new
        };
    }

    /**
     * If any of our gas extracting buildings needs worker, it will assign exactly one worker per frame (until
     * no more needed).
     *
     * @return
     */
    @Override
    protected boolean handle() {
        if (AGame.notNthGameFrame(9)) return false;

        handleSubcommanders();
        return false;
    }
}
