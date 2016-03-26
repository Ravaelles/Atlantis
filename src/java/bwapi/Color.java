package bwapi;

import java.lang.Override;
import java.util.HashMap;
import java.util.Map;

/**
 * Starcraft uses a 256 color palette to render everything,
 * so the colors available for draw shapes using BWAPI is limited to the colors available in the <a href="http://bwapi.googlecode.com/svn/wiki/colorPalette.gif" target="_blank">Pallete</a>.
 * Several predefined colors from the pallete are provided.
 */
public class Color {

    private int r, g, b;

    /**
     * Create a color using the color in the palette that is closest to the RGB color specified. This will check a number of colors in the pallet to see which is closest to the specified color so this function is relatively slow.
     * @param r
     * @param g
     * @param b
     */
    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Color Red;

    public static Color Blue;

    public static Color Teal;

    public static Color Purple;

    public static Color Orange;

    public static Color Brown;

    public static Color White;

    public static Color Yellow;

    public static Color Green;

    public static Color Cyan;

    public static Color Black;

    public static Color Grey;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color)) return false;

        Color color = (Color) o;

        if (b != color.b) return false;
        if (g != color.g) return false;
        if (r != color.r) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + g;
        result = 31 * result + b;
        return result;
    }
}
