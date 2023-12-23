package atlantis.game.events;

import atlantis.Atlantis;
import atlantis.units.AUnit;
import atlantis.units.special.ums.GoToNeutralNewCompanions;

public class OnUnitRenegade {

    /**
     * Called when a unit changes its AUnitType.
     * <p>
     * For example, when a Drone transforms into a Hatchery, a Siege Tank uses Siege Mode, or a Vespene Geyser
     * receives a Refinery.
     */
    public static void update(AUnit unit) {
        AUnit.forgetUnitEntirely(unit);
        unit.refreshType();

//        if (newUnit.type().isGasBuilding() || newUnit.type().isGeyser() || newUnit.isLarvaOrEgg()) {
//            return;
//        }

        // New unit for us e.g. some UMS maps give units
        if (unit.isOur()) {
            Atlantis.ourNewUnit(unit);

            if (!unit.type().isGasBuildingOrGeyser() && !unit.type().isMineralField()) {

                GoToNeutralNewCompanions.NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = unit;
            }
        }

        // Neutral means Refinery / Extractor
        else if (unit.isNeutral()) {
        }

        // New enemy
        else {
            if (unit.isOverlord()) {
                return;
            }

            Atlantis.enemyNewUnit(unit);

            if (!unit.type().isGasBuildingOrGeyser()) {
                System.out.println("NEW RENEGADE FOR ENEMY " + unit.name());
            }
        }
    }
}
