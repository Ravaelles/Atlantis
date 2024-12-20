package atlantis.combat.micro.attack.expansion;

import atlantis.architecture.Manager;
import atlantis.combat.missions.attack.focus.EnemyExistingExpansion;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class OverrideAndAttackEnemyExpansion extends Manager {

    private HasPosition expansion;

    public OverrideAndAttackEnemyExpansion(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit()
            && unit.isMissionAttack()
            && (expansion = EnemyExistingExpansion.get()) != null
            && rightDistance()
            && noReaverNear()
            && (A.s % 5 <= 0 || notTooManyEnemiesNearby())
            && notTooManyCBsNear();
    }

    private boolean noReaverNear() {
        return unit.friendsNear().reavers().countInRadius(8, unit) <= 0;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            OnTheWayAttackEnemiesInRange.class,
            ForceGoToEnemyExpansion.class,
        };
    }

    private boolean notLosingFormation() {
        AUnit leader = unit.squadLeader();
        return leader != null && unit.distTo(leader) < 10;
    }

    private boolean notTooManyEnemiesNearby() {
//        if (unit.shieldWound() <= 10 && unit.lastUnderAttackMoreThanAgo(30 * 5)) return true;

        return unit.enemiesNear().inRadius(4.7, unit).count() <= 0
            && unit.enemiesNear().canAttack(unit, 0.5).count() <= 1
            && unit.enemiesNear().canAttack(unit, 1.5).count() <= 2;
    }

    private boolean notTooManyCBsNear() {
        return unit.enemiesNear().combatBuildingsAntiLand().countInRadius(9, unit) <= 1;
    }

    private boolean rightDistance() {
        double dist = unit.distTo(expansion);

        return dist > 3 && dist <= 25;
    }
}
