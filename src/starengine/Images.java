package starengine;

import atlantis.game.A;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Images {
    public static BufferedImage groundImage;
    public static ImageIcon atlantisIcon;
    public static BufferedImage dragoonOur;
    public static BufferedImage dragoonEnemy;
    public static BufferedImage nonWalkable;

    // =========================================================

    public static void loadAllImages() {
        loadAtlantisIcon();
        loadDragoons();
        loadGroundImage();
        loadNonWalkablePattern();
    }

    private static void loadDragoons() {
        dragoonEnemy = loadStarEngineImage("dragoon-enemy.png");
        dragoonOur = loadStarEngineImage("dragoon-our.png");
    }

    // =========================================================

    private static void loadAtlantisIcon() {
        atlantisIcon = new ImageIcon("./icons/Atlantis.png");
    }

    private static void loadGroundImage() {
        groundImage = loadStarEngineImage("ground.jpg");
    }

    private static void loadNonWalkablePattern() {
        nonWalkable = loadStarEngineImage("non_walkable.png");
    }

    // =========================================================

    private static BufferedImage loadStarEngineImage(String s) {
        return loadBufferedImage("./img/starengine/" + s);
    }

    private static BufferedImage loadBufferedImage(String path) {
        try {
            return ImageIO.read(new File(path));

        } catch (IOException e) {
            A.errPrintln("Error loading image: " + path);
            e.printStackTrace();
            A.quit();
        }
        return null;
    }
}
