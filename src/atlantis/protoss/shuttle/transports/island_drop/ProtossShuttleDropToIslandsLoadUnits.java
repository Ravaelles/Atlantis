package atlantis.protoss.shuttle.transports.island_drop;

import atlantis.architecture.Manager;
import atlantis.combat.missions.drops.ProtossShouldDropToIsland;
import atlantis.information.enemy.EnemyOnCloseIsland;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ProtossShuttleDropToIslandsLoadUnits extends Manager {
    public ProtossShuttleDropToIslandsLoadUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isShuttle()
            && ProtossShouldDropToIsland.check()
            && unit.spaceRemaining() >= 2;
    }

    @Override
    protected Manager handle() {
        if (unit.isMoving() && unit.isAction(Actions.LOAD)) {
            unit.addLog("DILoading");
            return usedManager(this, "DILoadingIP");
        }

        AUnit unitToLoad = defineOurCombatUnitToDropOnIsland();
//        if (unitToLoad == null) System.err.println("unitToLoad = " + unitToLoad);
        if (unitToLoad == null) return null;
//        if (!unit.hasFreeSpaceFor(unitToLoad)) System.err.println("no space for " + unitToLoad);
        if (!unit.hasFreeSpaceFor(unitToLoad)) return null;

        if (unitToLoadAndEnemyOnIslandInTheSameRegion(unitToLoad)) {
            return null;
        }

//        if (unitToLoad.lastAttackOrderMoreThanAgo(30 * 4)) {
//        }
        if (
            unit.lastActionMoreThanAgo(60, Actions.LOAD)
                && unit.lastActionMoreThanAgo(6 * 30, Actions.UNLOAD)
        ) {
            unit.load(unitToLoad);
            unitToLoad.load(unit);
            return usedManager(this, "DropIslandLoad: " + unitToLoad);
        }

//        System.out.println("Not loading unit: " + unitToLoad);
//        System.out.println(unitToLoad.lastAttackOrderAgo());
//        System.out.println(unitToLoad.lastActionAgo(Actions.LOAD) + " / " + (unitToLoad.lastActionAgo(Actions.LOAD) >= 30 * 5));
//        System.out.println(unitToLoad.lastActionAgo(Actions.UNLOAD) + " / " + (unitToLoad.lastActionAgo(Actions.UNLOAD) >= 30 * 5));

        return null;
    }

    private boolean unitToLoadAndEnemyOnIslandInTheSameRegion(AUnit unitToLoad) {
        HasPosition positionOfEnemy = EnemyOnCloseIsland.get();
        if (positionOfEnemy == null) return false;

        if (positionOfEnemy.isPositionVisible() && unit.enemiesNear().countInRadius(15, unit) == 0) return true;

        return unitToLoad.regionsMatch(positionOfEnemy);
    }

    private AUnit defineOurCombatUnitToDropOnIsland() {
        HasPosition enemy = EnemyOnCloseIsland.get();

        return Select.ourCombatUnits()
            .havingAntiGroundWeapon()
            .notLoaded()
            .lastActionMoreThanAgo(30 * 8, Actions.UNLOAD)
            .lastActionMoreThanAgo(50, Actions.LOAD)
            .nearestTo(enemy);
    }
}
