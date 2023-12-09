package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public class DontAvoidEnemyWhenCloseToTank extends Manager {
    public DontAvoidEnemyWhenCloseToTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (unit.hasNoWeaponAtAll()) return false;
        if (unit.hp() <= 18) return false;
        if (unit.hasCooldown()) return false;

        return unit.friendsNear().tanksSieged().inRadius(4, unit).count() > 0;
    }

    @Override
    public Manager handle() {
        (new AttackNearbyEnemies(unit)).forceHandle();

        return usedManager(this);
    }
}
