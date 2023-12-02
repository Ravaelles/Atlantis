package atlantis.util.cache;

import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;
import bwta.BaseLocation;

import java.util.Arrays;

public class CacheKey {

    public static String create(Object... args) {
        StringBuilder key = new StringBuilder();

        for (Object o : args) {
            key.append(toKey(o)).append(",");
        }

        return key.toString();
    }

    public static String create(AUnitType... types) {
//        return Arrays.stream(types).reduce("", (result, type) -> (result + "," + type.id()));
        return Arrays.stream(types).map(AUnitType::name).reduce("", (result, type) -> (result + "," + type));
    }

    public static String toKey(Object object) {
        if (object == null) return "NuLL";

        if (object instanceof String) return (String) object;
        if (object instanceof Double) return A.digit((Double) object);
        if (object instanceof AUnit) return ((AUnit) object).typeWithUnitId();
        if (object instanceof AUnitType) return ((AUnitType) object).name();
        if (object instanceof APosition) return ((APosition) object).toStringPixels();
        if (object instanceof Construction) return ((Construction) object).id() + "";
        if (object instanceof Selection) return ((Selection) object).unitIds();
        if (object instanceof BaseLocation) return ((BaseLocation) object).toString();
        if (object instanceof ABaseLocation) return ((ABaseLocation) object).toString();

        ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
            "Unknown object to CacheKey: " + object.getClass().getName()
                + "\nReturning object.toString(), but this should get whitelisted."
        );

        return object.toString();
    }
}
