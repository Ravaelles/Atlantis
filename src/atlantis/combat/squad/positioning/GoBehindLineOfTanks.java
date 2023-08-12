package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class GoBehindLineOfTanks extends Manager {
    private Selection enemies;

    public GoBehindLineOfTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran()
            && !unit.isAir()
            && Count.tanks() >= 2
            && SiegeMode.isResearched()
            && (enemies = unit.enemiesNear().groundUnits().canAttack(unit, 3.1)).notEmpty()
            && (unit.isWounded() || unit.hasCooldown());
    }

    @Override
    protected Manager handle() {
        if (unit.isMissionDefend() && !Enemy.terran()) return null;

        AUnit furthestTank = Select.ourTanks().mostDistantTo(unit);
        if (furthestTank != null) {
            goToTank(furthestTank);
            return usedManager(this);
        }

        return null;
    }

    private void goToTank(AUnit tank) {
        unit.move(tank, Actions.MOVE_FORMATION, "BehindLine");
    }
}
