package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.combat.generic.enemy_in_range.AttackEnemiesInRange;
import atlantis.combat.generic.enemy_in_range.UnitHasEnemyInRange;
import atlantis.combat.generic.under_attack.UnitUnderAttack;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuilding;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildingCriticallyClose;
import atlantis.combat.micro.avoid.buildings.ProtossDontEngageWhenCombatBuildings;
import atlantis.combat.micro.avoid.buildings.TerranDontEngageWhenCombatBuildings;
import atlantis.combat.running.stop_running.ShouldStopRunning;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonely;
import atlantis.combat.squad.positioning.too_lonely.ProtossTooLonelyGetCloser;
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
            ProtossDontEngageWhenCombatBuildings.class,
            TerranDontEngageWhenCombatBuildings.class,

            ProtossTooLonelyGetCloser.class,
            ProtossTooLonely.class,

//            UnitHasEnemyInRange.class,
//            UnitUnderAttack.class,

            AttackNearbyEnemies.class,
            AttackEnemiesInRange.class,

//            ShouldStopRunning.class,
            AvoidEnemies.class,

            DontMoveWhenBeingRepared.class,
            UnitBeingReparedManager.class,
//            AttackNearbyEnemies.class,

            SquadScout.class,

            WeDontKnowWhereEnemyIs.class,
        };
    }
}

