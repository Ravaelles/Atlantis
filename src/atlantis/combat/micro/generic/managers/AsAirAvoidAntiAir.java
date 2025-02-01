package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.architecture.helper.InstantiateManager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AsAirAvoidAntiAir extends Manager {
    private HasPosition enemyAAPosition;

    public AsAirAvoidAntiAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;
        if (unit.shieldHealthy()) return false;

        enemyAAPosition = enemyAntiAirInRange(unit);
        if (enemyAAPosition == null) return false;

        return true;
    }

    public Manager handle() {
        if (invokedManager(AsAirRunToCannon.class)) return usedManager(AsAirRunToCannon.class);

        if (unit.moveAwayFrom(enemyAAPosition, 5, Actions.MOVE_FORMATION, "AirAvoidAA")) return usedManager(this);

        return null;
    }

    private HasPosition enemyAntiAirInRange(AUnit unit) {
        Selection enemies = unit.enemiesNear().havingAntiAirWeapon();
        HasPosition enemy = enemies.canAttack(unit, 1.3 + unit.woundPercent() / 15.0).center();

        return enemy;
    }
}
