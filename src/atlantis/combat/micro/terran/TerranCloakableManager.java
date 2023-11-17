package atlantis.combat.micro.terran;

import atlantis.architecture.Manager;
import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.TechType;

public class TerranCloakableManager extends Manager {
    private boolean enemiesThatCanAttackThisUnit;
    private boolean enemyDetectorsNear;

    public TerranCloakableManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith();
    }

    @Override
    protected Manager handle() {
        if (update()) {
            return usedManager(this);
        }

        return null;
    }

    public boolean update() {
        if (AGame.notNthGameFrame(7)) return false;

        if (unit.canCloak() && ATech.isResearched(TechType.Cloaking_Field)) {
            enemiesThatCanAttackThisUnit = unit.enemiesNear()
                .canAttack(unit, true, true, 10)
                .isNotEmpty();
            enemyDetectorsNear = Select.enemy()
                .detectors()
                .inRadius(13.1, unit)
                .isNotEmpty();

            // Cloaked
            if (unit.isCloaked()) {
                if (whenCloaked()) {
                    return true;
                }
            }

            // Not cloaked
            else {
                if (whenNotCloaked()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean whenNotCloaked() {
        if (unit.energy() > 50 && enemiesThatCanAttackThisUnit && !enemyDetectorsNear) {
            unit.cloak();
            unit.setTooltipTactical("CLOAK!");
            return true;
        }

        return false;
    }

    private boolean whenCloaked() {
        if (!enemiesThatCanAttackThisUnit || enemyDetectorsNear || unit.lastUnderAttackLessThanAgo(25)) {
            unit.decloak();
            unit.setTooltipTactical("DECLOAK");
            return true;
        }

        return false;
    }

}
