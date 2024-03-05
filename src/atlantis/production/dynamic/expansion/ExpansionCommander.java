package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.expansion.protoss.ProtossExpansionCommander;
import atlantis.production.dynamic.expansion.terran.TerranExpansionCommander;
import atlantis.production.dynamic.expansion.zerg.ZergExpansionCommander;

public class ExpansionCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProtossExpansionCommander.class,
            TerranExpansionCommander.class,
            ZergExpansionCommander.class,
        };
    }
}
