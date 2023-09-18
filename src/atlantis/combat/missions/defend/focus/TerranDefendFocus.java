package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TerranDefendFocus {
    public static AFocusPoint define() {
        if (!We.terran()) return null;

        AFocusPoint focus;

        if ((focus = TerranDefendFocus.bunkerAtNatural()) != null) return focus;
        if ((focus = TerranDefendFocus.bunker()) != null) return focus;

        return null;
    }

    protected static AFocusPoint bunker() {
        if (!We.terran()) return null;

        AFocusPoint focus;
        if (A.seconds() <= 700 && Count.tanks() <= 5) {
            AUnit main = Select.main();
            AUnit bunker = Select.ourWithUnfinishedOfType(AUnitType.Terran_Bunker).mostDistantTo(main);
            if (bunker != null) {
                APosition point;
                String tooltip;
                AChoke mainChoke = Chokes.mainChoke();
                if (mainChoke != null) {
//                    point = mainChoke.center().translateTilesTowards(3, bunker);
                    point = bunker.translateTilesTowards(3, mainChoke.center());
                    tooltip = "Bunker & Choke";
                }
                else {
                    point = bunker.translateTilesTowards(-3, main);
                    tooltip = "Bunker";
                }

                return new AFocusPoint(
                    point,
                    main,
                    tooltip
                );
            }
            else {
                focus = MissionDefendFocus.atMainChoke();
                if (focus != null) {
                    return focus;
                }
            }
        }
        return null;
    }

    protected static AFocusPoint bunkerAtNatural() {
        if (!We.terran()) return null;

        AUnit bunkerAtNatural = Bases.bunkerAtNatural();
        if (bunkerAtNatural != null) {
            return new AFocusPoint(
                bunkerAtNatural,
                Select.main(),
                "Bunker@Natural"
            );
        }
        return null;
    }
}
