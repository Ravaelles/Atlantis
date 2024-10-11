package atlantis.information.tech;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.util.cache.Cache;
import bwapi.TechType;

import java.util.List;

public class SpellCoordinator {

    private static Cache<APosition> recentSpellsPosition = new Cache<>();
    private static Cache<String> recentSpellsPositionToName = new Cache<>();

    public static boolean noOtherSpellAssignedHere(APosition targetPosition, TechType newSpell) {
//        recentSpellsAt.print("Recent spells", false);

        List<APosition> allSpells = recentSpellsPosition.allValid();
        if (allSpells.isEmpty()) return true;

        Positions<APosition> positions = new Positions<>(allSpells);

        for (APosition positionWithSpell : positions.inRadius(3, targetPosition).list()) {
            if (positionWithSpell.distTo(targetPosition) <= 3) {
                String spellNameHere = recentSpellsPositionToName.get(positionWithSpell.toString());
                if (spellNameHere != null && spellNameHere.equals(newSpell.name())) return false;
            }
        }

        return true;


//        APosition positionWithSpell = positions.inRadius(3, targetPosition).first();
//        if (positionWithSpell == null) return true;
//
////        if (positions.inRadius(3, targetPosition).notEmpty()) return false;
//
////        TechType spellHere = recentSpellsPositionToName.get(targetPosition.largeTile().toString());
//        String spellNameHere = recentSpellsPositionToName.get(targetPosition.largeTile().toString());
//
//        return spellNameHere == null || !spellNameHere.equals(newSpell.name());
    }

    public static void newSpellAt(APosition usedAt, TechType tech) {
        if (usedAt == null) return;
        
        recentSpellsPositionToName.set(usedAt.toStringPixels(), 105, tech.toString());
        recentSpellsPosition.set(usedAt.toStringPixels(), 105, usedAt);
    }

}
