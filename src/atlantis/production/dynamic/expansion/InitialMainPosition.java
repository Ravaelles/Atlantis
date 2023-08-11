package atlantis.production.dynamic.expansion;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Playing as Terran the position of main can change, and e.g. we don't want to rebuild entire turrets if
 * the base relocates to another location. Let's remember the initial position of our main.
 */
public class InitialMainPosition {
    private static InitialMainPosition instance = null;
    private static APosition initialMainPosition = null;

    // =========================================================

    public static void remember() {
        AUnit main = Select.main();
        if (main != null) {
            InitialMainPosition.initialMainPosition = main.position();
        }
    }

    public static APosition initialMainPosition() {
        return initialMainPosition;
    }
}
