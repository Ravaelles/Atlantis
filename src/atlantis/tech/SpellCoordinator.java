package atlantis.tech;

import atlantis.position.APosition;
import atlantis.util.Cache;
import bwapi.TechType;

public class SpellCoordinator {

    private static Cache<TechType> recentSpellsAt = new Cache<>();

    public static boolean noOtherSpellAssignedHere(APosition position, TechType newSpell) {
//        recentSpellsAt.print("Recent spells", false);

        TechType spellHere = recentSpellsAt.get(position.largeTile().toString());

        return spellHere == null || !spellHere.name().equals(newSpell.name());
    }

    public static void newSpellAt(APosition usedAt, TechType tech) {
        recentSpellsAt.set(usedAt.largeTile().toString(), 120, tech);
    }

}
