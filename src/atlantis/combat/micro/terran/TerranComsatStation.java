package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TechType;

public class TerranComsatStation {

    public static boolean update(AUnit comsat) {
        if (AGame.notNthGameFrame(5 + ((250 - comsat.energy()) / 3))) {
            return false;
        }
//        System.out.println(Select.enemy().ofType(AUnitType.Protoss_Observer).count());

        if (comsat.energy() >= 50) {
            scanObservers(comsat);
        }

        return false;
    }

    private static boolean scanObservers(AUnit comsat) {
        for (AUnit observer : Select.enemy().effCloaked().ofType(AUnitType.Protoss_Observer).listUnits()) {
            if (shouldScanThisObserver(observer, comsat)) {
                return scan(comsat, observer);
            }
        }

        return false;
    }

    private static boolean shouldScanThisObserver(AUnit observer, AUnit comsat) {
        if (comsat.energy() >= 100 && Select.ourRealUnits().inRadius(9, observer).atLeast(1)) {
            return true;
        }

        if (
                Select.enemies(AUnitType.Protoss_Carrier).inRadius(15, observer).isNotEmpty()
                && Select.ourRealUnits().inRadius(9, observer).atLeast(1)
        ) {
            return true;
        }

        return false;
    }

    private static boolean scan(AUnit comsat, AUnit unitToScan) {
//        System.out.println("unitToScan = " + unitToScan.getHP());
        if (unitToScan.effVisible()) {
            return false;
        }

//        if (unitToScan.isEffectivelyCloaked() && Select.enemy().exclude(unitToScan)
//                .cloakedButEffVisible().inRadius(3, unitToScan).isNotEmpty()) {
//            return false;
//        }

        System.err.println("=== COMSAT SCAN on " + unitToScan.shortName() + ", energy = " + comsat.energy() + " ===");
        comsat.setTooltip("Scanning " + unitToScan.shortName());
        return comsat.useTech(TechType.Scanner_Sweep, unitToScan);
    }

}
