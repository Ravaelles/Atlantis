package atlantis.util;

import org.openbw.bwapi4j.type.Color;

public class ColorUtil {

    public static String getColorString(Color color) {
        if (color == null) {
            return "";
        }

        if (color.equals(Color.CYAN)) {
            return "\u0002";
        }
        if (color.equals(Color.YELLOW)) {
            return "\u0003";
        }
        if (color.equals(Color.WHITE)) {
            return "\u0004";
        }
        if (color.equals(Color.GREY)) {
            return "\u0005";
        }
        if (color.equals(Color.RED)) {
            return "\u0006";
        }
        if (color.equals(Color.GREEN)) {
            return "\u0007";
        }
        if (color.equals(Color.BLUE)) {
            return "\u000E";
        }
        if (color.equals(Color.PURPLE)) {
            return "\u0010";
        }
        if (color.equals(Color.ORANGE)) {
            return "\u0011";
        }
        if (color.equals(Color.BROWN)) {
            return "\u0015";
        }
        // case :
        // return "\u0018";
        // case :
        // return "\u0019";
        // case :
        // return "\u001A";
        // case :
        // return "\u001B";
        // case :
        // return "\u001C";
        // case :
        // return "\u001D";
        // case :
        // return "\u001E";
        if (color.equals(Color.TEAL)) {
            return "\u001F";
        }

        return "";	//default
    }
}
