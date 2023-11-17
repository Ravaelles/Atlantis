package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuilding;
import atlantis.combat.retreating.RetreatManager;
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
            AvoidCombatBuilding.class,
            AvoidEnemies.class,
            DontMoveWhenBeingRepared.class,
            UnitBeingReparedManager.class,
            RetreatManager.class,
            AttackNearbyEnemies.class,
        };
    }
}

