package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AvoidLurkers extends Manager {

    private AUnit lurker;

    public AvoidLurkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isGroundUnit() || unit.isABuilding()) return false;

        if (beMoreBraveWithBigProtossArmy()) return false;

        lurker = unit.enemiesNear().lurkers().effUndetected().notHavingHp(1).inRadius(radius(), unit).nearestTo(unit);
        if (lurker == null) return false;

        if (Count.cannons() >= 1 && lurker.enemiesNear().buildings().cannons().countInRadius(6.4, lurker) > 0) {
            return false;
        }

        if (
            unit.hp() >= 80
                && unit.friendsNear().atLeast(10)
                && unit.enemiesNear().lurkers().inRadius(12, unit).count() <= 1
        ) {
            return false;
        }

//        if (beBraveWithDetectorsNearby()) return false;

        return lurker.distTo(unit) <= (8.2 + unit.woundPercent() / 40.0)
            || lurker.enemiesNear().combatUnits().inRadius(12, unit).atMost(dontEngageWhenAtMostFriendsNearby());
//        return unit.woundPercent() >= 10
//            || lurker.enemiesNear().combatUnits().inRadius(12, unit).atMost(dontEngageWhenAtMostFriendsNearby());
    }

    private boolean beMoreBraveWithBigProtossArmy() {
        if (!We.protoss()) return false;
        if (Count.observers() <= 1) return false;
        if (unit.shields() <= 5) return false;
        if (unit.friendsNear().combatUnits().atLeast(14)) return false;

        if (unit.friendsNear().observers().inRadius(13, unit).atLeast(2)) return true;

        return false;
    }

    private int dontEngageWhenAtMostFriendsNearby() {
        if (We.protoss()) return A.supplyUsed() >= 80 ? 8 : 6;

        return 5;
    }

    @Override
    protected Manager handle() {
        if (lurker == null || !lurker.hasPosition()) return null;

        if (unit.distTo(lurker) >= 5) {
            if (unit.moveAwayFrom(lurker, 2.5, Actions.MOVE_AVOID, "LURKER-A!")) return usedManager(this);
        }

        if (unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER-B!")) return usedManager(this);

        return null;
    }

//    private boolean beBraveWithDetectorsNearby() {
////        if (unit.combatEvalRelative() < 1.7) return false;
//        if (lurker.enemiesNear().detectors().inRadius(8, unit).empty()) return false;
//
//        return true;
//    }

    private double radius() {
        return 8.1
            + meleeDistBonus()
            + noDetectorsBonus()
            + unit.woundPercent() / 65.0;
    }

    private double noDetectorsBonus() {
        return unit.friendsNear().observers().empty() ? 1.8 : 0;
    }

    private double meleeDistBonus() {
        return unit.isMelee() ? 2.5 : 0;
    }
}
