package atlantis.units.buildings;

import atlantis.game.A;
import atlantis.units.select.Count;
import atlantis.util.We;

public class GasWorkersPerBuilding {
    public static int define() {
        int workers = Count.workers();
        int gas = A.gas();

//        if (workers <= 8) {
//            return 0;
//        }

        if (workers <= 7) {
            return 0;
        }

        if (workers >= 32 && gas <= 350) {
            return 3;
        }

        if (We.protoss()) {
            if (A.s <= 500 && gas >= 80) return 1;
            if (A.s <= 600 && gas >= 120) return 2;
            if (A.s <= 700 && gas >= 180) return 1;
        }

//        if (workers >= 17 && gas <= 200) {
//            return 3;
//        }

        if (gas >= 410) {
            return 1;
        }
        else if (gas >= 300) {
            if (A.seconds() <= 400) {
                if (A.minerals() <= 400) return 0;
                return 1;
            }
            else {
                if (A.minerals() <= 600) return 2;
            }
        }

        if (workers <= 13 && !A.hasMinerals(150)) {
            return 1;
        }

        if (A.hasGas(240)) return 2;
        if (A.hasGas(310)) return 1;

        if (A.seconds() <= 900) {
            if (workers <= 23 && A.hasGas(270)) {
                return 2;
            }
            if (workers <= 30 && A.hasGas(350)) {
                return 2;
            }
        }

        if (gas >= 380 && A.minerals() <= 280) {
            return A.inRange(1, Count.workers() / 12, 3);
        }

        int seconds = A.seconds();

        if (seconds < 150 && A.hasGas(170)) {
            return 1;
        }
        else if (seconds < 250 && A.hasGas(250)) {
            return 2;
        }
        else {
            return 3;
        }
    }
}
