package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.combat.targeting.air.AAirUnitAirTargets;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AttackWorkersWhenItMakesSense extends Manager {
    private Selection enemyWorkers;

    public AttackWorkersWhenItMakesSense(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir()
            && unit.hpMoreThan(40)
            && unit.hasGroundWeapon()
            && unit.lastUnderAttackMoreThanAgo(30 * 5)
            && (enemyWorkers = enemyWorkersNear()).notEmpty();
    }

    private Selection enemyWorkersNear() {
        return unit.enemiesNear()
            .workers()
            .canBeAttackedBy(unit, 0.5)
            .effVisible();
    }

    @Override
    public Manager handle() {
        AUnit target = enemyWorkers.mostWounded();

        if (target == null) return null;

        unit.attackUnit(target);
        return usedManager(this);
    }
}
