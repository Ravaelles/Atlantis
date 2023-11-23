package atlantis.util.cache;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import java.util.Arrays;

public class CacheKey {
    public static String get(AUnitType... types) {
//        return Arrays.stream(types).reduce("", (result, type) -> (result + "," + type.id()));
        return Arrays.stream(types).map(AUnitType::name).reduce("", (result, type) -> (result + "," + type));
    }

    public static String toKey(Object object) {
        if (object == null) return "-";

        if (object instanceof AUnit) return ((AUnit) object).toString();

        throw new RuntimeException("Unknown object to CacheKey: " + object.getClass().getName());
    }
}
