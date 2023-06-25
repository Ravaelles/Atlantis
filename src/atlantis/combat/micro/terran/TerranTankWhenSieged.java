package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranTankWhenSieged extends TerranTank {

    protected static boolean updateSieged(AUnit unit) {
//        if (handleShootingAtInvisibleUnits(unit)) {
//            return true;
//        }

        // Get the hell outta here
        if (
            unit.lastUnderAttackLessThanAgo(30)
            && (unit.hp() >= 100 || unit.enemiesNearInRadius(2) <= 2)
        ) {
            if (unit.enemiesNear().groundUnits().inRadius(3, unit).count() >= (unit.hpPercent() >= 50 ? 2 : 1)) {
                unit.setTooltip("Evacuate");
                unit.unsiege();
                return true;
            }
        }

        if (wouldBlockChoke(unit)) {
            unit.setTooltip("DoNotBlockChoke");
            unit.unsiege();
            return true;
        }

        if (shouldNotThinkAboutUnsieging(unit)) {
            return false;
        }

        if (shouldSiegeHereDuringMissionDefend(unit)) {
            return true;
        }

        if (
            unit.enemiesNear().combatUnits().empty()
                && unit.lastAttackFrameMoreThanAgo(30 * 3 + (unit.id() % 3))
                && unit.distToSquadCenter() >= 8
        ) {
            return wantsToUnsiege(unit, "Reposition");
        }

        if (allowToUnsiegeToMove(unit)) {
            return wantsToUnsiege(unit, "WantsToMove");
        }

        // Mission is CONTAIN
        if (
            Missions.isGlobalMissionContain()
                && unit.squad().distToFocusPoint() < 7.9
                && unit.lastAttackOrderLessThanAgo(7 * 30)
        ) {
            return false;
        }

//        if (
//            tooLonely(unit)
//                && !hasJustSiegedRecently(unit)
//                && unit.noCooldown()
//                && unit.lastStartedAttackMoreThanAgo(140)
//        ) {
////            System.out.println("LAST SIEGE = " + unit.lastActionAgo(Actions.SIEGE));
////            System.out.println("LAST UNSIEGE = " + unit.lastActionAgo(Actions.UNSIEGE));
//            return wantsToUnsiege(unit, "TooLonely");
//        }

        return false;
    }

    public static boolean wouldBlockChoke(AUnit unit) {
        return !Enemy.terran()
            && unit.isMissionAttack()
            && Select.enemy().combatBuildings(false).inRadius(TerranTankWhenNotSieged.COMBAT_BUILDING_DIST_SIEGE, unit).empty()
            && unit.distToNearestChokeLessThan(2);
    }

    // =========================================================

    private static boolean shouldNotThinkAboutUnsieging(AUnit unit) {
//        unit.setTooltip(unit + ", SIEGED ago = " + unit.lastActionAgo(Actions.SIEGE), false);
//        unit.setTooltip(unit + ", UNSIEGED ago = " + unit.lastActionAgo(Actions.UNSIEGE), false);

        if (unit.cooldownRemaining() > 0) {
            return true;
        }

        if (unit.lastActionLessThanAgo(30 * (5 + (unit.idIsOdd() ? 3 : 0)), Actions.SIEGE)) {
            return true;
        }

        return false;
    }

    public static boolean shouldSiegeHereDuringMissionDefend(AUnit unit) {
        if (unit.isMissionDefendOrSparta() && unit.distToFocusPoint() <= 6) {
            if (unit.target() == null || unit.target().distTo(unit) < 12) {
                return true;
            }
        }

        return false;
    }

    private static boolean wantsToUnsiege(AUnit unit, String log) {
        if (
            hasJustSiegedRecently(unit)
                || unit.lastAttackFrameLessThanAgo(30 * (1 + unit.id() % 5))
        ) {
            return false;
        }

        unit.setTooltipTactical(log);
        unit.addLog(log);
        unit.unsiege();
        return true;
    }

    // =========================================================

    private static boolean allowToUnsiegeToMove(AUnit unit) {
        if (unit.cooldownRemaining() == 0 && (A.now() % (1 + unit.id()) <= 1)) {
            return true;
        }

        return false;
    }
}
