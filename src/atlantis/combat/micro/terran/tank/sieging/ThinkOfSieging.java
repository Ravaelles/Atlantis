package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ThinkOfSieging extends Manager {
    public ThinkOfSieging(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isTankSieged()) return false;

        return unit.lastActionMoreThanAgo(30 * 3 + unit.id() % 4, Actions.UNSIEGE);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidCloseEnemiesToUnsiegedTank.class,
            SiegeAgainstCombatBuildings.class,
            UnitBeingReparedManager.class,
            SiegeHereDuringMissionDefend.class,
            SiegeAgainstEnemyTanks.class,
            SiegeBecauseSpecificEnemiesNear.class,
            SiegeAgainstRegularBuildings.class,
            SiegeAgainstRegularEnemies.class,
            GoodDistanceToContainFocusPoint.class,
        };
    }
}
