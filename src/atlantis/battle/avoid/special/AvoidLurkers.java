package atlantis.battle.avoid.special;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.We;

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

        if (zealotMustFleeBurrowing()) return true;

        lurkersToAvoid = lurkersToAvoid();
        lurker = lurkersToAvoid.inRadius(radius(), unit).nearestTo(unit);
//        if (lurker == null) lurker = allLurkers.inRadius(7.2, unit).nearestTo(unit);
        if (lurker == null) return false;

        if (avoidAsTerran()) return true;
        if (avoidAsProtoss()) return true;

        return meleeUnitAndNoMobileDetectorsNear();

//        if (beBraveWithDetectorsNearby()) return false;
//        return lurker.distTo(unit) <= (8.3 + unit.woundPercent() / 40.0)
//            || lurker.enemiesNear().combatUnits().inRadius(12, unit).atMost(dontEngageWhenAtMostFriendsNearby());
    }

    private boolean meleeUnitAndNoMobileDetectorsNear() {
        return unit.isMelee()
            && lurker.enemiesNear().mobileDetectors().countInRadius(7.5, unit) == 0;
    }

    /**
     * Owner UMS Lurkers2: a Lurker that STARTED burrowing is already
     * unhittable without a detector over it (engage only when it is
     * {@code effVisible}), and Zealots standing next to it die during
     * the transition - the burrowed-only filter below never sees it
     * coming. A burrowing Lurker nearby means Zealots leave,
     * unconditionally: no hp/army/cannon bravery vetoes apply. The
     * only exception is detector cover AT the unit (then it stays
     * hittable and the normal rules own the call).
     */
    private boolean zealotMustFleeBurrowing() {
        if (!unit.isZealot()) return false;

        AUnit burrowing = allLurkers.burrowing().inRadius(radius(), unit).nearestTo(unit);
        if (burrowing == null) return false;

        lurker = burrowing;
        twinBurrowing(unit, burrowing);

        return unit.friendsNear().mobileDetectors().countInRadius(7.5, unit) == 0;
    }

    private static final java.util.Map<Integer, String> lastBurrowTwin = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastBurrowTwinFrame = new java.util.HashMap<>();

    /**
     * Names the unconditional flee (owner UMS Lurkers2): without this row
     * a live game cannot tell "the branch never fired" from "it fired and
     * the move failed" - exactly the blindness of the 200353 vehicle
     * (8 dead, zero LURKER orders anywhere). Change-gated per unit.
     */
    private static void twinBurrowing(AUnit unit, AUnit burrowing) {
        try {
            if (unit == null || burrowing == null) return;

            int id = unit.id();
            String row = "burrowing#" + burrowing.id();
            int now = atlantis.game.A.now();
            boolean transition = !row.equals(lastBurrowTwin.get(id));
            if (!transition && now - lastBurrowTwinFrame.getOrDefault(id, -1000000) < 300) return;

            lastBurrowTwin.put(id, row);
            lastBurrowTwinFrame.put(id, now);
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.AVOID_LURKER)
                .param("unit", id)
                .param("lurker", burrowing.id())
                .param("dist", (int) (unit.distTo(burrowing) * 10))
                .param("burrowed", burrowing.isBurrowed())
                .param("transition", transition)
                .log(now, atlantis.game.A.minSec());
        } catch (Exception ignored) {
        }
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
            if (asProtossOnlyAvoidUndetectedLurkers()) {
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

    private boolean asProtossOnlyAvoidUndetectedLurkers() {
        if (unit.isMelee() && unit.eval() <= 1.9 && Count.dragoons() >= 4) return true;

        return unit.hp() >= 64 && unit.lastUnderAttackMoreThanAgo(130);
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

        return unit.friendsNear().observers().inRadius(13, unit).atLeast(2);
    }

    private int dontEngageWhenAtMostFriendsNearby() {
        if (We.protoss()) return A.supplyUsed() >= 80 ? 8 : 6;

        return 5;
    }

    @Override
    protected Manager handle() {
        if (lurker == null || !lurker.hasPosition()) return null;

        if (asTerranLoadIntoBunkers()) return usedManager(this);

        if (unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER-B!")) return usedManager(this);

        if (unit.distTo(lurker) >= 5) {
            if (unit.moveAwayFrom(lurker, 2.5, Actions.MOVE_AVOID, "LURKER-A!")) return usedManager(this);
        }

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
            + radiusBonusForTerran()
            + (unit.isWorker() ? 1.5 : 0)
            + (unit.isMelee() ? 2.5 : 0)
            + (unit.friendsNear().observers().empty() ? 1.8 : 0);
    }

    private double radiusBonusForTerran() {
        if (!We.terran()) return 0;

        return (unit.isTerranInfantry() ? 0.4 : 0)
            + (unit.isWounded() ? 0.6 : 0)
            + unit.woundPercent() / 50.0;
    }

    private double radiusBonusForProtoss() {
        if (!We.protoss()) return 0;

        if (unit.isReaver() && unit.shieldWound() <= 25 && unit.cooldown() <= 8) return -1.6;

        return (unit.shieldWound() >= 15 ? 1.2 : 0)
            + unit.woundPercent() / 40.0;
    }
}
