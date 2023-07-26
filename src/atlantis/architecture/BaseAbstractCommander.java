package atlantis.architecture;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;

import java.lang.reflect.InvocationTargetException;
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
            if (commander == null) {
                System.err.println("COMMANDER INIT null for " + classObject);
                AGame.exit();
            }

            commanderInstances[index++] = commander;
        }

        return null;
    }

    public Commander instantiateCommander(Class<? extends Commander> classObject) {
        try {
            return classObject.getDeclaredConstructor().newInstance();
        }
        catch (InstantiationException e) {
            A.printStackTrace("There has to be a constructor in class: " + classObject);
        }
        catch (InvocationTargetException e) {
            System.err.println("There was an error in constructor of class: " + classObject);
            e.printStackTrace();
        }
        catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getClass() + " / " + e.getMessage());
        }

        AGame.exit();
        return null;
    }

    // =========================================================

    protected static Class[] mergeCommanders(
        Class[] raceSpecific, Class[] generic
    ) {
        return Stream.concat(
            Arrays.stream(raceSpecific),
            Arrays.stream(generic)
        ).toArray(Class[]::new) ;
    }
}
