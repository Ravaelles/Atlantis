package atlantis.util;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import bwapi.Color;

public class CenterCamera {
    public static void on(HasPosition position) {
        on(position, false, null);
    }

    public static void on(APosition position, boolean paintCircle) {
        on(position, paintCircle, null);
    }

    public static void on(HasPosition position, boolean paintCircle, Color color) {
        if (position == null) return;
        if (color == null) color = Color.Yellow;

        if (paintCircle) {
            AAdvancedPainter.paintCircle(position, 5, color);
            AAdvancedPainter.paintCircle(position, 6, color);
            AAdvancedPainter.paintCircle(position, 10, color);
            AAdvancedPainter.paintCircle(position, 11, color);
            AAdvancedPainter.paintCircle(position, 17, color);
            AAdvancedPainter.paintCircle(position, 18, color);
        }

        CameraCommander.centerCameraOn(position);
    }
}
