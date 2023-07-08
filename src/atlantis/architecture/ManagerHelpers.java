package atlantis.architecture;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class ManagerHelpers {

    protected static Class<? extends Manager>[] mergeClasses(Class[] raceSpecific, Class[] generic) {
        return (Class<? extends Manager>[]) Stream.concat(Arrays.stream(raceSpecific), Arrays.stream(generic)).toArray();
    }

}
