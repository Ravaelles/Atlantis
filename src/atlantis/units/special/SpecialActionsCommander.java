package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.units.special.ums.UmsSpecialBehaviorCommander;

/**
 * Special manager for UMS maps (Use Map Settings type of maps). Great for testing macro on custom maps.
 */
public class SpecialActionsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            UmsSpecialBehaviorCommander.class,
        };
    }
}
