package atlantis.combat.advance.contain.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.UnitsArchive;
import atlantis.information.generic.OurArmyStrength;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ContainAsProtoss extends Manager {
    public static final int DIST_TO_ENEMY_MAIN_CHOKE = 9;

    public ContainAsProtoss(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return (new AppliesContainForProtoss(this)).applies();
    }

    @Override
    public Manager handle() {
        if (whenEnemyBuildingNear()) return usedManager(this);

        return null;
    }

    private boolean whenEnemyBuildingNear() {
        AUnit nearestEnemyBuilding = nearestEnemyBuilding();

        if (nearestEnemyBuilding != null) {
            double dist = unit.distTo(nearestEnemyBuilding);

            if (shouldIgnore(dist)) {
//                unit.move(nearestEnemyBuilding, Actions.MOVE_FORMATION, "ContainIn");
                return false;
            }
//            else if (dist <= 14 || unit.distToOr999(Chokes.enemyMainChoke()) < DIST_TO_ENEMY_MAIN_CHOKE) {
            else if (dist <= 14) {
                if (A.everyNthGameFrame(17)) {
                    unit.holdPosition("ContainHold");
                }
                else {
                    unit.moveToMain(Actions.MOVE_FORMATION, "ContainOut");
                }
                return true;
            }

            unit.holdPosition("ContainHold");
            return true;
        }

        return false;
    }

    private boolean shouldIgnore(double dist) {
        if (unit.enemiesNear().combatBuildingsAntiLand().count() >= 7 * unit.friendsNearCount()) return false;

        return dist > 17 || unit.enemiesNear().combatUnits().atMost(4);
    }

    private AUnit nearestEnemyBuilding() {
        Selection enemyBuildings = EnemyUnits.discovered().buildings();

        if (UnitsArchive.lastTimeOurCombatUnitDiedMoreThanAgo(30 * 5)) {
            enemyBuildings = enemyBuildings.combatBuildingsAntiLand();
        }

        return enemyBuildings.nearestTo(unit);
    }
}
