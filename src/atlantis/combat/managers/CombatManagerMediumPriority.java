package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.micro.attack.expansion.OverrideAndAttackEnemyExpansion;
import atlantis.combat.squad.squad_scout.SquadScout;
import atlantis.terran.repair.DontMoveWhenBeingRepared;
import atlantis.terran.repair.managers.UnitBeingReparedManager;
import atlantis.units.AUnit;

public class CombatManagerMediumPriority extends Manager {
    public CombatManagerMediumPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            TerranCombatManager.class,
//            ProtossCombatManager.class,

//            Cohesion.class,

//            UnitHasEnemyInRange.class,
//            UnitUnderAttack.class,

            SquadScout.class,

            OverrideAndAttackEnemyExpansion.class,

//            AttackNearbyEnemies.class,
//            AttackEnemiesInRange.class,

            DontMoveWhenBeingRepared.class,
            UnitBeingReparedManager.class,
//            AttackNearbyEnemies.class,

            WeDontKnowWhereEnemyIs.class,
        };
    }
}

