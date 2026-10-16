package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.AGame;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.units.select.Have;

public class DynamicProductionCommander extends Commander {
    @Override
    public boolean applies() {
        // Owner 2026-10-18 (__PLAN-DECISIONS): a memorized opening plays
        // itself - while build-order rows are due, dynamic producers stay
        // off and the script's own rows (workers included) drive. The
        // supply pump lives outside this commander and keeps running, so
        // a late BO pylon cannot supply-block the script.
        if (atlantis.production.orders.build.BuildOrderOwnsGame.owns()) return false;
        return !AGame.isUms() || Have.anyBuilding();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            AutoProduceWorkersCommander::new,
            DynamicUnitAndTechProducerCommander::new,
            DynamicBuildingsCommander::new,
        };
    }

}
