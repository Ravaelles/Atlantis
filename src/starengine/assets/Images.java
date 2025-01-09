package starengine.assets;

import atlantis.game.A;
import atlantis.units.AUnitType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Images {
    public static final String STARENGINE_DIR = "./img/starengine/";
    public static BufferedImage groundImage;
    public static ImageIcon atlantisIcon;
    public static BufferedImage dragoonOur;
    public static BufferedImage dragoonEnemy;
    public static BufferedImage nonWalkable;

    private static final HashMap<AUnitType, BufferedImage> unitTypesToImages = new HashMap<>();

    // =========================================================

    public static void loadAllImages() {
        loadAtlantisIcon();
        loadDragoonImages();
        loadGroundImage();
        loadNonWalkablePattern();
    }

    private static void loadDragoonImages() {
        dragoonEnemy = loadStarEngineUnitImage("Protoss_Dragoon_enemy.png");
        dragoonOur = loadStarEngineUnitImage("Protoss_Dragoon_our.png");
    }

    // =========================================================

    private static void loadAtlantisIcon() {
        atlantisIcon = new ImageIcon("./icons/Atlantis.png");
    }

    private static void loadGroundImage() {
        groundImage = loadStarEngineGenericImage("ground.jpg");
    }

    private static void loadNonWalkablePattern() {
        nonWalkable = loadStarEngineGenericImage("non_walkable.png");
    }

    // =========================================================

    private static BufferedImage loadStarEngineUnitImage(String s) {
        return loadBufferedImage(STARENGINE_DIR + "units/" + s);
    }

    private static BufferedImage loadStarEngineGenericImage(String s) {
        return loadBufferedImage(STARENGINE_DIR + "generic/" + s);
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

    public static BufferedImage getByType(AUnitType type) {
        if (unitTypesToImages.containsKey(type)) {
            return unitTypesToImages.get(type);
        }

        BufferedImage image = loadStarEngineUnitImage(type.fullName() + ".png");
        unitTypesToImages.put(type, image);
        return image;
    }
}
