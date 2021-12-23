package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.wrappers.ATech;
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
                    System.err.println(unit.shortName() + " CLOAKED");
                    unit.cloak();
                    unit.setTooltip("CLOAK!");
                    return true;
                }
            }

            // Cloaked
            else {
                System.out.println("CLOAKED");
                if (!enemiesNearby || detectorsNearby || unit.lastUnderAttackLessThanAgo(25)) {
                    System.err.println("------------- DECLOAK");
                    unit.decloak();
                    unit.setTooltip("DECLOAK");
                    return true;
                }
            }
        }

        return false;
    }

}
