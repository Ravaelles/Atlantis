package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class AsAirRunToCannon extends Manager {
    private AUnit cannon;

    public AsAirRunToCannon(AUnit unit) {
        super(unit);
    }

    public static boolean shouldRunToCannonGeneric(AUnit unit) {
        return Count.cannons() > 0
            && unit.shields() <= 10
            && (unit.hp() <= 60 || unit.eval() <= 1.5)
//            && unit.enemiesNear().groundUnits().canAttack(unit, 3).empty()
            && unit.enemiesNear().air().canAttack(unit, 6).notEmpty();
    }

    @Override
    public boolean applies() {
        return unit.isAir()
            && We.protoss()
            && unit.shields() <= 10
            && Count.cannons() > 0
            && unit.eval() <= 1.5
            && unit.enemiesThatCanAttackMe(2 + unit.woundPercent() / 30.0).empty()
            && (cannon = cannonToGoTo()) != null;
    }

    @Override
    public Manager handle() {
        if (unit.distTo(cannon) <= 3) {
            AUnit enemy = unit.enemiesThatCanAttackMe(10).nearestTo(unit);
            if (unit.moveAwayFrom(enemy, 3, Actions.MOVE_SAFETY)) return usedManager(this);
        }

        if (unit.enemiesThatCanAttackMe(5 + unit.woundPercent() / 40.0).empty()) {
            return null;
        }

        if (unit.move(cannon, Actions.MOVE_SAFETY, "AirToCannon")) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit cannonToGoTo() {
        AUnit cannon = Select.ourOfType(AUnitType.Protoss_Photon_Cannon).nearestTo(unit);
        if (cannon == null) return null;

        double cannonDist = cannon.distTo(unit);

        if (cannonDist <= 2 && unit.cooldown() <= 2) {
            return null;
        }

        if (cannonDist >= 0.6) {
            return cannon;
        }

        return null;
    }
}
