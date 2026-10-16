package atlantis.combat.targeting.air;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AAirUnitAirTargets extends HasUnit {
    public AAirUnitAirTargets(AUnit unit) {
        super(unit);
    }

    public AUnit targetsAir(Selection possibleTargets) {
        if (!unit.isAir()) return null;

        Selection targets = possibleTargets.air().ofType(
            AUnitType.Protoss_Arbiter,
            AUnitType.Protoss_Observer,
            AUnitType.Protoss_Shuttle,
            AUnitType.Terran_Wraith,
            AUnitType.Zerg_Mutalisk,
            AUnitType.Zerg_Queen
        );

        AUnit inRange = targets.inShootRangeOf(unit).mostWounded();

        if (inRange != null) return inRange;

        return targets.nearestTo(unit);
    }
}
