package atlantis.information;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class Neighborhood {

    public static boolean isItSafeToProduceWorker(AUnit base) {
        return Select.enemyCombatUnits().inRadius(10, base).isEmpty();
    }

}
