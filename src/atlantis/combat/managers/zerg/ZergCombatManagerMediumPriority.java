package atlantis.combat.managers.zerg;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.micro.attack.expansion.OverrideAndAttackEnemyExpansion;
import atlantis.combat.squad.squad_scout.SquadScout;
import atlantis.terran.repair.DontMoveWhenBeingRepared;
import atlantis.terran.repair.managers.UnitBeingReparedManager;
import atlantis.units.AUnit;

public class ZergCombatManagerMediumPriority extends Manager {
    public ZergCombatManagerMediumPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SquadScout.class,

            OverrideAndAttackEnemyExpansion.class,

            WeDontKnowWhereEnemyIs.class,
        };
    }
}

