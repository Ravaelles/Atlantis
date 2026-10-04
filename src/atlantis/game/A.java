package atlantis.game;

import atlantis.Atlantis;
import atlantis.decisions.Decision;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.game.player.Enemy;
import bwapi.Game;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.*;
import atlantis.util.GameClock;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Utility helper class A(tlantis). Makes it much easier to execute many commonly used methods.
 */
public class A {
    /**
     * Seconds - real time. The only counter left as a public field; the frame number
     * it used to sit next to had no production reader any more (everything reads
     * {@link #now()}), so it went on 2026-10-04 and {@link #setNow} is the only writer
     * of what is left.
     */
    public static int s;

    /**
     * <b>Random</b> object that can be used in any part of code.
     */
    public static Random random = new Random();

    public static Game game() {
        return Atlantis.game();
    }


    /**
     * @return string like "2011-09-03"
     */
    public static String getCurrentDateInFormatYMDHHmm() {
        return dateYear() + "-" + dateMonth() + "-" + dateDay() + " " + dateHours() + ":" + dateMinutes();
    }

    private static String dateYear() {
        String year = (new GregorianCalendar()).get(Calendar.YEAR) + "";
        if (year.length() < 2) {
            year = "0" + year;
        }
        return year;
    }

    private static String dateMonth() {
        String month = (new GregorianCalendar()).get(Calendar.MONTH) + "";
        if (month.length() < 2) {
            month = "0" + month;
        }
        return month;
    }

    private static String dateDay() {
        String day = (new GregorianCalendar()).get(Calendar.DAY_OF_MONTH) + "";
        if (day.length() < 2) {
            day = "0" + day;
        }
        return day;
    }

    private static String dateHours() {
        String hours = (new GregorianCalendar()).get(Calendar.HOUR_OF_DAY) + "";
        if (hours.length() < 2) {
            hours = "0" + hours;
        }
        return hours;
    }

    private static String dateMinutes() {
        String minutes = (new GregorianCalendar()).get(Calendar.MINUTE) + "";
        if (minutes.length() < 2) {
            minutes = "0" + minutes;
        }
        return minutes;
    }


    /**
     * Returns string like 21:12:59
     */
    public static String getCurrentTimeAsString() {
        GregorianCalendar today = new GregorianCalendar();
        String hour = today.get(GregorianCalendar.HOUR_OF_DAY) + "";
        int minutes = today.get(GregorianCalendar.MINUTE);
        int seconds = today.get(GregorianCalendar.SECOND);

        return (hour.length() < 2 ? ("0" + hour) : hour)
            + ":" + (minutes <= 9 ? "0" + minutes : minutes)
            + ":" + (seconds <= 9 ? "0" + seconds : seconds);
    }


    /**
     * Returns map containing number of occurences of each element in given collection.
     */
    public static TreeMap<String, Integer> getOccurenceMap(Collection<String> collection) {
        TreeMap<String, Integer> occurences = new TreeMap<String, Integer>();
        for (String string : collection) {
            if (occurences.containsKey(string)) {
                occurences.put(string, occurences.get(string) + 1);
            }
            else {
                occurences.put(string, 1);
            }
        }
        return occurences;
    }


    /**
     *
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascending) {
        final int compareModifier = ascending ? 1 : -1;
        java.util.List<Map.Entry<K, V>> list = new LinkedList<java.util.Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return compareModifier * (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    public static int countSubstrings(String str, String subStr) {
        return (str.length() - str.replaceAll(Pattern.quote(subStr), "").length()) / subStr.length();
    }

    /**
     * 12.665 => "12.6"
     */
    public static String digit(double number) {
        return String.format("%.1f", number).replace(",", ".");
    }

    public static String trueFalse(boolean bool) {
        return bool ? "Yes" : "No";
    }

    public static String at() {
        return "@" + now() + " ";
    }

    public static int now() {
        return AGame.now();
    }

