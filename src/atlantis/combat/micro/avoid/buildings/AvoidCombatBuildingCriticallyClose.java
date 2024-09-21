package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AvoidCombatBuildingCriticallyClose extends Manager {
    private AUnit combatBuilding;
    private Selection ourCombatUnitsNearby;

    public AvoidCombatBuildingCriticallyClose(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {

        return (combatBuilding = combatBuilding()) != null
            && doNotAvoidThirdBaseCombatBuildings()
            && !strongEnoughToAttack()
            && ifLurkersNearbyDontAvoidTheBuilding()
            && combatBuildingShouldNotBeEngaged();
    }

    private boolean doNotAvoidThirdBaseCombatBuildings() {
        if (unit.combatEvalRelative() <= 1.2) return false;

        APosition enemyThird = BaseLocations.enemyThird();
        if (enemyThird == null) return false;

        return enemyThird.distTo(combatBuilding) <= 8;
    }

    private boolean ifLurkersNearbyDontAvoidTheBuilding() {
        return unit.enemiesNear().lurkers().inRadius(8, unit).notEmpty();
    }

    private boolean strongEnoughToAttack() {
//        int ourStrength = OurArmy.strength();

//        if (ourStrength >= 500 || (ourStrength >= 400 && A.hasMinerals(800))) return true;
//        if (unit.combatEvalRelative() >= (2.7 - (A.supplyUsed() > 185 ? 1 : 0))) return true;
        if (unit.combatEvalRelative() <= (2.7 - (A.supplyUsed() > 185 ? 1 : 0))) return false;
//        if (unit.friendsNear().combatUnits().count() * 6.9 >= unit.enemiesNear().havingWeapon().count()) return true;
//        if (unit.friendsNear().combatUnits().count() * 6.9 >= unit.enemiesNear().havingWeapon().count()) return true;

        if (unit.friendsNear().combatUnits().count() * 6 >= unit.enemiesNear().combatBuildingsAntiLand().count()) {
            if (unit.friendsNear().totalHp() >= unit.enemiesNear().havingAntiGroundWeapon().totalHp()) {
                return unit.combatEvalRelative() >= 1.3;
            }
        }

        return false;
    }

    @Override
    protected Manager handle() {
        if (shouldHoldGround(combatBuilding)) {
            unit.holdPosition("HoldHere");
            return usedManager(this);
        }

        if (!unit.isMoving() || A.fr % 15 == 0) {
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_AVOID, "AvoidCB");
        }
        return usedManager(this);
    }

    private boolean shouldHoldGround(AUnit combatBuilding) {
        if (unit.lastUnderAttackLessThanAgo(30 * 2)) return false;
        if (unit.combatEvalRelative() >= 2.5) return false;

        double dist = unit.targetPosition().distTo(combatBuilding);
        if (dist <= 8.5) return false;

        if (unit.enemiesNear().canAttack(unit, 1).notEmpty()) return false;

        double minDistAllowed = (unit.isRanged() ? 8.4 : 10.2) + (unit.woundPercent() / 25.0);

        if (
            dist <= minDistAllowed && (
                !unit.isAttacking() || !unit.isTargetInWeaponRangeAccordingToGame()
            )
        ) return true;

        return unit.isMoving()
            && unit.targetPosition() != null
            && dist < minDistAllowed;
    }

    // =========================================================


    private AUnit combatBuilding() {
        return unit.enemiesNear()
            .buildings()
            .onlyCompleted()
            .combatBuildingsAnti(unit)
            .inRadius(9.5, unit)
            .nearestTo(unit);
    }

    private boolean combatBuildingShouldNotBeEngaged() {
        ourCombatUnitsNearby = combatBuilding.enemiesNear().combatUnits();

        if (dontEngageBecauseTooManyEnemyCombatUnitsNearby()) return true;

        AUnit ourUnit = ourCombatUnitsNearby.nearestTo(combatBuilding);
//        System.err.println("ourUnit.combatEvalRelative() = " + ourUnit.combatEvalRelative());
//        System.err.println("ourUnit.combatEvalAbs() = " + ourUnit.combatEvalAbsolute());
        return ourUnit != null
//            && ourCombatUnitsNearby.count() <= 4
            && (
            ourUnit.combatEvalRelative() <= 2.5
                && ourUnit.combatEvalAbsolute() <= -200
        );
    }

    private boolean dontEngageBecauseTooManyEnemyCombatUnitsNearby() {
        if (A.supplyUsed() >= 170 || A.hasMinerals(3000)) return false;

        if (unit.friendsNear().combatUnits().atLeast(25)) return false;

        return combatBuilding.enemiesNear()
            .combatUnits()
            .inRadius(6, combatBuilding).atMost((int) (ourCombatUnitsNearby.count() / 8));
    }
}
