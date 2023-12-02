package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class AvoidCombatBuilding extends Manager {
//    private ShouldRetreat shouldRetreat;

    private Selection combatBuildings;
    private AUnit combatBuilding;

    public AvoidCombatBuilding(AUnit unit) {
        super(unit);
//        shouldRetreat = new ShouldRetreat(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAir()) return true;
        if (unit.isGroundUnit() && unit.isMissionDefendOrSparta()) return false;

        AUnit combatBuilding = unit.enemiesNear()
            .combatBuildings(false)
            .canAttack(unit, A.supplyUsed() <= 40 ? 8 : 5)
            .nearestTo(unit);

        if (combatBuilding == null) return false;

        if (
            A.supplyUsed() <= 100 && unit.isInfantry() && Count.tanks() <= 3 && unit.combatEvalRelative() <= 3
        ) return true;

        if (!unit.isTank()) {
            if (We.terran() && unit.friendsNear().groundUnits().havingAntiGroundWeapon().atLeast(9)) return false;

            if (
                unit.isMissionAttack()
                    && unit.woundPercent() >= 20
                    && unit.friendsNear().tanks().inRadius(15, unit).atLeast(2)
                    && unit.friendsNear().groundUnits().inRadius(1, unit).atMost(2)
                    && unit.friendsNear().groundUnits().inRadius(3, unit).atMost(8)
            ) return true;
        }

        if (
            unit.isGroundUnit() && unit.combatEvalRelative() >= 2.5 && unit.hp() >= 30 && unit.woundPercent() <= 40
        ) return false;

        combatBuildings = EnemyUnits.discovered().combatBuildings(false);
        combatBuilding = combatBuildings.inRadius(12, unit).canAttack(unit, 4.5).nearestTo(unit);

        return combatBuilding != null;
    }

    protected Manager handle() {
        if (combatBuilding == null) return null;

        if (unit.isTankUnsieged() && handleForTank(combatBuilding) != null) return usedManager(this);

//        APainter.paintCircleFilled(8, Color.Red);
//        System.err.println("@ C = " + ShouldRetreat.shouldRetreat(unit));
        if (
            !unit.isAir()
                && unit.friendsInRadiusCount(3) >= 6
                && unit.friendsInRadiusCount(5) >= 8
                && unit.combatEvalRelative() <= 4.5
//                && !shouldRetreat.shouldRetreat(unit)
                && combatBuildings.combatBuildings(false).inRadius(10, unit).notEmpty()
        ) {
//            unit.setTooltip("@ D YOLO " + unit);
            return null;
        }

//        double criticalDist = 9.8 + (unit.isAir() ? 2.5 : 0);
        double criticalDist = criticalDist();
        double distTo = combatBuilding.distTo(unit);
        double doNothingMargin = 1.5;

        if (distTo <= criticalDist + doNothingMargin) {
//            unit.holdPosition("HoldVsBuilding");
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_AVOID, "AvoidCB");
            return usedManager(this);
        }

//        if (distTo <= criticalDist) {
////            if (thereIsNoSafetyMarginAtAll(combatBuilding)) return usedManager(avoidCombatBuildingCriticallyClose);
//            return barelyAnySafetyLeft(combatBuilding);
//        }
//        else if (holdPositionToShoot(combatBuilding) != null) {
//            return usedManager(this);
//        }
//        else if (distTo <= (criticalDist + doNothingMargin)) {
//            return stillSomePlaceLeft(combatBuilding);
//        }
//        else if (distTo < (criticalDist + doNothingMargin)) {
//            Manager manager = thereIsNoSafetyMarginAtAll(combatBuilding);
//            if (manager != null) return usedManager(manager);
//        }

        return null;
    }

    private Manager handleForTank(AUnit combatBuilding) {
        if (SiegeMode.isResearched()) return null;

        if (unit.isTankUnsieged() && combatBuilding.distTo(unit) < 11) {
            unit.setTooltip("SiegeVsBuilding");
            unit.siege();
            return usedManager(this);
        }

        return null;
    }

    private Manager holdPositionToShoot(AUnit combatBuilding) {
        Selection enemiesToAttack = unit.enemiesNear().canBeAttackedBy(unit, 0);
        if (enemiesToAttack.notEmpty()) {
            if (
                unit.isMoving()
                    && !unit.isRunning()
                    && unit.lastStartedRunningLessThanAgo(30 * 7)
            ) {
                unit.holdPosition("Hold&Shoot");
            }
            return usedManager(this);
        }

//        if (A.seconds() % 8 <= 4) {
//            unit.holdPosition("HoldToShoot");
//            return usedManager(this);
//        }

        return null;
    }

    private Manager thereIsNoSafetyMarginAtAll(AUnit combatBuilding) {
        AvoidCombatBuildingCriticallyClose avoidCombatBuildingCriticallyClose = new AvoidCombatBuildingCriticallyClose(unit, combatBuilding);
        if (avoidCombatBuildingCriticallyClose.invoke() != null) {
            return avoidCombatBuildingCriticallyClose;
        }

        return null;
    }

    private Manager barelyAnySafetyLeft(AUnit combatBuilding) {
        if (unit.isAir()) {
            APosition runFrom = combatBuilding.position();

            if (unit.hp() >= 60 || A.chance(10)) {
                runFrom = runFrom.position().randomizePosition(3);
            }

            unit.runningManager().runFrom(runFrom, 6, Actions.MOVE_AVOID, false);
            return usedManager(this);
        }
        else {
//            unit.runningManager().runFrom(combatBuilding, 0.1, Actions.MOVE_AVOID, false);
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_AVOID, "Ah+" + combatBuilding.name());
            return usedManager(this);
        }
    }

    private Manager stillSomePlaceLeft(AUnit combatBuilding) {
        return (new CircumnavigateCombatBuilding(unit, combatBuilding)).invoke();
//        APosition runFrom = combatBuilding.position();
//
////        if (A.chance(70)) {
//        runFrom = runFrom.position().randomizePosition(5 + unit.id() % 4);
////        }
//
//        unit.runningManager().runFrom(runFrom, 6, Actions.MOVE_AVOID, false);
//        return usedManager(this);
    }

    private double criticalDist() {
        return 9.1
            + (combatBuilding.isSunken() ? 1.6 : 0)
            + (unit.isAir() ? 0.6 : 0) + (unit.isMoving() ? 1.25 : 0)
            + unit.woundPercent() / 50;
    }

}
