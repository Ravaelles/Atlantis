package atlantis.map.position;

import atlantis.units.select.Select;

public class GoTo {
    public static HasPosition orMain(HasPosition goTo) {
        if (goTo != null) return goTo;

        if (Select.main() != null) return Select.main();

        return Select.our().first();
    }
}
