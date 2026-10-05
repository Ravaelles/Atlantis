package atlantis.units.select;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;
import bwta.BaseLocation;

import java.util.Arrays;

/**
 * Turns the arguments of a cache query into the string that names it.
 *
 * <p>This lives beside {@link Selection} rather than in {@code atlantis.util}
 * because it cannot be a kernel helper: it reads units, positions, chokes, base
 * locations, constructions and selections by {@code instanceof}, and a shared
 * kernel that knows thirteen domain types is not shared, it is just misplaced.
 * Five of the thirteen call sites are in this class; the rest are consumers that
 * already reach for {@code atlantis.units.select}.
 *
 * <p>Two branches had to go for that to be true. It used to know what a
 * {@code Construction} is (the two call sites that passed one now pass its id,
 * which is all the key ever read) and what an {@code ABaseLocation} is - a branch
 * that was a wrapper around {@code toString()}, so removing it changed no key
 * anywhere. Both types are consumers of the core, and a core class may not name
 * one: the move from {@code atlantis.util.cache} to here traded those two
 * violations for two new ones if they stayed, which is the trap #13 warns about.
 *
 * <p>What is left in
 * {@code atlantis.util.cache} is the cache itself, whose own two questions - "what
 * time is it" and "how do I copy a value I did not create" - are the ones still
 * worth a decision (_AI/NEXT.md #13).</p>
 */
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
        if (object instanceof Integer) return object.toString();
        if (object instanceof AUnit) return ((AUnit) object).typeWithUnitId();
        if (object instanceof AUnitType) return ((AUnitType) object).name();
        if (object instanceof APosition) return ((APosition) object).toStringPixels();
        if (object instanceof Selection) return ((Selection) object).unitIds();
        if (object instanceof BaseLocation) return ((BaseLocation) object).toString();
        if (object instanceof AChoke) return ((AChoke) object).toString();
        if (object instanceof TechType) return ((TechType) object).toString();
        if (object instanceof UpgradeType) return ((UpgradeType) object).name();
//        if (object instanceof UpgradeType) return ((UpgradeType) object).name()
//            + "(" + AGame.getPlayerUs().getUpgradeLevel((UpgradeType) object) + ")";
        if (object instanceof Class) return ((Class) object).getSimpleName();
        if (object instanceof Class[]) {
            return Arrays.stream((Class[]) object).map(Class::getSimpleName).reduce(
                "",
                (result, type) -> (result + "," + type)
            );
        }

        ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
            "Unknown object to CacheKey: " + object.getClass().getName()
                + "\nReturning object.toString(), but this should get whitelisted."
        );

        return object.toString();
    }
}
