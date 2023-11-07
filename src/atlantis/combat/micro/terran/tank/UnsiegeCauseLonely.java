package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class UnsiegeCauseLonely extends Manager {
    public UnsiegeCauseLonely(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged();
    }

    @Override
    protected Manager handle() {
        Selection groundFriends = unit.friendsNear().groundUnits();

        if (groundFriends.havingWeapon().atMost(2)) {
            unit.setTooltip("ForeverAlone");
            if (TerranTank.wantsToUnsiege(unit)) {
                return usedManager(this);
            }
        }

        return null;
    }
}