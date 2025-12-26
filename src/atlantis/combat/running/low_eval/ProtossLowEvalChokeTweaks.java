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
        if (choke == null || choke.center() == null) return 0
;
        double chokeDist = unit.distTo(choke.center());
        if (chokeDist >= 7) return 0;

//        double penaltyModifier = Army.strengthWithoutOurCB() <= 300 ? 1.6 : 1;
        double penaltyModifier = 1;
        int ourCombatUnits = Count.ourCombatUnits();

        if (chokeDist <= 5) {
            if (ourCombatUnits <= 12 && choke.isMainChoke() && Count.workers() <= 2) {
                return ourCombatUnits <= 8 ? -0.9 : -0.5;
            }

            if (ourCombatUnits <= 15 && choke.isNaturalChoke()) {
                return ourCombatUnits <= 12 ? -0.4 : -0.25;
            }
        }

        return (choke.width() <= 3.5 ? -1.8 : -1.2) * penaltyModifier;
    }
}