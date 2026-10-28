package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.production.dynamic.protoss.ProtossDynamicBuildingsCommander;
import atlantis.production.dynamic.protoss.ProtossSpecificBuildingsCommander;
import atlantis.util.We;

public class DynamicBuildingsCommander extends Commander {
    public static Commander get() {
        if (We.protoss()) return (new ProtossDynamicBuildingsCommander());

        return null;
    }

    @Override
    public boolean applies() {
        // Owner 2026-10-18 (__PLAN-DECISIONS): while the memorized opening
        // owns the game, no dynamic building may jump the script's rows
        // (measured: third Gateway site at supply 12). Workers and supply
        // keep flowing - buildings don't consume supply, so gating them
        // cannot stall the script.

        return !atlantis.production.orders.build.BuildOrderOwnsGame.owns();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected CommanderFactory[] subcommanders() {
        CommanderFactory[] generic = new CommanderFactory[]{
            ProtossSpecificBuildingsCommander::new,

            ExpansionCommander::new,
        };

        CommanderFactory[] raceSpecific = new CommanderFactory[0];

        if (We.protoss()) raceSpecific = new CommanderFactory[]{
            ProtossDynamicBuildingsCommander::new,
        };

        return mergeCommanders(generic, raceSpecific);
    }
}
