package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.Select;

public class TerranCloakableManager {

    public static boolean update(AUnit unit) {
        if (AGame.notNthGameFrame(4)) {
            return false;
        }

        if (unit.canCloak()) {
            boolean enemiesNearby = Select.enemyRealUnits()
                    .inRadius(11, unit)
                    .canAttack(unit, false, true)
                    .isNotEmpty();
//            boolean detectorsNearby = true;
            boolean detectorsNearby = Select.enemy()
                    .detectors()
                    .inRadius(11.1, unit)
                    .isNotEmpty();

//            if (Select.enemy()
//                    .detectors().size() > 0) {
//                System.out.print (Select.enemy()
//                        .detectors().size() + " ");
//            }


//            System.out.println("DETECTORS = " + detectorsNearby);
//            for (AUnit enemy : Select.enemy().detectors().listUnits()) {
//                System.out.print(" " + enemy.shortName());
//            }
//            System.out.println("");
//            System.out.println("");

            // Not cloaked
            if (!unit.isCloaked()) {
                if (enemiesNearby && !detectorsNearby) {
                    unit.cloak();
                    System.out.println(unit + " cloaked");
                    unit.setTooltip("CLOAK!");
                    return true;
                }
            }

            // Cloaked
            else {
                if (!enemiesNearby || detectorsNearby || unit.isUnderAttack()) {
                    unit.decloak();
                    System.out.println(unit + " decloaked...");
                    unit.setTooltip("DECLOAK");
                    return true;
                }
            }
        }

        return false;
    }

//    private static boolean makesSenseToCloak(AUnit unit) {
//        return ;
//    }

}
