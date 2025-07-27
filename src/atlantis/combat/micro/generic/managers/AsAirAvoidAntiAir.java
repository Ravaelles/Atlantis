package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.architecture.helper.InstantiateManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.APosition;
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

        if (unit.woundHp() <= 31 && unit.eval() >= 1.5) return false;

        enemyAAPosition = enemyAntiAirInRange(unit);
        if (enemyAAPosition == null) return false;

        return true;
    }

    public Manager handle() {
        if (invokedManager(AsAirRunToCannon.class)) return usedManager(AsAirRunToCannon.class);

        if (goToAlphaLeader()) return usedManager(this, "AARunToAlphaLeader");

        if (unit.moveAwayFrom(enemyAAPosition, 5, Actions.MOVE_FORMATION, "AirAvoidAA")) return usedManager(this);

        return null;
    }

    private boolean goToAlphaLeader() {
        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return false;

        if (Alpha.get().units().havingAntiAirWeapon().atMost(2)) return false;

        return unit.move(leader, Actions.MOVE_SAFETY, "AARunToAlphaLeader");
    }

    private HasPosition enemyAntiAirInRange(AUnit unit) {
        Selection enemies = unit.enemiesNear().havingAntiAirWeapon().canAttack(unit, 1.5 + unit.woundPercent() / 13.0);

        APosition center = enemies.center();
        if (center != null) return center;

        return enemies.first();
    }
}
