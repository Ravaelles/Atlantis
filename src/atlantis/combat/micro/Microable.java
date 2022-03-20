package atlantis.combat.micro;

import atlantis.units.AUnit;

public abstract class Microable {

    public Class[] macroManagers;

//    public abstract Class[] macroManagers();

    public boolean update(AUnit unit) {
        return false;
    }

}
