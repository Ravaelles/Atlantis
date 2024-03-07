package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;
import atlantis.map.position.APosition;
import bwapi.Color;

import java.util.ArrayList;

public class PathToEnemyFocus {
    public static AFocusPoint getIfApplies() {
        if (Alpha.count() <= 7) return null;

        ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();
        return selectChokeAndReturnFocusPoint(chokes);

//        return new AFocusPoint(choke, Select.mainOrAnyBuilding(), "PathToEnemy");
    }

    private static AFocusPoint selectChokeAndReturnFocusPoint(ArrayList<AChoke> chokes) {
        int currentIndex = 2;
        int prevIndex = currentIndex - 1;

        AChoke best = chokes.get(currentIndex);
        double bestScore = evalChoke(best);

        int iMin = currentIndex;
        int iMax = currentIndex + 3;
        for (currentIndex = iMin; currentIndex <= iMax; currentIndex++) {
            AChoke choke = chokes.get(currentIndex);
            double eval = evalChoke(choke);

            if (eval > bestScore) {
                prevIndex = currentIndex - 1;
                best = choke;
                bestScore = eval;
            }
        }

        APosition fromSide = chokes.get(prevIndex).center();
        APosition point = best.center().translateTilesTowards(6, fromSide);

//        AAdvancedPainter.paintCircle(point, 22, Color.Purple);
//        AAdvancedPainter.paintCircle(point, 20, Color.Purple);
//        AAdvancedPainter.paintCircle(point, 18, Color.Purple);

        return new AFocusPoint(point, fromSide, "PathToEnemy");
    }

    private static double evalChoke(AChoke choke) {
        return 100 - choke.width();
    }
}
