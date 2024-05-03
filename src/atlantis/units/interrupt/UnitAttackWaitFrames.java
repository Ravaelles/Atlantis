package atlantis.units.interrupt;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * This is a topic
 * <p>
 * Taken from:
 * https://github.com/dgant/PurpleWave/blob/5ba03a56dd21a3cbd41899efc992d6c7d2ddb5d5/src/ProxyBwapi/UnitClasses/UnitClass.scala
 */
public class UnitAttackWaitFrames {

//    public static boolean unitAlreadyStartedAttackAnimation(AUnit unit) {
//        return unit.isAttackingOrMovingToAttack()
//                && unit.getTarget() != null
//                && (unit.lastFrameOfStartingAttackAgo() + stopFrames(unit.type())) > A.now();
//    }

    public static boolean waitedLongEnoughForAttackFrameToFinish(AUnit unit) {
//        return unit.lastAttackFrameAgo() > attackAnimationFrames(unit.type());
        return unit.lastAttackFrameAgo()
            >
            (attackAnimationFrames(unit.type()) + effectiveStopFrames(unit.type()));
    }

    public static boolean waitedLongEnoughForStartedAttack(AUnit unit) {
        return !(unit.lastFrameOfStartingAttackAgo() <= effectiveStopFrames(unit.type()));
    }

    public static boolean unitAlreadyStartedAttackAnimation(AUnit unit) {
//        return unit.isAttackingOrMovingToAttack()
//        return unit.isAttackingOrMovingToAttack()
//                && unit.hasValidTarget()
//                && unit.lastFrameOfStartingAttackAgo() < (unit.cooldownAbsolute() / 3)
//                && (unit.lastFrameOfStartingAttackAgo() + stopFrames(unit.type())) > A.now();

//        System.err.println("A = " + unit.lastFrameOfStartingAttackAgo());
//        System.err.println("B = " + unit.lastFrameOfAttackFrameAgo());
//        System.err.println("C = " + effectiveStopFrames(unit.type()));

        // @Check Interesting - this works for Dragoons (when it's disabled)
//        if (unit.lastFrameOfStartingAttackAgo() <= effectiveStopFrames(unit.type())) return true;

        if (waitedLongEnoughForAttackFrameToFinish(unit)) return true;

        // @Check
//        return unit.cooldownRemaining() > 0 && unit.isAttackingOrMovingToAttack();
        return false;
    }

    public static int effectiveStopFrames(AUnitType type) {
        return stopFrames(type) + attackAnimationFrames(type);
    }

    public static int stopFrames(AUnitType type) {
        if (type.equals(AUnitType.Protoss_Arbiter)) return 4;
        else if (type.equals(AUnitType.Protoss_Archon)) return 15;
        else if (type.equals(AUnitType.Protoss_Corsair)) return 8;
        else if (type.equals(AUnitType.Protoss_Dark_Templar)) return 9;
        else if (type.equals(AUnitType.Protoss_Dragoon)) return 7; // Original 7
        else if (type.equals(AUnitType.Protoss_Photon_Cannon)) return 7;
        else if (type.equals(AUnitType.Protoss_Probe)) return 2;
        else if (type.equals(AUnitType.Protoss_Reaver)) return 1;
        else if (type.equals(AUnitType.Protoss_Scout)) return 2;
        else if (type.equals(AUnitType.Protoss_Zealot)) return 7;
        else if (type.equals(AUnitType.Terran_SCV)) return 2;
        else if (type.equals(AUnitType.Terran_Battlecruiser)) return 2;
        else if (type.equals(AUnitType.Terran_Firebat)) return 8;
        else if (type.equals(AUnitType.Terran_Ghost)) return 3;
        else if (type.equals(AUnitType.Terran_Goliath)) return 1;
        else if (type.equals(AUnitType.Terran_Marine)) return 8; // original 8
        else if (type.equals(AUnitType.Terran_Siege_Tank_Siege_Mode)) return 1;
        else if (type.equals(AUnitType.Terran_Siege_Tank_Tank_Mode)) return 1;
        else if (type.equals(AUnitType.Terran_Valkyrie)) return 40;
        else if (type.equals(AUnitType.Terran_Vulture)) return 2;
        else if (type.equals(AUnitType.Terran_Wraith)) return 2;
        else if (type.equals(AUnitType.Zerg_Devourer)) return 9;
        else if (type.equals(AUnitType.Zerg_Drone)) return 2;
        else if (type.equals(AUnitType.Zerg_Hydralisk)) return 3;
        else if (type.equals(AUnitType.Zerg_Lurker)) return 2;
        else if (type.equals(AUnitType.Zerg_Mutalisk)) return 2;
        else if (type.equals(AUnitType.Zerg_Ultralisk)) return 14;
        else if (type.equals(AUnitType.Zerg_Zergling)) return 4;
        else return 2; // Arbitrary
    }

//    protected static int minStop(AUnitType type) {
////        if (type.equals(AUnitType.Protoss_Dragoon)) return 5; // Original
//        if (type.equals(AUnitType.Protoss_Dragoon)) return 6; // Raised to 6, still some glitches
//        else if (type.equals(AUnitType.Protoss_Carrier)) return 48;
//        else if (type.equals(AUnitType.Zerg_Devourer)) return 7;
//        else return 0;
//    }

    protected static int attackAnimationFrames(AUnitType type) {
        if (type.equals(AUnitType.Protoss_Arbiter)) return 5;
        else if (type.equals(AUnitType.Protoss_Corsair)) return 8;
        else if (type.equals(AUnitType.Protoss_Dark_Templar)) return 9;
        else if (type.equals(AUnitType.Protoss_Dragoon)) return 9; // Original 9
        else if (type.equals(AUnitType.Protoss_Reaver)) return 1;
        else if (type.equals(AUnitType.Protoss_Zealot)) return 9; // Original 8
        else if (type.equals(AUnitType.Terran_SCV)) return 2;
        else if (type.equals(AUnitType.Terran_Firebat)) return 8;
        else if (type.equals(AUnitType.Terran_Ghost)) return 4;
        else if (type.equals(AUnitType.Terran_Goliath)) return 1;
        else if (type.equals(AUnitType.Terran_Marine)) return 9; // Original 8
        else if (type.equals(AUnitType.Terran_Siege_Tank_Siege_Mode)) return 1;
        else if (type.equals(AUnitType.Terran_Siege_Tank_Tank_Mode)) return 1;
        else if (type.equals(AUnitType.Zerg_Hydralisk)) return 3;
        else if (type.equals(AUnitType.Zerg_Ultralisk)) return 15;
        else if (type.equals(AUnitType.Zerg_Zergling)) return 5;
        else return 0;
    }

}
