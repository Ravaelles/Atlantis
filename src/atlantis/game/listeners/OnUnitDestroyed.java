package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.UnitsArchive;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OnUnitDestroyed {
    public static void onUnitDestroyed(AUnit unit) {
        // Some ums maps have funky stuff happening at the start, exclude first 20 frames
        if (A.now() <= 20) return;

        if (unit.isOur() && unit.isBase()) OurClosestBaseToEnemy.clearCache();

        // Our unit
        if (unit.isOur() && unit.isRealUnit()) {
            OnOurUnitDestroyed.update(unit);
        }
//        else if (unit.isEnemy() && unit.isRealUnit()) {
        else if (unit.isEnemy()) {
            onEnemyUnitDestroyed(unit);
        }

        // Needs to be at the end, otherwise unit is reported as dead too early
        UnitsArchive.markUnitAsDestroyed(unit);

        // =========================================================

        if (A.now() >= 50 && A.isUms() && A.supplyUsed() == 0 && Select.ourCombatUnits().isEmpty()) {
            System.out.println("### ROUND END at " + A.seconds() + "s ###");
            UnitsArchive.paintLostUnits();
            UnitsArchive.paintKilledUnits();
        }
    }

    private static void onEnemyUnitDestroyed(AUnit unit) {
        EnemyInfo.removeDiscoveredUnit(unit);
        if (!unit.type().isGeyser()) {
            Atlantis.KILLED++;
            Atlantis.KILLED_RESOURCES += unit.type().getTotalResources();

            if (unit.type().isABuilding()) Atlantis.KILLED_BUILDINGS++;
        }
    }
}
