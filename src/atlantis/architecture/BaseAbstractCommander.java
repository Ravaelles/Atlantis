package atlantis.architecture;

import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class BaseAbstractCommander {
    protected Commander[] commanderInstances;

    protected abstract Class<? extends Commander>[] subcommanders();

    protected Manager instantiateManager(Class<? extends Manager> classObject, AUnit unit) {
        try {
            return classObject.getDeclaredConstructor().newInstance(unit);
        } catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getMessage());
            return null;
        }
    }

    protected Commander[] initializeCommanderInstances() {
        Class<? extends Commander>[] subcommanders = subcommanders();

        commanderInstances = new Commander[subcommanders.length];

        int index = 0;
        for (Class<? extends Commander> classObject : subcommanders){
            Commander commander = instantiateCommander(classObject);

            commanderInstances[index++] = commander;
        }

        return null;
    }

    protected Commander instantiateCommander(Class<? extends Commander> classObject) {
        try {
            return classObject.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getMessage());
            return null;
        }
    }

    // =========================================================

    protected static Class<? extends Commander>[] mergeCommanders(Class[] raceSpecific, Class[] generic) {
        return (Class<? extends Commander>[]) Stream.concat(
            Arrays.stream(raceSpecific), Arrays.stream(generic)
        ).toArray();
    }
}
