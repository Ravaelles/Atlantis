package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerRepository;
import atlantis.util.We;

import java.util.Collection;

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
     */
    @Override
    protected void handle() {
        if (AGame.notNthGameFrame(9)) return;

        handleSubcommanders();
    }
}
