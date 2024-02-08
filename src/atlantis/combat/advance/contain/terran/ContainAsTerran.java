package atlantis.combat.advance.contain.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.terran.tank.sieging.ForceSiege;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ContainAsTerran extends Manager {
    public ContainAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    public Manager handle() {
        if (unit.isTank()) return asTank();

        return asNonTank();
    }


    protected Manager asNonTank() {
        if (unit.noCooldown() && unit.lastUnderAttackMoreThanAgo(30) && noEnemiesInShootRange()) {
//            unit.holdPosition("Steady");
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_FORMATION, "SteadyNow");
            return usedManager(this);
        }

        AUnit target = unit.nearestOurTank();
        if (target != null && unit.distTo(target) >= 4) {
            unit.move(target, Actions.MOVE_FORMATION, "CloserToTank");
            return usedManager(this);
        }

        return null;
    }

    protected Manager asTank() {
        if (unit.isSieged() && unit.lastSiegedAgo() <= 30 * (24 + unit.id() % 6)) {
            if (unit.noCooldown() && unit.enemiesNear().groundUnits().inShootRangeOf(unit).notEmpty()) {
                (new AttackNearbyEnemies(unit)).invoke(this);
            }

            return usedManager(this, "StayHere");
        }

        if (unit.isTankUnsieged()) {
            if (unit.enemiesNearInRadius(10) == 0) {
                ForceSiege.forceSiegeNow(this, "RemainHere");
                //            WantsToSiege.wantsToSiegeNow(unit, "RemainHere");
                return usedManager(this);
            }
        }

        return null;
    }

    protected boolean noEnemiesInShootRange() {
        return unit.enemiesNear().canBeAttackedBy(unit, 1.5).empty();
    }
}
