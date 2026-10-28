package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.production.dynamic.terran.tech.*;
import atlantis.util.We;

public class TerranDynamicTechResearch extends Commander {
    @Override
    public boolean applies() {
        return We.terran() && A.everyNthGameFrame(39);
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            ResearchSiegeMode::new,
            ResearchStimpacks::new,
            ResearchU238::new,
            CloakingField::new,
            Lockdown::new,
            TerranInfantryWeapons::new,
            TerranInfantryArmor::new,
        };
    }
}
