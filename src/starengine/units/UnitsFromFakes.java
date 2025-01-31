package starengine.units;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import starengine.assets.Images;
import starengine.assets.Map;
import starengine.StarEngine;
import tests.fakes.FakeUnit;

import java.awt.image.BufferedImage;

public class UnitsFromFakes extends Units {
    protected StarEngine engine;
    protected Map map;

    public UnitsFromFakes(StarEngine engine, Map map) {
        super(engine, map);

        assignUnitImages();
    }

    private void assignUnitImages() {
        for (AUnit au : Select.all().list()) {
            FakeUnit unit = (FakeUnit) au;

            UnitImageAssigner.assignImage(unit);
        }
    }
}
