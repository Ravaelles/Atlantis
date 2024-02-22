package atlantis.combat.advance.contain.protoss;

import atlantis.architecture.Manager;
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
        if (!We.protoss()) return false;

        if (unit.isDT() && (unit.woundHp() <= 40 || unit.effUndetected())) return false;

        if (OurArmyStrength.relative() >= 500) return false;

        return true;
    }

    @Override
    public Manager handle() {
        AUnit nearestEnemyBuilding = nearestEnemyBuilding();

        if (nearestEnemyBuilding != null) {
            double dist = unit.distTo(nearestEnemyBuilding);

            if (dist > 17) {
                unit.move(nearestEnemyBuilding, Actions.MOVE_FORMATION, "ContainIn");
                return usedManager(this);
            }
            else if (dist <= 14 || unit.distToOr999(Chokes.enemyMainChoke()) < DIST_TO_ENEMY_MAIN_CHOKE) {
                if (A.everyNthGameFrame(13)) {
                    unit.holdPosition("ContainHold");
                }
                else {
                    unit.moveToMain(Actions.MOVE_FORMATION, "ContainOut");
                }
                return usedManager(this);
            }

            unit.holdPosition("ContainHold");
            return usedManager(this);
        }

        return null;
    }

    private AUnit nearestEnemyBuilding() {
        Selection enemyBuildings = EnemyUnits.discovered().buildings();

        if (UnitsArchive.lastTimeOurCombatUnitDiedMoreThanAgo(30 * 5)) {
            enemyBuildings = enemyBuildings.combatBuildingsAntiLand();
        }

        return enemyBuildings.nearestTo(unit);
    }
}