    /**
     * The only place the frame and second counters get written.
     *
     * <p>There are two views of the counters: this field ({@code s}, for the four
     * production classes that still read it) and {@link atlantis.util.GameClock}, which
     * the kernel reads because {@code atlantis.util} may not depend on
     * {@code atlantis.game}. Both are written here, in one breath, so they cannot drift
     * apart - which they did: the acceptance tier's
     * {@code AbstractWorldCreatingTest.onFrameStart} wrote the fields without
     * publishing, so kernel code reading the clock there saw the previous frame (found
     * by the GLM review of 2026-10-04, F-1).</p>
     *
     * <p>Three writers, all here: the game layer once per frame
     * ({@code AGame.calcSeconds}) and the two harnesses
     * ({@code AbstractTestWithUnits.useFakeTime},
     * {@code AbstractWorldCreatingTest.onFrameStart}).</p>
     *
     * <p>{@code s} still has four readers ({@code NeedChokeBlockers},
     * {@code DontAttackOverlords}, {@code ProtossForceFight},
     * {@code LeaderProgressFlagToNextFocusChoke}), so it stays a field; moving those
     * four to {@link #seconds()} is a separate change, and unlike the frame field it
     * is not a dead one.</p>
     */
    public static void setNow(int framesNow, int secondsNow) {
        s = secondsNow;

        GameClock.publish(framesNow, secondsNow);
    }


    public static int seconds() {
        return AGame.timeSeconds();
    }

    public static int ago(int frame) {
        return A.now() - frame;
    }

    public static double secondsAgo(int frame) {
        return (A.now() - frame) / 30.0;
    }


    public static boolean everyNthGameFrame(int n) {
        return A.now() % n == 0;
    }


    public static boolean isUms() {
        return AGame.isUms();
    }

    public static boolean notUms() {
        return !AGame.isUms();
    }

    /**
     * Returns string like "(0.4)"
     */
    public static String dist(double dist) {
        return "(" + A.digit(dist) + ")";
    }

    public static String dist(AUnit unit1, HasPosition unit2) {
        return "(" + (unit1 != null && unit2 != null ? A.digit(unit1.distTo(unit2)) : "-") + ")";
    }

    public static String distGround(AUnit unit1, HasPosition unit2) {
        return "(" + (unit1 != null && unit2 != null ? A.digit(unit1.groundDist(unit2)) : "-") + ")";
    }

    public static int minerals() {
        return AGame.minerals();
    }

    public static int gas() {
        return AGame.gas();
    }

    public static int supplyUsed() {
        return AGame.supplyUsed();
    }

    public static int supplyFree() {
        return AGame.supplyFree();
    }

    public static int supplyTotal() {
        return AGame.supplyTotal();
    }

    public static boolean supplyUsed(int min) {
        return AGame.supplyUsed() >= min;
    }


    public static int resourcesBalance() {
        return AGame.killsLossesResourceBalance();
    }

    public static boolean hasFreeSupply(int supplyNeeded) {
        return AGame.supplyFree() >= supplyNeeded;
    }


    public static boolean hasMinerals(int minerals) {
        return A.minerals() >= minerals;
    }

    public static boolean hasMineralsAndGas(int minerals, int gas) {
        return hasMinerals(minerals) && hasGas(gas);
    }

    public static boolean hasGas(int gas) {
        return A.gas() >= gas;
    }

    /**
     * Returns false once per n game frames.
     */
    public static boolean everyFrameExceptNthFrame(int n) {
        return Atlantis.game().getFrameCount() % n != 0;
    }


    public static long realSecondsNow() {
        return Instant.now().getEpochSecond();
    }


    public static void sleep(int ms) {
        try {
//            TimeUnit.MILLISECONDS.sleep(ms);
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException in A.sleep");
        }
    }

    public static int reservedMinerals() {
        return ReservedResources.minerals();
    }


    public static void quit() {
        AGame.exit();
    }

    public static String ucfirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String substring(String str, int start, int end) {
        return str.substring(start, Math.min(end, str.length()));
    }


    /**
     * Returns true if we can afford minerals and gas for given unit type.
     */
    public static boolean canAfford(AUnitType unitType) {
        return AGame.minerals() >= unitType.mineralPrice() && AGame.gas() >= unitType.gasPrice();
    }

