package atlantis.combat.missions.defend.focus.terran;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.defend.focus.MissionDefendFocusPoint;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public class TerranMissionDefendFocus {
    public static AFocusPoint define() {
        if (!We.terran()) return null;

        AFocusPoint focus;

        if ((focus = TerranMissionDefendFocus.bunkerAtNatural()) != null) return focus;
        if ((focus = TerranMissionDefendFocus.bunker()) != null) return focus;

        return null;
    }

    protected static AFocusPoint bunker() {
        if (!We.terran()) return null;

//        if (A.seconds() <= 700 && Count.tanks() <= 5) {
        AUnit main = Select.main();
        AUnit bunker = Select.ourWithUnfinishedOfType(AUnitType.Terran_Bunker).closestToEnemyBase();

        if (bunker == null) return MissionDefendFocusPoint.atMainChoke();

        APosition point;
        String tooltip;
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke != null) {
//                point = bunker.translateTilesTowards(1, main);
//                point = bunker.translateTilesTowards(1, mainChoke.center());
            point = bunker.translateTilesTowards(7, mainChoke.center());
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
//        }

//        return null;
    }

    protected static AFocusPoint bunkerAtNatural() {
        if (!We.terran()) return null;

        AUnit bunkerAtNatural = BaseLocations.bunkerAtNatural();
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
