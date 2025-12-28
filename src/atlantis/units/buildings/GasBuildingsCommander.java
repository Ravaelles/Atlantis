package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.game.AGame;

public class GasBuildingsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            NumberOfGasWorkersCommander.class
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
