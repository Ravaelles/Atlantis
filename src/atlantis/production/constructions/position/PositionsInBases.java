package atlantis.production.constructions.position;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;

public class PositionsInBases {
    public static HasPosition mainHalfwayToMainChoke() {
        HasPosition main = Select.mainOrAnyBuilding();
        if (main == null) return null;

        AChoke choke = Chokes.mainChoke();
        if (choke == null) return main;

        return main.translatePercentTowards(50, choke);
    }

    public static HasPosition atTheBackOfMain() {
        HasPosition main = Select.mainOrAnyBuilding();
        if (main == null) return null;

        AChoke choke = Chokes.mainChoke();
        if (choke == null) return main;

        return main.translatePercentTowards(-20, choke);
    }
}
