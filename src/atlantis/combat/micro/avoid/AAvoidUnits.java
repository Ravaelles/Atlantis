package atlantis.combat.micro.avoid;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;

public abstract class AAvoidUnits {

    public static boolean avoid(AUnit unit) {
        if (AAvoidInvisibleEnemyUnits.avoid(unit)) {
            return true;
        }

        if ((new AAvoidEnemyMeleeUnits(unit)).avoid()) {
            return true;
        }

        if (!Missions.isGlobalMissionAttack()) {
            if (AAvoidEnemyDefensiveBuildings.avoid(unit, false)) {
                return true;
            }
        }

        return false;
    }

}
