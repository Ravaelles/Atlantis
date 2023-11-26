package atlantis.combat.micro.terran.tank.unsieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class SiegeTankRunCritical extends Manager {
    public SiegeTankRunCritical(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged()
            &&
            (
                darkTemplarsVeryNear() || manyMeleeNear()
            );
    }

    private boolean manyMeleeNear() {
        double minDist = A.inRange(2, 3 + unit.woundPercent() / 30.0 - unit.friendsNear().count() / 7.0, 5);
        int minCount = unit.isWounded() ? 1 : 2;

        return unit.enemiesNear().groundUnits().havingWeapon().inRadius(minDist, unit).count() >= minCount;
    }

    private boolean darkTemplarsVeryNear() {
        return unit.enemiesNear().ofType(AUnitType.Protoss_Dark_Templar).inRadius(3, unit).notEmpty()
            && unit.friendsNear().havingAntiGroundWeapon().inRadius(7, unit).atMost(3);
    }

    protected Manager handle() {
        TerranTank.forceUnsiege(unit);
        unit.setTooltip("EvacuateNow");
        return usedManager(this);
    }
}
