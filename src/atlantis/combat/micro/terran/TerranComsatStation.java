package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.AGameSpeed;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.TechType;

public class TerranComsatStation {

    public static boolean update(AUnit comsat) {
        if (comsat.getEnergy() < 150 || AGame.notNthGameFrame(5)) {
            return false;
        }

        if (AGame.notNthGameFrame(30)) {
            return false;
        }

        if (comsat.getEnergy() >= 50) {
            scanObservers(comsat);
        }

        return false;
    }

    private static boolean scanObservers(AUnit comsat) {
//        System.out.println(Select.enemy().invisible().ofType(AUnitType.Protoss_Observer).count() + " // " + Select.enemy().ofType(AUnitType.Protoss_Observer).count() + " // " + Select.enemy().invisible().count());
        System.err.print (Select.enemy().effCloaked().ofType(AUnitType.Protoss_Observer).count()
                + " / " + Select.enemy().effVisible().ofType(AUnitType.Protoss_Observer).count() + " ");
        for (AUnit observer : Select.enemy().effCloaked().ofType(AUnitType.Protoss_Observer).listUnits()) {
//            System.out.print(Select.ourRealUnits().inShootRangeOf(observer).count() + " ");
//            System.out.print(Select.ourRealUnits().inRadius(7, observer).count() + " ");
//            System.out.println(Select.ourRealUnits().inRadius(7, observer).count() + " // " + Select.ourRealUnits().inShootRangeOf(observer).count());
//            if (Select.ourRealUnits().inShootRangeOf(observer).count() >= 1) {
            System.out.println(Select.ourRealUnits().inRadius(7, observer).count() + " // HP:" + observer.getHP());
            if (Select.ourRealUnits().inRadius(7, observer).count() >= 1) {
                return scan(comsat, observer);
            }

        }
        System.out.println("");

        return false;
    }

    private static boolean scan(AUnit comsat, AUnit otherUnit) {
        if (otherUnit.isEffectivelyVisible()) {
            return false;
        }

//        System.out.println("Comsat scan " + otherUnit + " eff_cloaked:" + otherUnit.isEffectivelyCloaked() + " // cloaked:" + otherUnit.isCloaked());
        System.err.println("=== COMSAT SCAN ===");
        System.err.println("Scanning " + otherUnit.shortName());
        comsat.setTooltip("Scanning " + otherUnit.shortName());
        return comsat.useTech(TechType.Scanner_Sweep, otherUnit);
    }

}
