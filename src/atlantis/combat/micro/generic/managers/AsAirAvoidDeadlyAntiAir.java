package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AsAirAvoidDeadlyAntiAir extends Manager {
    protected HasPosition enemyAAPosition;
    private Selection deadlyEnemies;

    public AsAirAvoidDeadlyAntiAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;
//        if (unit.shieldWound() <= 9) return false;

        enemyAAPosition = enemyDeadlyAntiAirInRange(unit);
        if (enemyAAPosition == null) return false;

        return true;
    }

    public Manager handle() {
        if (allowedToRunToCannon() && invokedManager(AsAirRunToCannon.class)) return usedManager(AsAirRunToCannon.class);

        if (moveAway()) return usedManager(this);

        return null;
    }

    protected boolean moveAway() {
        return unit.moveAwayFrom(enemyAAPosition, 6, Actions.MOVE_FORMATION, "AirDeadlyAA");
    }

    protected boolean moveToAlphaLeader() {
        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return false;
        if (unit.distTo(leader) <= 2) return false;

        return unit.move(leader, Actions.MOVE_FORMATION, "ToAlphaLeader");
    }

    protected boolean allowedToRunToCannon() {
        return true;
    }

    protected HasPosition enemyDeadlyAntiAirInRange(AUnit unit) {
        deadlyEnemies = unit.enemiesNear().ofType(
//            AUnitType.Protoss_Corsair,
//            AUnitType.Protoss_Photon_Cannon,
            AUnitType.Zerg_Scourge,
            AUnitType.Zerg_Devourer
//            AUnitType.Zerg_Spore_Colony,
//            AUnitType.Terran_Valkyrie,
//            AUnitType.Terran_Missile_Turret
        );
        if (deadlyEnemies.empty()) return null;

        return deadlyEnemies.canAttack(unit, safetyMargin(unit)).center();
    }

    protected static double safetyMargin(AUnit unit) {
        return 3.8
            + unit.woundPercent() / 15.0;
    }
}
