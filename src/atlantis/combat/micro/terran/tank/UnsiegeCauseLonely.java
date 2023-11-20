package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.SiegeAgainstSpecificEnemies;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class UnsiegeCauseLonely extends Manager {
    public UnsiegeCauseLonely(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged() && unit.lastSiegedOrUnsiegedAgo() > 30 * 7;
    }

    @Override
    protected Manager handle() {
        Selection groundFriends = unit.friendsNear().groundUnits();

        if (isTooLonely(groundFriends)) {
            unit.setTooltip("ForeverAlone");

//            if (TerranTank.wantsToUnsiege(unit)) {
            if (TerranTank.forceUnsiege(unit)) return usedManager(this);
        }

        return null;
    }

    private boolean noSpecialSiegeOrder() {
        return !(new SiegeAgainstSpecificEnemies(unit)).applies();
//        (new TankCrucialTargeting(unit, unit.enemiesNear())).crucialTarget() == null)
    }

    private boolean isTooLonely(Selection groundFriends) {
        return (groundFriends.havingWeapon().atMost(4) || tooManyEnemies(groundFriends))
            && (noSpecialSiegeOrder() || unit.meleeEnemiesNearCount(3) > 0);
    }

    private boolean tooManyEnemies(Selection groundFriends) {
        return unit.noCooldown() && groundFriends.inRadius(8, unit).atMost(unit.enemiesNear().size() + 1);
    }
}
