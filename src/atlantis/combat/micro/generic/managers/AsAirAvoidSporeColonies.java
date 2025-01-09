package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AsAirAvoidSporeColonies extends Manager {
    private HasPosition enemyAAPosition;
    private Selection closeSpores;

    public AsAirAvoidSporeColonies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;
        if (unit.shieldWound() <= 3) return false;

        enemyAAPosition = enemyDeadlyAntiAirInRange(unit);
        if (enemyAAPosition == null) return false;

        if (allowSporadicRalliesAgainstOverlords()) {
            return false;
        }

        return true;
    }

    private boolean allowSporadicRalliesAgainstOverlords() {
        if (unit.hp() <= 65) return false;

        Selection friendlyCorsairs;

        return closeSpores.atMost(1)
            && unit.enemiesNear().overlords().atLeast(3)
            && (friendlyCorsairs = unit.friendsNear().ofType(AUnitType.Protoss_Corsair)).atLeast(3)
            && friendlyCorsairs.averageShields() >= 40;
    }

    public Manager handle() {
        if (invokedManager(AsAirRunToCannon.class)) return usedManager(AsAirRunToCannon.class);

        if (unit.moveAwayFrom(enemyAAPosition, 5, Actions.MOVE_FORMATION, "DeadlySpore")) return usedManager(this);

        return null;
    }

    private HasPosition enemyDeadlyAntiAirInRange(AUnit unit) {
        Selection enemies = unit.enemiesNear().ofType(
            AUnitType.Zerg_Spore_Colony
        );

        closeSpores = enemies.canAttack(unit, 1.3 + unit.woundPercent() / 15.0);
        HasPosition enemy = closeSpores.center();

        return enemy;
    }
}
