package atlantis.terran;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.debug.painter.APainter;
import atlantis.map.position.APosition;
import atlantis.map.position.GoTo;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class FlyingBuildingScoutManager extends Manager {

    private AUnit combatBuilding;

    public FlyingBuildingScoutManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isABuilding() && unit.isLifted();
    }

    protected Manager handle() {
        printUnderAttackAgo();

        if (updateFlyingBuilding()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean updateFlyingBuilding() {
        APainter.paintCircle(unit, new int[]{7, 10, 13, 16}, Color.Grey);

        if (avoidCombatBuildings()) return unit.moveAwayFrom(combatBuilding, 3, Actions.MOVE_SAFETY, "BloodyBuilding");

        if (underAttack()) return actWhenUnderFire();

        if (woundedAndAntiAirUnitsNear()) return true;

        if (goToFocusPoint()) return true;

        return false;
    }

    private boolean actWhenUnderFire() {
        HasPosition goTo = GoTo.orMain(Alpha.get().center());

        unit.move(goTo, Actions.MOVE_SAFETY, "UnderFire");
        return true;
    }

    private boolean woundedAndAntiAirUnitsNear() {
        if (!unit.woundPercentMin(3)) return false;

        Selection antiAirEnemies = unit.enemiesNear().havingAntiAirWeapon().canAttack(unit, 0.2);
        if (antiAirEnemies.notEmpty()) {
//            unit.moveAwayFrom(
//                antiAirEnemies.nearestTo(unit), 2, Actions.MOVE_SAFETY, "OopsAntiAir"
//            );
            unit.move(GoTo.orMain(Alpha.get().center()), Actions.MOVE_SAFETY, "OopsAntiAir");
            return true;
        }

        return false;
    }

    private boolean goToFocusPoint() {
        APosition focusPoint = flyingBuildingFocusPoint();

        // Move towards focus point if needed
        if (focusPoint != null) {
            double distToFocusPoint = focusPoint.distTo(unit);

            if (distToFocusPoint > 0.5) {
                unit.moveStrategic(focusPoint, Actions.SPECIAL, "Fly baby!");
                return true;
            }
        }
        return false;
    }

    private boolean underAttack() {
        if (unit.lastUnderAttackLessThanAgo(30 * 4)) {
//            AUnit enemy = unit.enemiesNear().canAttack(unit, 3).nearestTo(unit);
//            if (enemy != null) {
            HasPosition moveTo = null;

            AUnit friend = Select.ourCombatUnits().nearestTo(unit);
            if (friend == null) return false;

            if (friend != null && friend.distTo(unit) > 3) {
                moveTo = friend;
            }

            if (moveTo == null) {
                moveTo = Select.main();
            }

            if (moveTo != null) {
                unit.move(friend, Actions.MOVE_SAFETY, "UnderFire");
                return true;
            }
//            }
            System.err.println("Weird, no friend to move to?");
        }
        return false;
    }

    private boolean avoidCombatBuildings() {
        Selection combatBuildings = unit.enemiesNear()
            .combatBuildingsAntiAir()
            .inRadius(7.8, unit);

        combatBuilding = combatBuildings.first();

        return combatBuilding != null;
    }

//    private APosition flyingBuildingFocusPoint() {
//        APosition containFocusPoint = Missions.globalMission().focusPoint();
//        APosition attackFocusPoint = Missions.ATTACK.focusPoint();
//
//        if (containFocusPoint != null && attackFocusPoint != null) {
//            return containFocusPoint.translateTilesTowards(attackFocusPoint, 4);
//        }
//
//        return containFocusPoint;
////        return Select.ourTanks().first().position();
//    }

    private APosition flyingBuildingFocusPoint() {
        APosition focusPoint = Alpha.get().median();
        if (focusPoint != null) {
            HasPosition attackFocusPoint = Missions.ATTACK.focusPoint();

            if (attackFocusPoint != null) {
                focusPoint = focusPoint.translateTilesTowards(16, attackFocusPoint);
            }

            return focusPoint;
        }

        return null;
    }

    private void printUnderAttackAgo() {
        int underAttackAgo = unit.lastUnderAttackAgo();
        if (underAttackAgo <= 30 * 30) {
            AAdvancedPainter.paintTextCentered(unit, "AttackedAgo: " + underAttackAgo, Color.Grey);
        }
    }
}

