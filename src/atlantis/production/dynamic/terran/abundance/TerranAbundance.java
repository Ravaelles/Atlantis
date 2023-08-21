package atlantis.production.dynamic.terran.abundance;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.AbundanceCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranAbundance extends AbundanceCommander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            TerranAbundanceEarlyToMidGame.class,
            TerranAbundanceLateGame.class,
            TerranAbundanceTech.class,
        };
    }
}
