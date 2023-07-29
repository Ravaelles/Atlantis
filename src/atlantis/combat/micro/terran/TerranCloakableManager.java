package atlantis.combat.micro.terran;

import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.TechType;

public class TerranCloakableManager extends Manager {

    public TerranCloakableManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith();
    }

    @Override
    public Manager handle() {
        if (update()) {
            return usedManager(this);
        }

        return null;
    }

    public boolean update() {
        if (AGame.notNthGameFrame(7)) {
            return false;
        }

        if (unit.canCloak() && ATech.isResearched(TechType.Cloaking_Field)) {
            boolean enemiesNear = unit.enemiesNear()
                .canAttack(unit, true, true, 3)
                .isNotEmpty();
            boolean detectorsNear = Select.enemy()
                .detectors()
                .inRadius(9.1, unit)
                .isNotEmpty();

            // Not cloaked
            if (!unit.isCloaked()) {
                if (unit.energy() > 10 && enemiesNear && !detectorsNear) {
                    unit.cloak();
                    unit.setTooltipTactical("CLOAK!");
                    return true;
                }
            }

            // Cloaked
            else {
                System.out.println("CLOAKED");
                if (!enemiesNear || detectorsNear || unit.lastUnderAttackLessThanAgo(25)) {
                    unit.decloak();
                    unit.setTooltipTactical("DECLOAK");
                    return true;
                }
            }
        }

        return false;
    }

}
