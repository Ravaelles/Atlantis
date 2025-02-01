package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class AvoidReavers extends Manager {
    public static final double BASE_AVOID_RANGE = 10.4;
    private AUnit reaver;

    public AvoidReavers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit()
            && !unit.isABuilding()
            && !unit.isABuilding()
            && !ignoreType()
            && (reaver = enemyReaver()) != null
            && unit.eval() <= 2
            && !muchMoreOurThanEnemies();
//            && !unit.isZealot()
//            && !unit.isDragoon()
//            && !unit.isMissionDefend();
    }

    private AUnit enemyReaver() {
        return unit.enemiesNear().reavers().inRadius(BASE_AVOID_RANGE + distBonus(), unit).nearestTo(unit);
    }

    private boolean muchMoreOurThanEnemies() {
        int ours = 1 + unit.friendsNear().combatUnits().inRadius(8, unit).size();
        int enemies = reaver.friendsNear().combatUnits().countInRadius(6, reaver);

        if (ours >= 2 && enemies <= 1) return true;

        return ours >= 6 && ((double) ours / enemies) >= 2.6;
    }

    private boolean ignoreType() {
        return unit.isTank();
    }

    @Override
    protected Manager handle() {
        if (enoughForcesNotToRunFromReaver(reaver)) return null;

        if (unit.isCombatUnit()) {
            Selection friendsNear = unit.friendsNear().combatUnits();
            if (
                friendsNear.inRadius(4, unit).atLeast(5) && friendsNear.inRadius(6, unit).atLeast(8)
            ) {
                return null;
            }
        }

        unit.runningManager().runFromAndNotifyOthersToMove(reaver, "REAVER!");
        return usedManager(this);
    }

    private double distBonus() {
//        if (unit.isWorker()) return 1.5;
        if (unit.hp() <= 101) return 1.5;

        return 0;
    }

    private boolean enoughForcesNotToRunFromReaver(AUnit reaver) {
        int MIN_FORCES_TO_FIGHT = We.terran() ? 11 : (We.protoss() ? 6 : 9);

        return reaver
            .enemiesNear()
            .combatUnits()
            .havingAntiGroundWeapon()
            .inRadius(16, unit)
            .atLeast(MIN_FORCES_TO_FIGHT);
    }
}
