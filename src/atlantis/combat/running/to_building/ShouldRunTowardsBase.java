package atlantis.combat.running.to_building;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ShouldRunTowardsBase {
    public static boolean check(AUnit unit, HasPosition runAwayFrom) {
        if (A.seconds() >= 500) return false;
        if (unit.isFlying()) return false;
        if (unit.hp() <= 20) return false;
        if (unit.isDragoon() || unit.isTank()) return false;

        AUnit main = Select.main();
        if (main == null) return false;

        if (unit.distTo(runAwayFrom) < 2.1) return false;
        if (unit.meleeEnemiesNearCount(1.8) > 0) return false;

        if (unit.isAir() && main.distTo(unit) > 12) return true;

        if (OurStrategy.get().isRushOrCheese() && A.seconds() <= 300) return false;
        if (!unit.hasPathTo(main)) return false;

        double distToMain = unit.distTo(main);

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(4);
        if (distToMain >= 40 || (distToMain > 15 && meleeEnemiesNearCount == 0 && unit.isMissionDefend()))
            return true;

        if (A.seconds() >= 380) return false;

        if (unit.isScout()) return false;

        // If already close to the base, don't run towards it, no point
        if (distToMain < 50) return false;

        if (meleeEnemiesNearCount >= 1) return false;

        // Only run towards our main if our army isn't too numerous, otherwise units gonna bump upon each other
        if (Count.ourCombatUnits() > 10) return false;

        if (unit.lastStartedRunningLessThanAgo(30) && unit.lastStoppedRunningLessThanAgo(30))
            return false;

        if (Count.ourCombatUnits() <= 10 || unit.isNearEnemyBuilding()) {
            if (unit.meleeEnemiesNearCount(3) == 0) {
                return true;
            }
        }

        return false;

//        AUnit mainOrAnyBuilding = Select.mainOrAnyBuilding();
//        if (mainOrAnyBuilding == null) return false;
//
//        return unit.distTo(mainOrAnyBuilding) >= 20
//            && unit.meleeEnemiesNearCount(1.7) == 0;
    }

    public static AUnit position() {
        return Select.mainOrAnyBuilding();
    }
}