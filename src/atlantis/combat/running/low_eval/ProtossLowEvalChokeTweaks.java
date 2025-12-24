package atlantis.combat.running.low_eval;

import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Count;

public class ProtossLowEvalChokeTweaks extends HasUnit {
    public ProtossLowEvalChokeTweaks(AUnit unit) {
        super(unit);
    }

    protected double evalChokePenalty() {
        AChoke choke = unit.nearestChoke();
        double chokeDist = unit.distTo(choke.center());

        if (chokeDist >= 7) return 0;

//        double penaltyModifier = Army.strengthWithoutOurCB() <= 300 ? 1.6 : 1;
        double penaltyModifier = 1;

        if (Count.ourCombatUnits() <= 17 && choke.isMainChoke()) {
            return -2;
        }

        if (Count.ourCombatUnits() <= 22 && choke.isNaturalChoke()) {
            return -1.6;
        }

        return (choke.width() <= 3.5 ? -1.8 : -1.2) * penaltyModifier;
    }
}