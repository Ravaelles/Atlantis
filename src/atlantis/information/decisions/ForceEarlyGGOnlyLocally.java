package atlantis.information.decisions;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.generic.OurArmy;
import atlantis.units.select.Count;
import atlantis.util.log.ErrorLog;

public class ForceEarlyGGOnlyLocally extends Commander {
    @Override
    public boolean applies() {
        if (!GGForEnemy.allowed) return false;

        return A.s >= 8 * 60
//            && (OurArmy.strength() <= 15 || A.resourcesBalance() <= -1100)
            && !A.isUms()
            && OurArmy.strength() <= 10
            && Count.ourCombatUnits() <= 1
            && Count.workers() <= 25;
    }

    @Override
    protected void handle() {
        A.errPrintln("####################################################");
        A.errPrintln("####################################################");
        A.errPrintln("####################################################");
        A.errPrintln("####################################################");
        A.errPrintln("### Local forced early leave #######################");
        A.errPrintln("####################################################");
        A.errPrintln("####################################################");
        A.errPrintln("####################################################");
        A.errPrintln("####################################################");

        AGame.sendMessage("Local force early leave");
        ErrorLog.printErrorOnce("ForceEarlyGGOnlyLocally at " + A.s + "s, strength: " + OurArmy.strength());

        Atlantis.getInstance().onEnd(false);
    }
}
