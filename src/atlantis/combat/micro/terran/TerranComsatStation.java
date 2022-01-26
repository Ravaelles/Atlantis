package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;

public class TerranComsatStation {

    public static boolean update(AUnit comsat) {
        if (AGame.notNthGameFrame(15)) {
            return false;
        }

        if (comsat.energy() >= 50) {
            return scanLurkers(comsat)
                    || scanDarkTemplars(comsat)
                    || scanObservers(comsat);
        }

        return false;
    }

    // =========================================================
    // Zerg

    private static boolean scanLurkers(AUnit comsat) {
        for (AUnit lurker : Select.enemies(AUnitType.Zerg_Lurker).effCloaked().list()) {
            if (shouldScanThisLurker(lurker, comsat)) {
                return scan(comsat, lurker);
            }
        }

        return false;
    }

    private static boolean shouldScanThisLurker(AUnit lurker, AUnit comsat) {
        if (comsat.energy(190)) {
            return true;
        }

        int minUnitsNearby = (comsat.energy(160) ? 3 : (comsat.energy(60) ? 4 : 6));

        if (comsat.energy(100) && Select.ourBuildingsIncludingUnfinished().inRadius(6.5, lurker).isNotEmpty()) {
//            System.err.println("Scan " + lurker + " because buildings are close");
            return true;
        }

        return Select.ourCombatUnits()
                .excludeTypes(AUnitType.Terran_Medic)
                .inRadius(12, lurker)
                .atLeast(minUnitsNearby);
    }

    // =========================================================
    // Protoss

    private static boolean scanDarkTemplars(AUnit comsat) {
        for (AUnit dt : Select.enemy().effCloaked().ofType(AUnitType.Protoss_Dark_Templar).list()) {
            if (Select.ourCombatUnits().excludeTypes(AUnitType.Terran_Medic).inRadius(8, dt)
                    .atLeast(comsat.energy(150) ? (comsat.energy(190) ? 2 : 4) : 7)) {
                if (Select.ourCombatUnits().nearestTo(dt).distToLessThan(dt, 6)) {
                    return scan(comsat, dt);
                }
            }
        }

        return false;
    }

    private static boolean scanObservers(AUnit comsat) {
        if (comsat.energy() <= 200) {
            return false;
        }

        for (AUnit observer : Select.enemy().effCloaked().ofType(AUnitType.Protoss_Observer).list()) {
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

    // =========================================================

    private static boolean scan(AUnit comsat, AUnit unitToScan) {
        if (!unitToScan.effCloaked()) {
            System.err.println("unitToScan is not effectively cloaked = " + unitToScan.hp());
            return false;
        }

//        if (unitToScan.isEffectivelyCloaked() && Select.enemy().exclude(unitToScan)
//                .cloakedButEffVisible().inRadius(3, unitToScan).isNotEmpty()) {
//            return false;
//        }

        if (!(unitToScan instanceof FakeUnit)) {
//            System.err.println("=== COMSAT SCAN on " + unitToScan + ", energy = " + comsat.energy() + " ===");
        }
        comsat.setTooltipTactical("Scanning " + unitToScan.name());
        return comsat.useTech(TechType.Scanner_Sweep, unitToScan);
    }

}
