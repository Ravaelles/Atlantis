package atlantis.game.listeners;

import atlantis.game.neutral.NeutralUnits;
import atlantis.units.AUnit;

public class OnNeutralNewUnitDiscovered {
    public static void update(AUnit unit) {
        NeutralUnits.weDiscoveredNewUnit(unit);
    }
}
