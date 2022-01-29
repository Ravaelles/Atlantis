package atlantis.debug.painter;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.map.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.ColorUtil;
import bwapi.Color;
import bwapi.Game;
import bwapi.Text;

/**
 * Here you can include code that will draw extra informations over units etc.
 */
public class APainter {

    public static final int MODE_NO_PAINTING = 1;
    public static final int MODE_PARTIAL_PAINTING = 2;
    public static final int MODE_FULL_PAINTING = 3;

    public static int paintingMode;
//    public static int paintingMode = MODE_PARTIAL_PAINTING;
//    public static int paintingMode = MODE_FULL_PAINTING;
//    public static int paintingMode = MODE_NO_PAINTING;

    protected static Game bwapi;

    // =========================================================

    public static void init() {
        if (Env.isLocal() && !Env.isParamTweaker()) {
            paintingMode = MODE_FULL_PAINTING;
        }
        else {
            paintingMode = MODE_NO_PAINTING;
        }
    }

    public static boolean isDisabled() {
        return paintingMode == MODE_NO_PAINTING;
    }

    public static void togglePainting() {
        if (paintingMode == MODE_NO_PAINTING) {
            paintingMode = MODE_FULL_PAINTING;
        } else {
            paintingMode = MODE_NO_PAINTING;
        }
    }

    public static void enablePainting() {
        paintingMode = MODE_FULL_PAINTING;
    }

    public static void disablePainting() {
        paintingMode = MODE_NO_PAINTING;
    }

    public static void paintSideMessage(String text, Color color) {
        paintSideMessage(text, color, 0);
    }

    public static void paintSideMessage(String text, Color color, int yOffset) {
        if (color == null) {
            color = Color.White;
        }

        int screenX = 10;
        int screenY = 5 + 9 * (yOffset == 0 ? AAdvancedPainter.sideMessageTopCounter : AAdvancedPainter.sideMessageBottomCounter);
        paintMessage(text, color, screenX, yOffset + screenY, true);

        if (yOffset == 0) {
            AAdvancedPainter.sideMessageTopCounter++;
        } else {
            AAdvancedPainter.sideMessageBottomCounter++;
        }
    }

    public static void paintMessage(String text, Color color, int x, int y, boolean screenCoord) {
        if (screenCoord) {
            bwapi.drawTextScreen(new APosition(x, y), ColorUtil.getColorString(color) + text);
        } else {
            bwapi.drawTextMap(new APosition(x, y), ColorUtil.getColorString(color) + text);
        }
    }

    public static void paintRectangle(HasPosition position, int width, int height, Color color) {
        if (isDisabled()) { return; }

        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position.position(), position.translateByPixels(width, height), color, false);
    }

    public static void paintRectangleFilled(APosition position, int width, int height, Color color) {
        if (isDisabled()) { return; }

        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, position.translateByPixels(width, height), color, true);
    }

    public static void paintCircle(AUnit unit, int radius, Color color) {
        paintCircle(unit.position(), radius, color);
    }

    public static void paintCircle(HasPosition position, int radius, Color color) {
        if (isDisabled()) { return; }

        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position.position(), radius, color, false);
    }

    public static void paintCircleFilled(HasPosition position, int radius, Color color) {
        if (isDisabled()) { return; }

        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position.position(), radius, color, true);
    }

    public static void paintCircleFilled(AUnit unit, int radius, Color color) {
        paintCircleFilled(unit.position(), radius, color);
    }

    public static void paintLine(HasPosition start, int dx, int dy, Color color) {
        if (isDisabled()) { return; }

        bwapi.drawLineMap(start.position(), start.translateByPixels(dx, dy), color);
    }

//    public static void paintLine(Position start, Position end, Color color) {
//        if (start == null || end == null || isDisabled()) {
//            return;
//        }
//        bwapi.drawLineMap(start, end, color);
//    }

    public static boolean paintLine(AUnit unit, AUnit end, Color color) {
        if (unit == null || end == null || isDisabled()) {
            return false;
        }
        bwapi.drawLineMap(unit.position(), end.position(), color);
        return true;
    }

    public static void paintLine(HasPosition unit, HasPosition end, Color color) {
        if (unit == null || end == null || isDisabled()) {
            return;
        }
        bwapi.drawLineMap(unit.position(), end.position(), color);
    }

    public static void paintTextCentered(AUnit unit, String text, Color color) {
        paintTextCentered(unit.position(), text, color, false);
    }

    public static void paintTextCentered(APosition position, String text, Color color) {
        paintTextCentered(position, text, color, false);
    }

    public static void paintTextCentered(HasPosition position, String text, Color color, double tileDX, double tileDY) {
        paintTextCentered(position.translateByPixels(
                (int) tileDX * 32, (int) tileDY * 32),
                text, color, false
        );
    }

    public static void paintTextCentered(AUnit unit, String text, boolean screenCords) {
        paintTextCentered(unit.position(), text, null, screenCords);
    }

    public static void paintTextCentered(APosition position, String text, boolean screenCords) {
        paintTextCentered(position, text, null, screenCords);
    }

    public static void paintTextCentered(APosition position, String text, Color color, boolean screenCoords) {
        if (position == null || text == null || isDisabled()) {
            return;
        }

        if (screenCoords) {
            bwapi.drawTextScreen(position.translateByPixels((int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        } else {
            bwapi.drawTextMap(position.translateByPixels((int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        }
    }

    public static void paintText(APosition position, String text, Color color) {
        if (position == null || text == null || isDisabled()) {
            return;
        }

        bwapi.drawTextMap(position, ColorUtil.getColorString(color) + text);
    }

    protected static void setTextSizeMedium() {
        bwapi.setTextSize(Text.Size.Default);
    }

    protected static void setTextSizeSmall() {
        bwapi.setTextSize(Text.Size.Small);
    }

    protected static void setTextSizeLarge() {
        bwapi.setTextSize(Text.Size.Large);
    }

    public static void paintChoke(AChoke choke, Color color, String extraText) {
        if (choke == null || isDisabled()) {
            return;
        }

        if ("".equals(extraText)) {
            extraText = choke.width() + " wide choke";
        }

        APainter.paintCircle(choke.center(), choke.width() * 32, color);
        APainter.paintTextCentered(
                choke.center().translateByTiles(0, choke.width()),
                extraText,
                color
        );
    }

    protected static void paintBase(APosition position, String text, Color color) {
        if (position == null || isDisabled()) {
            return;
        }

        paintRectangle(
                position.translateByPixels(-2 * 32, (int) -1.5 * 32),
                4 * 32, 3 * 32, color
        );
        APainter.paintTextCentered(position.translateByTiles(1, -1), text, color);
    }

    public static void assignBwapiInstance() {
        bwapi = Atlantis.game();
    }

}
