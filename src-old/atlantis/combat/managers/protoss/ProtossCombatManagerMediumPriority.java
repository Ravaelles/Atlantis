package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.micro.attack.expansion.OverrideAndAttackEnemyExpansion;
import atlantis.combat.squad.squad_scout.SquadScout;
import atlantis.units.AUnit;

public class ProtossCombatManagerMediumPriority extends Manager {
    public ProtossCombatManagerMediumPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            SquadScout::new,

            OverrideAndAttackEnemyExpansion::new,

            WeDontKnowWhereEnemyIs::new,
        };
    }
}

