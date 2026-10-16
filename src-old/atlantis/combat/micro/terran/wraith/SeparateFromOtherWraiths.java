package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class SeparateFromOtherWraiths extends Manager {
    private Selection otherWraiths;

    public SeparateFromOtherWraiths(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith()
            && unit.noCooldown()
            && !unit.isRunning()
            && A.seconds() % 8 <= 5
            && otherAirUnitsNear().atLeast(1)
            && unit.lastStartedAttackMoreThanAgo(30 * 3);
    }

    private Selection otherAirUnitsNear() {
        return otherWraiths = unit.friendsNear()
            .ofType(AUnitType.Terran_Wraith)
            .inRadius(10, unit);
    }

    @Override
    public Manager handle() {
        AUnit otherWraith = otherWraiths.nearestTo(unit);

        if (otherWraith != null) {
            unit.moveAwayFrom(otherWraith, 8, Actions.MOVE_SPACE, "SeparateFromWraith");
            return usedManager(this);
        }

        return null;
    }
}
