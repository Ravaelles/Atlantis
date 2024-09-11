package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class TooClustered extends Manager {
    public TooClustered(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;

        int seconds = A.seconds();

        if (seconds >= 300 && seconds % 10 <= 4) return false;
        if (unit.isMissionAttackOrGlobalAttack()) return false;
        if (unit.enemiesNear().inRadius(14, unit).notEmpty()) return false;
        if (unit.friendsNear().buildings().inRadius(3, unit).notEmpty()) return false;
        if (unit.isMissionDefend() && EnemyWhoBreachedBase.notNull()) return false;

        if (seconds <= 300 && unit.isMissionDefend() && unit.friendsNear().inRadius(2, unit).notEmpty())
            return true;

        return squad.size() >= 2 && unit.friendsNear().inRadius(0.3, unit).groundUnits().atLeast(3);
    }

    protected Manager handle() {
        if (unit.lastActionLessThanAgo(15, Actions.MOVE_FORMATION)) {
            return null;
        }

        Selection ourCombatUnits = Select.ourCombatUnits().groundUnits().inRadius(5, unit);
        AUnit nearestBuddy = ourCombatUnits.nearestTo(unit);
        double minDistBetweenUnits = minDistBetweenUnits();

        if (tooClustered(ourCombatUnits, nearestBuddy, minDistBetweenUnits)) {
//            APosition goTo = unit.makeFreeOfAnyGroundUnits(4, 0.2, unit);
//            if (goTo != null) {
//                unit.move(goTo, Actions.MOVE_FORMATION, "SpreadOut", false);
//                return usedManager(this);
//            }

//            double moveDistance = A.chance(15) ? 2 : 0.5;
//            unit.moveAwayFrom(nearestBuddy, moveDistance, Actions.MOVE_FORMATION, "SpreadOut");
            if (uncluster()) return usedManager(this, "SpreadOut");
        }

        return null;
    }

    // =========================================================

    private boolean uncluster() {
        APosition center = unit.friendsNear().inRadius(1.5, unit).center();
        if (center != null) {
            unit.moveAwayFrom(center, 1.5, Actions.MOVE_FORMATION, "SpreadOut");
            return true;
        }

        return unit.moveToMain(Actions.MOVE_FORMATION, "SpreadOut");
    }

    private boolean tooClustered(
        Selection ourCombatUnits,
        AUnit nearestBuddy,
        double minDistBetweenUnits
    ) {
        return nearestBuddy != null
            && ourCombatUnits.size() >= 3
            && nearestBuddy.distToLessThan(unit, minDistBetweenUnits)
            && (unit.friendsInRadius(1.5).size() >= 4 || unit.friendsInRadius(0.8).notEmpty());
    }

    private double minDistBetweenUnits() {
        double baseDist = preferedBaseDistToNextUnit();
        int enemiesNear = unit.enemiesNearInRadius(4);

        if (enemiesNear <= 1 || unit.noCooldown()) {
            int highTemplars = unit.enemiesNear().ofType(AUnitType.Protoss_High_Templar).havingEnergy(75).count();
            if (highTemplars > 0) {
                return baseDist + 0.7 * highTemplars;
            }
        }

        if (enemiesNear <= 2 || unit.noCooldown()) {
            int lurkers = unit.enemiesNear().ofType(AUnitType.Zerg_Lurker).count();
            if (lurkers > 0) {
                return baseDist + 0.1 * lurkers;
            }
        }

        return baseDist;
    }

    private double preferedBaseDistToNextUnit() {
        if (unit.isTank()) {
            return 0.8;
        }

        return 0.4;
    }
}
