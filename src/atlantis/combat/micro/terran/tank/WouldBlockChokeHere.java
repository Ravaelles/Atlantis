package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class WouldBlockChokeHere extends Manager {
    public WouldBlockChokeHere(AUnit unit) {
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
                && Select.enemy().combatBuildings(false).inRadius(TerranTankWhenUnsieged.COMBAT_BUILDING_DIST_SIEGE, unit).empty()
                && unit.distToNearestChokeLessThan(2)
        ) {
            unit.setTooltip("DoNotBlockChoke");
            return usedManager(this);
        }

        return null;
    }
}
