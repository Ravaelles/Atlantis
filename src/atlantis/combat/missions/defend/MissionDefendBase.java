package atlantis.combat.missions.defend;

//public class MissionDefendBase extends Mission {
//
//    protected MissionDefendBase(String name) {
//        super("DefendBase");
//    }
//
//    @Override
//    public boolean update(AUnit unit) {
//        AUnit enemy = null;
//
//        // === Attack units nears main =============================
//
//        AUnit ourCenterUnit = Select.main();
//        if (ourCenterUnit == null) {
//            ourCenterUnit = unit;
//        }
//
//        if (ourCenterUnit != null) {
//            AUnit nearestEnemy = Select.enemy().effVisible().nearestTo(ourCenterUnit);
//            if (nearestEnemy != null) {
//                enemy = nearestEnemy;
//            }
//        }
//
//        // Attack enemy
//        if (enemy != null) {
//            unit.setTooltip("#DefendBase");
//            unit.attackUnit(enemy);
//            return true;
//        }
//
//        // =========================================================
//
//        return false;
//    }
//
//}
