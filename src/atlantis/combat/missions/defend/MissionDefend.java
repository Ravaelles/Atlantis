package atlantis.combat.missions.defend;

import atlantis.AGame;
import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class MissionDefend extends Mission {

    public MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        // =========================================================

        AFocusPoint focusPoint = focusPoint();
        if (focusPoint == null) {
            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        return MoveToDefendFocusPoint.move(unit, focusPoint);
    }

    // =========================================================


    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (unit.hasWeaponRangeToAttack(enemy, 1.6)) {
            return true;
        }

        if (Have.main()) {
            if (Select.enemy().inRadius(18, Select.main()).atLeast(1)) {
                return true;
            }

            if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
                return true;
            }
        }

        if (focusPoint() != null) {
            double unitDistToFocusPoint = unit.distTo(focusPoint());
            if (unitDistToFocusPoint >= 8 || unitDistToFocusPoint > unit.distTo(enemy)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        return false;
//        return enemies.onlyMelee() && unit.hp() >= 18;
    }
}
