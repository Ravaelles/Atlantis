package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.units.special.ums.UmsSpecialBehaviorCommander;

/**
 * Special manager for UMS maps (Use Map Settings type of maps). Great for testing macro on custom maps.
 */
public class SpecialActionsCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            UmsSpecialBehaviorCommander::new,
        };
    }
}
