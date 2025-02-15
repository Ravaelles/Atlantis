package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AsAirAvoidDeadlyAntiAir extends Manager {
    private HasPosition enemyAAPosition;

    public AsAirAvoidDeadlyAntiAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;
        if (unit.shieldWound() <= 9) return false;

        enemyAAPosition = enemyDeadlyAntiAirInRange(unit);
        if (enemyAAPosition == null) return false;

        return true;
    }

    public Manager handle() {
        if (invokedManager(AsAirRunToCannon.class)) return usedManager(AsAirRunToCannon.class);

        if (unit.moveAwayFrom(enemyAAPosition, 5, Actions.MOVE_FORMATION, "AirDeadlyAA")) return usedManager(this);

        return null;
    }

    private HasPosition enemyDeadlyAntiAirInRange(AUnit unit) {
        Selection enemies = unit.enemiesNear().ofType(
            AUnitType.Protoss_Corsair,
            AUnitType.Protoss_Photon_Cannon,
            AUnitType.Zerg_Scourge,
            AUnitType.Zerg_Devourer,
            AUnitType.Zerg_Spore_Colony,
            AUnitType.Terran_Valkyrie,
            AUnitType.Terran_Missile_Turret
        );

        HasPosition enemy = enemies.canAttack(unit, 1.3 + unit.woundPercent() / 15.0).center();

        return enemy;
    }
}
