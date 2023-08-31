package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.combat.targeting.air.AAirUnitAirTargets;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AttackOtherAirUnits extends Manager {
    private Selection otherAirEnemiesNear;

    public AttackOtherAirUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && unit.hasAirWeapon()
            && (otherAirEnemiesNear = otherAirEnemiesNear()).notEmpty();
//            && unit.enemiesNear().groundUnits().canAttack(unit, 2.5).empty();
    }

    private Selection otherAirEnemiesNear() {
        return unit.enemiesNear()
            .air()
            .havingAntiAirWeapon()
            .canBeAttackedBy(unit, 3.5)
            .effVisible();
    }

    @Override
    public Manager handle() {
        AUnit airTarget = (new AAirUnitAirTargets(unit)).targetsAir(otherAirEnemiesNear);

//        System.err.println("@ " + A.now() + " - " + unit.nameWithId() + " airTarget = " + airTarget);

        if (airTarget == null) return null;

        unit.attackUnit(airTarget);
        return usedManager(this);
    }
}
