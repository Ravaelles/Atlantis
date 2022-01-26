package atlantis.combat.micro;

import atlantis.units.AUnit;

public abstract class Microable {

    public static Class[] macroManagers;

//    public abstract Class[] macroManagers();

    public static boolean update(AUnit unit) {
        return false;
    }

}
