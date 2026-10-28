package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.production.dynamic.protoss.tech.*;
import atlantis.util.We;


public class ProtossDynamicTechResearch extends Commander {
    @Override
    public boolean applies() {
        return We.protoss() && A.everyNthGameFrame(61);
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            ResearchSingularityCharge::new,
            ResearchPsionicStorm::new,
            ResearchLegEnhancements::new,
            ResearchProtossGroundWeapons::new,
            ResearchProtossGroundArmor::new,
        };
    }
}
