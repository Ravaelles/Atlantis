package atlantis.production.orders.production.queue;

import atlantis.game.A;
import atlantis.game.AGame;

public class ReservedResources {
    public static final int MAX_VALUE = 500;

    private static int minerals = 0;
    private static int gas = 0;

    // =========================================================

    public static void reset() {
//        System.out.println("---------------------- RESET");
//        if (minerals > 0) A.printStackTrace("Ah!");

        minerals = 0;
        gas = 0;
    }

    // =========================================================

    public static void reserveMinerals(int minerals, String whatFor) {
        ReservedResources.minerals += minerals;

//        if (ReservedResources.minerals >= MAX_VALUE) ReservedResources.minerals = MAX_VALUE;
//        if (ReservedResources.minerals < 0) ReservedResources.minerals = 0;

//        System.out.println("Reserved MINERALS = " + ReservedResources.minerals + " / " + whatFor);

//        if (ReservedResources.minerals < 0 && !Env.isTesting()) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Trying to reserve negative minerals");
//        }
    }

    public static void reserveGas(int gas, String whatFor) {
        ReservedResources.gas += gas;

//        if (ReservedResources.gas >= MAX_VALUE) ReservedResources.gas = MAX_VALUE;
//        if (ReservedResources.gas < 0) ReservedResources.gas = 0;

//        if (gas > 0)
//            System.out.println("Reserved GAS = " + ReservedResources.gas + " / " + whatFor);

//        if (ReservedResources.gas < 0 && !Env.isTesting()) {
//            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Trying to reserve negative gas");
//        }
    }

    // =========================================================

    public static int minerals() {
        return Math.min(500, ReservedResources.minerals);
    }

    public static int gas() {
        return Math.min(250, ReservedResources.gas);
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
