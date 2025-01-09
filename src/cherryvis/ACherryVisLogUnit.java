package cherryvis;

import atlantis.game.A;
import atlantis.units.AUnit;
import cherryvis.java.CherryVis;

public class ACherryVisLogUnit {
    public static void logUnitData(AUnit unit, String message) {
        CherryVis.getInstance().log(message, unit.u(), "At " + A.now() + " (" + A.seconds() + "s)");
    }
}
