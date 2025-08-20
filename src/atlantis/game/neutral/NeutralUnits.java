package atlantis.game.neutral;

import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;

import java.util.HashMap;
import java.util.Map;

public class NeutralUnits {
    protected static Map<Integer, AbstractFoggedUnit> neutralUnitsDiscovered = new HashMap<>();

    public static Selection discovered() {
        Units units = new Units();
        units.addFoggedUnits(neutralUnitsDiscovered.values());

        return Select.from(units);
    }

    public static void weDiscoveredNewUnit(AUnit neutralUnit) {
        if (!neutralUnit.type().isMineralField() && !neutralUnit.type().isGeyser()) return;

        addFoggedUnit(neutralUnit);
    }

    public static void addFoggedUnit(AUnit neutralUnit) {
        if (!neutralUnit.isNeutral()) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "NeutralUnits.addFoggedUnit() called for non-neutral unit: " + neutralUnit
            );
            return;
        }

        int id = neutralUnit.id();

//        System.err.println("addFoggedUnit = " + neutralUnit);
        if (!neutralUnitsDiscovered.containsKey(id)) {
            AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(neutralUnit);

            neutralUnitsDiscovered.put(id, foggedUnit);
//            System.err.println("added " + foggedUnit + " / " + enemyUnitsDiscovered.size());
        }
    }
}
