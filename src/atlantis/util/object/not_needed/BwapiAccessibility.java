package atlantis.util.object.not_needed;

import atlantis.game.AGame;
import atlantis.map.AMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.CappedList;
import bwapi.*;
import bwem.*;
import com.esotericsoftware.kryo.Kryo;

import java.util.*;

public class BwapiAccessibility {
    private static Kryo kryo = new Kryo();

    public static void makeAllBwapiClassesAndFieldsAccessible(Class<?> clazz, Kryo kryo) {
        registerInKryoAndMakeAccessibile(clazz);

        registerInKryoAndMakeAccessibile(List.class);
        registerInKryoAndMakeAccessibile(ArrayList.class);
        registerInKryoAndMakeAccessibile(CappedList.class);
        registerInKryoAndMakeAccessibile(Map.class);
        registerInKryoAndMakeAccessibile(HashMap.class);
        registerInKryoAndMakeAccessibile(Collection.class);

        registerInKryoAndMakeAccessibile(Area.class);
        registerInKryoAndMakeAccessibile(Base.class);
        registerInKryoAndMakeAccessibile(BWMap.class);
        registerInKryoAndMakeAccessibile(ChokePoint.class);
//        registerInKryoAndMakeAccessibile(Client.class);
        registerInKryoAndMakeAccessibile(Game.class);
        registerInKryoAndMakeAccessibile(Geyser.class);
        registerInKryoAndMakeAccessibile(Graph.class);
        registerInKryoAndMakeAccessibile(Pair.class);
        registerInKryoAndMakeAccessibile(Player.class);
        registerInKryoAndMakeAccessibile(Region.class);
        registerInKryoAndMakeAccessibile(Unit.class);

        registerInKryoAndMakeAccessibile(AUnit.class);
        registerInKryoAndMakeAccessibile(AMap.class);
        registerInKryoAndMakeAccessibile(AGame.class);
        registerInKryoAndMakeAccessibile(AUnitType.class);
    }

    public static void registerInKryoAndMakeAccessibile(Class<?> clazz) {
        kryo.register(clazz);
        Accessibility.makeAllAccessible(clazz);
    }
}