    /**
     * Returns true if we can afford minerals and gas for given upgrade.
     */
    public static boolean canAfford(UpgradeType upgrade) {
        return AGame.minerals() >= upgrade.mineralPrice() && AGame.gas() >= upgrade.gasPrice();
    }

    public static boolean canAfford(TechType tech) {
        return AGame.minerals() >= tech.mineralPrice() && AGame.gas() >= tech.gasPrice();
    }

    /**
     * Returns true if we can afford both so many minerals and gas at the same time.
     */
    public static boolean canAfford(int minerals, int gas) {
        return AGame.minerals() >= minerals && AGame.gas() >= gas;
    }

    /**
     * Returns true if we can afford both so many minerals and gas at the same time.
     * Takes into account planned constructions and orders.
     */
    public static boolean canAffordWithReserved(int minerals, int gas) {
        return canAfford(
            minerals + ReservedResources.minerals(),
            gas + ReservedResources.gas()
        ) || canAfford(Math.min(minerals, 550), Math.min(minerals, 250));
    }

    public static boolean canAffordWithReserved(int minerals) {
        return canAfford(
            minerals + ReservedResources.minerals(),
            0
        ) || canAfford(Math.min(minerals, 550), 0);
    }

    public static boolean canAffordWithReserved(AUnitType type) {
        return canAffordWithReserved(type.mineralPrice(), type.gasPrice());
    }

    public static boolean canAffordWithReserved(TechType type) {
        return canAffordWithReserved(type.mineralPrice(), type.gasPrice());
    }

    public static boolean canAffordWithReserved(UpgradeType type) {
        return canAffordWithReserved(type.mineralPrice(), type.gasPrice());
    }

    public static String keysToString(Set<? extends Object> keys) {
        Object[] strings = keys.toArray(new Object[keys.size()]);

        StringBuilder toString = new StringBuilder();
        toString.append("[");
        for (Object object : strings) {
            toString.append(object).append(",");
        }
        toString.append("]");
        return toString.toString();
    }

    public static int whenEnemyProtossTerranZerg(int ifEnemyProtoss, int ifEnemyTerran, int ifEnemyZerg) {
        if (Enemy.protoss()) return ifEnemyProtoss;
        if (Enemy.terran()) return ifEnemyTerran;
        return ifEnemyZerg;
    }

    public static int whenEnemyProtoss(int ifEnemyProtoss, int otherwise) {
        if (Enemy.protoss()) return ifEnemyProtoss;
        return otherwise;
    }

    public static double whenEnemyProtoss(double ifEnemyProtoss, double otherwise) {
        if (Enemy.protoss()) return ifEnemyProtoss;
        return otherwise;
    }

    public static int whenEnemyZerg(int ifEnemyZerg, int otherwise) {
        if (Enemy.zerg()) return ifEnemyZerg;
        return otherwise;
    }

    public static double whenEnemyZerg(double ifEnemyZerg, double otherwise) {
        if (Enemy.zerg()) return ifEnemyZerg;
        return otherwise;
    }

    public static int whenEnemyProtossZerg(int ifEnemyProtoss, int ifEnemyZerg) {
        if (Enemy.protoss()) return ifEnemyProtoss;
        if (Enemy.terran()) return 0;
        return ifEnemyZerg;
    }

    public static double whenEnemyProtossTerranZerg(double ifEnemyProtoss, double ifEnemyTerran, double ifEnemyZerg) {
        if (Enemy.protoss()) return ifEnemyProtoss;
        if (Enemy.terran()) return ifEnemyTerran;
        return ifEnemyZerg;
    }

    public static Decision whenEnemyProtossTerranZerg(
        Callable ifEnemyProtoss, Callable ifEnemyTerran, Callable ifEnemyZerg
    ) {
        try {
            if (Enemy.protoss()) return (Decision) ifEnemyProtoss.call();
            if (Enemy.terran()) return (Decision) ifEnemyTerran.call();
            return (Decision) ifEnemyZerg.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String minSec() {
        int minutes = A.s / 60;
        int seconds = A.s % 60;

        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
