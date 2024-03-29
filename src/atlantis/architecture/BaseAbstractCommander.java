package atlantis.architecture;

import atlantis.game.A;
import atlantis.game.AGame;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class BaseAbstractCommander {
    protected Commander[] commanderObjects;

    public BaseAbstractCommander() {
        initializeCommanderInstances();
    }

    protected abstract Class<? extends Commander>[] subcommanders();

    protected void initializeCommanderInstances() {
        Class<? extends Commander>[] subcommanders = subcommanders();

        commanderObjects = new Commander[subcommanders.length];

        int index = 0;
        for (Class<? extends Commander> classObject : subcommanders){
            try {
                Commander commander = instantiateCommander(classObject);
                if (commander == null) {
                    System.err.println("COMMANDER INIT null for " + classObject);
                    AGame.exit();
                }

                commanderObjects[index++] = commander;
            }
            catch (Exception e) {
                System.err.println("Exception /" + e.getClass() + "/ trying to init /" + classObject + "/");
            }
        }
    }

    protected Commander instantiateCommander(Class<? extends Commander> classObject) {
        try {
            return classObject.getDeclaredConstructor().newInstance();
        }
        catch (InstantiationException e) {
            A.printStackTrace("There has to be a constructor in:\n" + classObject);
        }
        catch (InvocationTargetException e) {
            System.err.println("There was an error in constructor of:\n" + classObject);
            e.printStackTrace();
        }
        catch (Exception e) {
            A.printStackTrace("Could not instantiate " + classObject + " / " + e.getClass() + " / " + e.getMessage());
        }

        AGame.exit();
        return null;
    }

    // =========================================================

    protected static Class[] mergeCommanders(Class[] raceSpecific, Class[] generic) {
        return Stream.concat(Arrays.stream(raceSpecific), Arrays.stream(generic)).toArray(Class[]::new) ;
    }
}
