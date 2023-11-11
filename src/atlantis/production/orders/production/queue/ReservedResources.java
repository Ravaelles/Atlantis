package atlantis.production.orders.production.queue;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.util.log.ErrorLog;

public class ReservedResources {
    public static final int MAX_VALUE = 500;

    private static int minerals = 0;
    private static int gas = 0;

    // =========================================================

    public static void reset() {
        minerals = 0;
        gas = 0;
    }

    // =========================================================

    public static void reserveMinerals(int minerals) {
        ReservedResources.minerals += minerals;
        if (ReservedResources.minerals >= MAX_VALUE) ReservedResources.minerals = MAX_VALUE;
        if (ReservedResources.minerals < 0) ReservedResources.minerals = 0;

//        if (ReservedResources.minerals < 0 && !Env.isTesting()) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Trying to reserve negative minerals");
//        }
    }

    public static void reserveGas(int gas) {
        ReservedResources.gas += gas;
        if (ReservedResources.gas >= MAX_VALUE) ReservedResources.gas = MAX_VALUE;
        if (ReservedResources.gas < 0) ReservedResources.gas = 0;

//        if (ReservedResources.gas < 0 && !Env.isTesting()) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Trying to reserve negative gas");
//        }
    }

    // =========================================================

    public static int minerals() {
        return minerals;
    }

    public static int gas() {
        return gas;
    }

    public static void printMinerals() {
        A.errPrintln(
            "         Reserved minerals = " + ReservedResources.minerals + " / " + AGame.minerals()
        );
    }

    public static void print() {
        A.errPrintln(
            "         Reserved minerals/gas = " + ReservedResources.minerals + " / " + ReservedResources.gas
        );
    }
}
