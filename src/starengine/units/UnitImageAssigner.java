package starengine.units;

import starengine.assets.Images;
import starengine.canvas.colors.ImageReplaceColor;
import tests.fakes.FakeUnit;

import java.awt.image.BufferedImage;

public class UnitImageAssigner {
    public static void assignImage(FakeUnit unit) {
        BufferedImage image = defineBaseImage(unit);
        image = colorTheImage(image, unit);
//        image = sizeTheImage(image, unit);

        unit.setImage(image);
    }

//    private static BufferedImage sizeTheImage(BufferedImage image, FakeUnit unit) {
//        if (unit.isOur()) {
//            return Images.resize(image, unit.width(), unit.height());
//        }
//
//        return Images.resize(image, unit.width(), unit.height());
//    }

    private static BufferedImage colorTheImage(BufferedImage image, FakeUnit unit) {
        if (unit.isOur()) {
            return ImageReplaceColor.replaceMagentaWithOurColor(image);
        }
        else if (unit.isEnemy()) {
            return ImageReplaceColor.replaceMagentaWithEnemyColor(image);
        }

        return ImageReplaceColor.replaceMagentaWithNeutralColor(image);
    }

    private static BufferedImage defineBaseImage(FakeUnit unit) {
//        if (unit.isOur() && unit.isDragoon()) {
//            return Images.dragoonOur;
//        }

        return Images.getByType(unit.type());
    }
}
