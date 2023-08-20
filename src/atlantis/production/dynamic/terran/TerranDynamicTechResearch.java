package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.terran.tech.*;
import atlantis.util.We;

public class TerranDynamicTechResearch extends Commander {
    @Override
    public boolean applies() {
        return We.terran() && A.everyNthGameFrame(39);
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            SiegeMode.class,
            Stimpacks.class,
            U238.class,
            CloakingField.class,
            Lockdown.class,
            TerranInfantryWeapons.class,
            TerranInfantryArmor.class,
        };
    }
}
