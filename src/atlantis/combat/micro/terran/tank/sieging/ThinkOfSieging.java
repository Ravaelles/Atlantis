package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.kursk.SiegeAgainstEnemyTanks;
import atlantis.terran.repair.managers.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ThinkOfSieging extends Manager {
    public ThinkOfSieging(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isTankSieged()) return false;
        if (unit.hasSiegedOrUnsiegedRecently()) return false;

        Selection enemiesNear = unit.enemiesNear().groundUnits();
        if (enemiesNear.inRadius(7, unit).notEmpty()) {
            if (enemiesNear.crucialUnits().inRadius(12, unit).empty()) return false;
        }

        return unit.lastActionMoreThanAgo(30 * 3 + unit.id() % 4, Actions.UNSIEGE);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SiegeAgainstEnemyTanks.class,
            SiegeVsCombatBuildings.class,
            UnitBeingReparedManager.class,
            SiegeHereDuringMissionDefend.class,
            SiegeAgainstSpecificEnemies.class,
            SiegeVsRegularBuildings.class,
            SiegeVsTerran.class,
            SiegeVsRegularEnemies.class,
            GoodDistanceToContainFocusPoint.class,
        };
    }
}
