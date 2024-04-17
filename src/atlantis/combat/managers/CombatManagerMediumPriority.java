package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.generic.UnitUnderAttack;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.combat.micro.avoid.ContinueRunning;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuilding;
import atlantis.combat.running.ShouldStopRunning;
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
            UnitUnderAttack.class,

            ContinueRunning.class,
            ShouldStopRunning.class,

            AvoidCombatBuilding.class,
            AvoidEnemiesIfNeeded.class,
            DontMoveWhenBeingRepared.class,
            UnitBeingReparedManager.class,
//            AttackNearbyEnemies.class,

            SquadScout.class,

            WeDontKnowWhereEnemyIs.class,
        };
    }
}

