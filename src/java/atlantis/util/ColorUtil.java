package atlantis.util;

import bwapi.Color;

public class ColorUtil {

    public static String getColorString(Color color) {
        if (color == null) {
            return "";
        }

        if (color.equals(Color.Cyan)) {
            return "\u0002";
        }
        if (color.equals(Color.Yellow)) {
            return "\u0003";
        }
        if (color.equals(Color.White)) {
            return "\u0004";
        }
        if (color.equals(Color.Grey)) {
            return "\u0005";
        }
        if (color.equals(Color.Red)) {
            return "\u0006";
        }
        if (color.equals(Color.Green)) {
            return "\u0007";
        }
        if (color.equals(Color.Blue)) {
            return "\u000E";
        }
        if (color.equals(Color.Purple)) {
            return "\u0010";
        }
        if (color.equals(Color.Orange)) {
            return "\u0011";
        }
        if (color.equals(Color.Brown)) {
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
        if (color.equals(Color.Teal)) {
            return "\u001F";
        }

        return "";	//default
    }
}
