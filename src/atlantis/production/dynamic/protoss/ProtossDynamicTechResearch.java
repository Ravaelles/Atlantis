package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.protoss.tech.*;
import atlantis.util.We;


public class ProtossDynamicTechResearch extends Commander {
    @Override
    public boolean applies() {
        return We.protoss() && A.everyNthGameFrame(61);
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ResearchSingularityCharge.class,
            ResearchPsionicStorm.class,
            ResearchLegEnhancements.class,
            ResearchProtossGroundWeapons.class,
            ResearchProtossGroundArmor.class,
        };
    }
}
