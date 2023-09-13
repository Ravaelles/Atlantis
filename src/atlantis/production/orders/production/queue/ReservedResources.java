package atlantis.production.orders.production.queue;

import atlantis.game.A;
import atlantis.game.AGame;

public class ReservedResources {
    private static int minerals = 0;
    private static int gas = 0;

    // =========================================================

    public static void reset() {
        minerals = 0;
        gas = 0;
    }

    // =========================================================

    public static void reserveMinerals(int minerals) {
//        A.errPrintln("reserveMinerals(" + minerals + " / " + ReservedResources.minerals + ")");
        ReservedResources.minerals += minerals;
    }

    public static void reserveGas(int gas) {
        ReservedResources.gas += gas;
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