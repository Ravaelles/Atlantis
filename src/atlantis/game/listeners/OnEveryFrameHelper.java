package atlantis.game.listeners;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import bwapi.Color;

import java.util.ArrayList;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrameHelper {

    // Aux code to be executed at every frame
    public static void handle() {
    }

    private static void paintPathToEnemyBase(ArrayList<AChoke> chokes) {
        int chokeIndex = 0;
        APosition prevPoint = null;

        for (AChoke choke : chokes) {
            AAdvancedPainter.paintChoke(choke, Color.Orange, "Milestone=" + chokeIndex);

            if (prevPoint != null) {
                AAdvancedPainter.paintLine(prevPoint, choke.center(), Color.Orange);
            }
            prevPoint = choke.center();

            chokeIndex++;
        }
    }
}
