package atlantis.combat.micro.terran;

import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.TechType;

public class TerranCloakableManager {

    public static boolean update(AUnit unit) {
        if (AGame.notNthGameFrame(7)) {
            return false;
        }

        if (unit.canCloak() && ATech.isResearched(TechType.Cloaking_Field)) {
            boolean enemiesNearby = unit.enemiesNearby()
                    .canAttack(unit, true, true, 3)
                    .isNotEmpty();
            boolean detectorsNearby = Select.enemy()
                    .detectors()
                    .inRadius(9.1, unit)
                    .isNotEmpty();

            // Not cloaked
            if (!unit.isCloaked()) {
                if (unit.energy() > 10 && enemiesNearby && !detectorsNearby) {
                    System.err.println(unit.name() + " CLOAKED");
                    unit.cloak();
                    unit.setTooltipTactical("CLOAK!");
                    return true;
                }
            }

            // Cloaked
            else {
                System.out.println("CLOAKED");
                if (!enemiesNearby || detectorsNearby || unit.lastUnderAttackLessThanAgo(25)) {
                    System.err.println("------------- DECLOAK");
                    unit.decloak();
                    unit.setTooltipTactical("DECLOAK");
                    return true;
                }
            }
        }

        return false;
    }

}
