package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.production.constructions.ConstructionsCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtectScvBusyConstructing extends Manager {
    public ProtectScvBusyConstructing(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (Count.ourCombatUnits() >= 1) return false;
        if (unit.id() % 6 != 0 || unit.hp() <= 13) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (handleProtect()) return usedManager(this);

        return null;
    }

    private boolean handleProtect() {
        for (AUnit builder : ConstructionsCommander.builders()) {
            if (builder.hp() <= 34 && builder.lastUnderAttackLessThanAgo(30 * 10)) {
                AUnit nearestPeskyEnemyunit = builder.enemiesNear().inRadius(3, builder).nearestTo(unit);
                if (nearestPeskyEnemyunit != null) {
                    unit.setTooltipTactical("ProtectBuilder");
                    return unit.attackUnit(nearestPeskyEnemyunit);
                }
            }
        }

        return false;
    }
}
