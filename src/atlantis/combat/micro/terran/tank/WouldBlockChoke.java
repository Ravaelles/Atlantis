package atlantis.combat.micro.terran.tank;

import atlantis.units.AUnit;
import atlantis.architecture.Manager;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class WouldBlockChoke extends Manager {

    public WouldBlockChoke(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (
            !Enemy.terran()
            && unit.isMissionAttack()
            && Select.enemy().combatBuildings(false).inRadius(TerranTankWhenNotSieged.COMBAT_BUILDING_DIST_SIEGE, unit).empty()
            && unit.distToNearestChokeLessThan(2)
        ) {
            unit.setTooltip("DoNotBlockChoke");
            return usedManager(this);
        }

        return null;
    }
}
