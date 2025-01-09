package starengine.canvas.colors;

import java.awt.image.BufferedImage;

public class ImageReplaceColor {
    public static final int COLOR_THRESHOLD = 180;

    public static BufferedImage replaceMagentaWithOurColor(BufferedImage image) {
        return colorizeMagenta(image, 0, 120, 255, COLOR_THRESHOLD);
    }

    public static BufferedImage replaceMagentaWithEnemyColor(BufferedImage image) {
        return colorizeMagenta(image, 255, 175, 10, COLOR_THRESHOLD);
    }

    public static BufferedImage replaceMagentaWithNeutralColor(BufferedImage image) {
        return colorizeMagenta(image, 70, 70, 70, COLOR_THRESHOLD);
    }

    private static BufferedImage colorizeMagenta(
        BufferedImage image, int targetR, int targetG, int targetB, int colorThreshold
    ) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);

                // Extract color components
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Check if the color is close to magenta (255, 0, 255)
                if (Math.abs(red - 255) <= colorThreshold &&
                    Math.abs(green - 0) <= colorThreshold &&
                    Math.abs(blue - 255) <= colorThreshold) {

                    // Compute grayscale intensity based on original magenta shades
                    int intensity = (red + blue) / 2; // Averaging red & blue (green is 0)

                    // Scale target color by intensity to preserve shading
                    int newR = (targetR * intensity) / 255;
                    int newG = (targetG * intensity) / 255;
                    int newB = (targetB * intensity) / 255;

                    // Reconstruct the new pixel with preserved transparency
                    int newRgb = (alpha << 24) | (newR << 16) | (newG << 8) | newB;
                    image.setRGB(x, y, newRgb);
                }
            }
        }
        return image;
    }
}
