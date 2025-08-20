package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.buildings.CircumnavigateCombatBuilding;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AsAirAvoidTowers extends Manager {
    private HasPosition enemyAAPosition;
    private Selection closeBuildings;

    public AsAirAvoidTowers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;
//        if (unit.shieldWound() <= 3) return false;

        enemyAAPosition = building(unit);
        if (enemyAAPosition == null) return false;

        if (allowSporadicRalliesAgainstOverlords()) {
            return false;
        }

        return true;
    }

    private boolean allowSporadicRalliesAgainstOverlords() {
        if (unit.hp() <= 140) return false;
        if (EnemyUnits.discovered().mutalisks().atLeast(1)) return false;

        Selection friendlyCorsairs;

        return closeBuildings.atMost(1)
            && (friendlyCorsairs = unit.friendsNear().ofType(AUnitType.Protoss_Corsair)).atLeast(3)
            && unit.enemiesNear().overlords().atLeast(friendlyCorsairs.size() >= 3 ? 3 : 5)
            && friendlyCorsairs.averageShields() >= 30;
    }

    public Manager handle() {
        CircumnavigateCombatBuilding circumnavigate = new CircumnavigateCombatBuilding(unit, closeBuildings.first());
        if (circumnavigate.forceHandled()) {
            return usedManager(circumnavigate);
        }

        enemyAAPosition = enemyAAPosition.randomizeByTiles(4, A.s % 10 + unit.id());

        if (unit.moveAwayFrom(enemyAAPosition, 5, Actions.MOVE_FORMATION, "DeadlyTower")) return usedManager(this);

        return null;
    }

    private HasPosition building(AUnit unit) {
        Selection enemies = unit.enemiesNear().ofType(
            AUnitType.Zerg_Spore_Colony, AUnitType.Protoss_Photon_Cannon, AUnitType.Terran_Missile_Turret
        );

        closeBuildings = enemies.canAttack(unit, base() + unit.woundPercent() / 40.0);
        HasPosition enemy = closeBuildings.center();

        return enemy;
    }

    private double base() {
        double noShotBonus = unit.shotSecondsAgo(10) ? 0 : -0.15;

        if (unit.shieldHealthy()) {
            return 1.4 + noShotBonus;
        }

        return 1.6 + noShotBonus;
    }
}
