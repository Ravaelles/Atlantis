package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.We;
import bwapi.Color;

public class AvoidLurkers extends Manager {

    private AUnit lurker;
    private Selection lurkersToAvoid;
    private Selection allLurkers;

    public AvoidLurkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isGroundUnit() || unit.isABuilding()) return false;

//        allLurkers = EnemyUnits.discovered().lurkers();
        allLurkers = unit.enemiesNear().lurkers();
        if (allLurkers.empty()) return false;

        lurkersToAvoid = lurkersToAvoid();
        lurker = lurkersToAvoid.inRadius(radius(), unit).nearestTo(unit);
//        if (lurker == null) lurker = allLurkers.inRadius(7.2, unit).nearestTo(unit);
        if (lurker == null) return false;

        if (avoidAsTerran()) return true;
        if (avoidAsProtoss()) return true;
        if (meleeUnitAndNoMobileDetectorsNear()) return true;

//        if (beBraveWithDetectorsNearby()) return false;

        return false;
//        return lurker.distTo(unit) <= (8.3 + unit.woundPercent() / 40.0)
//            || lurker.enemiesNear().combatUnits().inRadius(12, unit).atMost(dontEngageWhenAtMostFriendsNearby());
    }

    private boolean meleeUnitAndNoMobileDetectorsNear() {
        return unit.isMelee()
            && lurker.enemiesNear().mobileDetectors().countInRadius(7.5, unit) == 0;
    }

    private Selection lurkersToAvoid() {
//        Selection allLurkers = EnemyUnits.discovered().lurkers().havingCooldownMax(13);
//        for (AUnit lurker : allLurkers.list()) {
//            if (lurker.cooldown() > 0) System.out.println("LURKER COOLDOWN: " + lurker.cooldown() + " / " + lurker.isBurrowed());

//            System.out.println(
//                lurker + " / BRD:" + lurker.isBurrowed()
//                    + " / BING:" + lurker.isBurrowing()
//                    + " / INT:" + lurker.isInterruptible()
//            );
//            if (lurker.isBurrowing()) lurker.paintCircleFilled(12, Color.Orange);
//        }

//        Selection lurkersBurrowed = allLurkers.burrowed();
//        for (AUnit lurker : lurkersBurrowed.list()) {
//            lurker.paintCircleFilled(10, Color.Blue);
//        }

//        Selection lurkers = allLurkers.effUndetected().add(allLurkers.burrowing()).add(allLurkers.burrowed());
        Selection lurkers = allLurkers.add(allLurkers.burrowed());
//        for (AUnit lurker : lurkers.list()) {
//            lurker.paintCircleFilled(8, Color.Green);
//        }

        if (We.protoss()) {
            if (unit.hp() >= 64 && unit.lastUnderAttackMoreThanAgo(130)) {
                lurkers = lurkers.effUndetected();
            }
            else {
                lurkers = lurkers.burrowed();
            }
        }
        else {
            lurkers = lurkers.effUndetected();
        }

        return lurkers;
    }

    private boolean avoidAsTerran() {
        if (!We.terran()) return false;
        if (unit.isStartingAttack() || unit.isAttackFrame()) return false;

        if (unit.hp() <= 22) return true;
//        if (unit.cooldown() <= 6) return true;

        if (dontAvoidNearBunker()) return false;

        if (multipleLurkersNearby()) return true;

        double distTo = lurker.distTo(unit);
        if (distTo <= 3.9 && unit.friendsNear().havingWeapon().atMost(8)) return true;

        return distTo <= (6.2 + unit.woundPercent() / 40.0)
            || lurker.enemiesNear().combatUnits().havingWeapon().inRadius(12, unit).atMost(dontEngageWhenAtMostFriendsNearby());
    }

    private boolean multipleLurkersNearby() {
        return EnemyUnits.discovered().lurkers().countInRadius(7.2, unit) >= 2;
    }

    private boolean avoidAsProtoss() {
        if (!We.protoss()) return false;

        if (beMoreBraveWithBigProtossArmy()) return false;
        if (dontAvoidNearCannons()) return false;

        if (
            unit.hp() >= 80
                && unit.friendsNear().atLeast(10)
                && unit.enemiesNear().lurkers().inRadius(12, unit).count() <= 1
        ) {
            return false;
        }

        return lurker.distTo(unit) <= (8.2 + unit.woundPercent() / 40.0)
            || lurker.enemiesNear().combatUnits().inRadius(12, unit).atMost(dontEngageWhenAtMostFriendsNearby());
    }

    private boolean dontAvoidNearBunker() {
        return We.terran()
            && unit.hp() >= 34
            && Count.bunkers() >= 1
            && lurker.enemiesNear().buildings().countInRadius(6, lurker) > 0;
    }

    private boolean dontAvoidNearCannons() {
        return We.protoss()
            && Count.cannons() >= 1
            && lurker.enemiesNear().buildings().cannons().countInRadius(6.4, lurker) > 0;
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

        if (asTerranLoadIntoBunkers()) return usedManager(this);

        if (unit.distTo(lurker) >= 5) {
            if (unit.moveAwayFrom(lurker, 2.5, Actions.MOVE_AVOID, "LURKER-A!")) return usedManager(this);
        }

        if (unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER-B!")) return usedManager(this);

        return null;
    }

//    private boolean beBraveWithDetectorsNearby() {

    /// /        if (unit.combatEvalRelative() < 1.7) return false;
//        if (lurker.enemiesNear().detectors().inRadius(8, unit).empty()) return false;
//
//        return true;
//    }
    private boolean asTerranLoadIntoBunkers() {
        if (!We.terran()) return false;
        if (!unit.isMarine()) return false;

        AUnit bunker = unit.friendsNear().bunkers().havingSpaceFree(1).nearestTo(unit);
        if (bunker == null) return false;

        if (unit.load(bunker)) {
            unit.addLog("HideFromLurker");
            return true;
        }

        return false;
    }

    private double radius() {
        return 8.1
            + radiusBonusForProtoss()
            + (unit.isTerranInfantry() ? 0.4 : 0)
            + (unit.isWorker() ? 1.5 : 0)
            + (unit.isMelee() ? 2.5 : 0)
            + (unit.friendsNear().observers().empty() ? 1.8 : 0)
            + unit.woundPercent() / 65.0;
    }

    private double radiusBonusForProtoss() {
        if (!We.protoss()) return 0;

        if (unit.isReaver() && unit.shieldWound() <= 25 && unit.cooldown() <= 8) return -1.6;

        return 0;
    }
}
