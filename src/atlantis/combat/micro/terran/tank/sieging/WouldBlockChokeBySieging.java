package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTankWhenUnsieged;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class WouldBlockChokeBySieging extends Manager {
    public WouldBlockChokeBySieging(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankUnsieged();
    }

    public Manager handle() {
        if (
            !Enemy.terran()
                && unit.isMissionAttack()
                && Select.enemy().combatBuildings(false).inRadius(8, unit).empty()
                && unit.distToNearestChokeLessThan(3.3)
        ) {
            unit.setTooltip("DoNotBlockChoke");
            return usedManager(this);
        }

        return null;
    }
}
