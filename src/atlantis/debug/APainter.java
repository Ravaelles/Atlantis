package atlantis.debug;

import atlantis.Atlantis;
import atlantis.map.AChokepoint;
import atlantis.position.APosition;
import atlantis.position.PositionHelper;
import atlantis.units.AUnit;
import atlantis.util.CodeProfiler;
import atlantis.util.ColorUtil;
import bwapi.*;

/**
 * Here you can include code that will draw extra informations over units etc.
 */
public class APainter {

    public static final int MODE_NO_PAINTING = 1;
    public static final int MODE_PARTIAL_PAINTING = 2;
    public static final int MODE_FULL_PAINTING = 3;

//    public static int paintingMode = MODE_NO_PAINTING;
//    public static int paintingMode = MODE_PARTIAL_PAINTING;
    public static int paintingMode = MODE_FULL_PAINTING;

    protected static Game bwapi;

    // =========================================================

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

    public static void paintRectangle(APosition position, int width, int height, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, PositionHelper.translateByPixels(position, width, height), color, false);
    }

    public static void paintRectangleFilled(APosition position, int width, int height, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawBoxMap(position, PositionHelper.translateByPixels(position, width, height), color, true);
    }

    public static void paintCircle(AUnit unit, int radius, Color color) {
        paintCircle(unit.getPosition(), radius, color);
    }

    public static void paintCircle(Position position, int radius, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position, radius, color, false);
    }

    public static void paintCircleFilled(Position position, int radius, Color color) {
        if (position == null) {
            return;
        }
        bwapi.drawCircleMap(position, radius, color, true);
    }

    public static void paintLine(APosition start, int dx, int dy, Color color) {
        paintLine(start, PositionHelper.translateByPixels(start, dx, dy), color);
    }

    public static void paintLine(Position start, Position end, Color color) {
        if (start == null || end == null) {
            return;
        }
        bwapi.drawLineMap(start, end, color);
    }

    public static void paintLine(AUnit unit, AUnit end, Color color) {
        if (unit == null || end == null) {
            return;
        }
        bwapi.drawLineMap(unit.getPosition(), end.getPosition(), color);
    }

    public static void paintLine(AUnit unit, Position end, Color color) {
        if (unit == null || end == null) {
            return;
        }
        bwapi.drawLineMap(unit.getPosition(), end, color);
    }

    public static void paintTextCentered(AUnit unit, String text, Color color) {
        paintTextCentered(unit.getPosition(), text, color, false);
    }

    public static void paintTextCentered(APosition position, String text, Color color) {
        paintTextCentered(position, text, color, false);
    }

    public static void paintTextCentered(APosition position, String text, Color color, double tileDX, double tileDY) {
        paintTextCentered(position.translateByPixels(
                (int) tileDX * 32, (int) tileDY * 32),
                text, color, false
        );
    }

    public static void paintTextCentered(AUnit unit, String text, boolean screenCords) {
        paintTextCentered(unit.getPosition(), text, null, screenCords);
    }

    public static void paintTextCentered(APosition position, String text, boolean screenCords) {
        paintTextCentered(position, text, null, screenCords);
    }

    public static void paintTextCentered(APosition position, String text, Color color, boolean screenCoords) {
        if (position == null || text == null) {
            return;
        }

        if (screenCoords) {
            bwapi.drawTextScreen(PositionHelper.translateByPixels(position, (int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        } else {
            bwapi.drawTextMap(PositionHelper.translateByPixels(position, (int) (-2.7 * text.length()), -2),
                    ColorUtil.getColorString(color) + text
            );
        }
    }

    public static void paintText(APosition position, String text, Color color) {
        if (position == null || text == null) {
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

    protected static void paintChoke(AChokepoint choke, Color color, String extraText) {
        if (choke == null) {
            return;
        }

        if ("".equals(extraText)) {
            extraText = choke.getWidth() + " wide choke";
        }

        APainter.paintCircle(choke.getCenter(), choke.getWidth() * 32, color);
        APainter.paintTextCentered(
                choke.getCenter().translateByTiles(0, choke.getWidth()),
                extraText,
                color
        );
    }

    protected static void paintBase(APosition position, String text, Color color) {
        if (position == null) {
            return;
        }

        paintRectangle(
                position.translateByPixels(-2 * 32, (int) -1.5 * 32),
                4 * 32, 3 * 32, color
        );
        APainter.paintTextCentered(position.translateByTiles(1, -1), text, color);
    }
}
