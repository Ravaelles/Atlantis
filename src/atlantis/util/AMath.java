package atlantis.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Pure numeric helpers (ranges, scaling, simple statistics), extracted from
 * the {@code A} god utility (REVIEW §4, Stage H).
 *
 * <p>No state, no game dependency: every method is a total function of its
 * arguments, which is what makes it testable without a running bot.</p>
 */
public class AMath {

    /**
     * Returns something like: 1d 3h 2m 53s
     */
    public static String convertSecondsToDisplayableFormat(int numberOfSeconds) {
        if (numberOfSeconds < 60) {
            return numberOfSeconds + "s";
        }
        else if (numberOfSeconds < 3600) {
            return numberOfSeconds / 60 + "m " + convertSecondsToDisplayableFormat(numberOfSeconds % 60);
        }
        else if (numberOfSeconds < 86400) {
            return numberOfSeconds / 3600 + "h " + convertSecondsToDisplayableFormat(numberOfSeconds % 3600);
        }
        else {
            return numberOfSeconds / 86400 + "d " + convertSecondsToDisplayableFormat(numberOfSeconds % 86400);
        }
    }

    /**
     * Returns value that is not less than min and not greater than max.
     */
    public static double forceValueInRange(double value, int min, int max) {
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    /**
     * Returns value that is not less than min and not greater than max.
     */
    public static int forceValueInRange(int value, int min, int max) {
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    /**
     * Returns median of given double list.
     */
    public static double median(Collection<Double> list) {
        return median(list, true);
    }

    /**
     * Returns median of given double list.
     *
     * @param mathematicMedian If true it will return normal median. If it is set to false and number of
     *                         elements is even the center (but lesser) element will be returned e.g. for [1 2 3 4] it would return 2.
     */
    public static double median(Collection<Double> list, boolean mathematicMedian) {
        if (list.isEmpty()) {
            AGui.displayMessage("List for computing a median is empty!");
            return -1;
        }
        if (list.size() == 1) {
            return list.iterator().next();
        }

        ArrayList<Double> sorted = new ArrayList<Double>();
        sorted.addAll(list);
        Collections.sort(sorted);

        int size = sorted.size();
        if (size % 2 == 0) {
            return sorted.get(sorted.size() / 2);
        }
        else if (mathematicMedian) {
            return (sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2 + 1)) / 2;
        }
        else {
            return (sorted.get(sorted.size() / 2));
        }
    }

    public static int getMaxElement(Collection<Integer> collection) {
        int max = -9999999;
        for (int number : collection) {
            if (max < number) {
                max = number;
            }
        }
        return max;
    }

    public static double inRange(double min, double value, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static int inRange(int min, int value, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static boolean isInRange(int min, int value, int max) {
        if (value < min) return false;
        if (value > max) return false;
        return true;
    }

    /**
     * Returns value that gradually changes from minValue to maxValue, according to paramValue position
     * between minParamValue and maxParamValue.
     */
    public static double gradual(
        double paramValue,
        double minParamValue, double maxParamValue,
        double minValue, double maxValue
    ) {
        if (paramValue <= minParamValue) return minValue;
        if (paramValue >= maxParamValue) return maxValue;
        double ratio = (paramValue - minParamValue) / (maxParamValue - minParamValue);
        return minValue + ratio * (maxValue - minValue);
    }
}
