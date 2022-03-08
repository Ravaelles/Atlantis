package atlantis.util.cache;

import atlantis.units.AUnitType;

import java.util.Arrays;

public class CachePathKey {

    public static String get(AUnitType ...types) {
//        return Arrays.stream(types).reduce("", (result, type) -> (result + "," + type.id()));
        return Arrays.stream(types).map(AUnitType::name).reduce("", (result, type) -> (result + "," + type));
    }

}
