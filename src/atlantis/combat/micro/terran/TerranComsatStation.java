package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.TechType;

public class TerranComsatStation {

    public static boolean update(AUnit comsat) {
        if (AGame.notNthGameFrame(5 + ((250 - comsat.energy()) / 3))) {
            return false;
        }
//        System.out.println(Select.enemy().ofType(AUnitType.Protoss_Observer).count());

        if (comsat.energy() >= 50) {
            return scanDarkTemplars(comsat)
                    || scanObservers(comsat)
                    || scanLurkers(comsat);
        }

        return false;
    }

    // =========================================================
    // Zerg

    private static boolean scanLurkers(AUnit comsat) {
        for (AUnit lurker : Select.enemy().effCloaked().ofType(AUnitType.Zerg_Lurker).listUnits()) {
            System.out.println(lurker + " // " + lurker.effVisible() + " // " + lurker.hp());
            if (shouldScanThisLurker(lurker, comsat)) {
                return scan(comsat, lurker);
            }
        }

        return false;
    }

    private static boolean shouldScanThisLurker(AUnit lurker, AUnit comsat) {
        return Select.ourRealUnits().canAttack(lurker, 3).atLeast(6) || comsat.energy() >= 180;
    }

    // =========================================================
    // Protoss

    private static boolean scanDarkTemplars(AUnit comsat) {
        for (AUnit dt : Select.enemy().effCloaked().ofType(AUnitType.Protoss_Dark_Templar).listUnits()) {
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

    // =========================================================

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
