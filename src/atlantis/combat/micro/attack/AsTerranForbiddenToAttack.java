package atlantis.combat.micro.attack;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AsTerranForbiddenToAttack extends HasUnit {
    public AsTerranForbiddenToAttack(AUnit unit) {
        super(unit);
    }

    public boolean asTerranForbiddenToAttack() {
        if (unit.isMissionDefend()) return false;

        if (unit.isAir()) return false;

        if (Count.tanks() < 2) return false;

        if (unit.friendsInRadius(2).combatUnits().count() >= 7) return false;

        AUnit nearestTank = Select.ourTanks().nearestTo(unit);
        if (
            nearestTank.distTo(unit) >= 9
                && (unit.isWounded() || unit.enemiesNear().combatUnits().inRadius(12, unit).notEmpty())
        ) return false;

        Selection enemiesNear = unit.enemiesNear().groundUnits().effVisible();
        if (enemiesNear.inRadius(unit.groundWeaponRange(), unit).notEmpty() && unit.noCooldown()) {
            return false;
        }
        
        if (nearestTank.distTo(enemiesNear.nearestTo(nearestTank)) <= 12) return false;

        return true;
    }
}