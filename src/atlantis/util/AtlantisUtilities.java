package atlantis.util;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Rafaelles <rafaelles.org>
 */
public class AtlantisUtilities {

    /**
     * <b>Random</b> object that can be used in any part of code.
     */
    public static final Random random = new Random();

    /**
     * Displays small window with <b>text</b> information. Very useful for testing, error reporting.
     */
    public static void displayMessage(String text) {
        JOptionPane.showMessageDialog(new JOptionPane(), text, "", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Displays small window with <b>text</b> information and with <b>title</b> title. Very useful for
     * testing, error reporting.
     */
    public static void displayMessage(String title, String text) {
        JOptionPane.showMessageDialog(new JOptionPane(), text, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Displays small window showing that some error has occured, window has <b>errorText</b> information.
     */
    public static void displayError(String errorText) {
        JOptionPane.showMessageDialog(new JOptionPane(), errorText, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays small window showing that some error has occured, window has <b>errorText</b> information and
     * <b>title</b> title.
     */
    public static void displayError(String title, String errorText) {
        JOptionPane.showMessageDialog(new JOptionPane(), errorText, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Prints the list of the given argument, separated with commas.
     */
    public static void print(Object... args) {
        System.out.print(args[0]);

        if (args.length > 1) {
            System.out.print(", ");
        }

        for (int i = 1; i < args.length - 1; i++) {
            System.out.print(args[i] + ", ");
        }

        System.out.println(args[args.length - 1]);
    }

    /**
     * @return exception stack converted to String (each trace in new line)
     */
    public static String convertStackToString(StackTraceElement[] stackTrace) {
        return convertStackToString(stackTrace.length, stackTrace);
    }

    /**
     * @return exception stack converted to String (each trace in new line)
     * @param maxLines maximum number of lines of result String
     */
    public static String convertStackToString(int maxLines, StackTraceElement[] stackTrace) {
        String result = "";

        for (int i = 0; i < stackTrace.length && i < maxLines; i++) {
            result += stackTrace[i];

            if (i != stackTrace.length - 1) {
                result += "\n";
            }
        }
        // for (int i = stackTrace.length - 1; i >= 0; i--) {
        // result += stackTrace[i];
        //
        // if (i != 0)
        // result += "\n";
        // }

        return result;
    }

    /**
     * @param percentChance is chance percentage of some action, e.g. 87.2 means some event occurs with 87.2%
     * probability
     * @return true if given random event occured
     */
    public static boolean chanceOfPercent(double percentChance) {
        return random.nextDouble() <= (percentChance / 100);
    }

    /**
     * @return random integer number from range [min, max]
	 *
     */
    public static int rand(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * @return String representing a number with given decimal places e.g. (3.1415, 2) will give "3.14"
     * @param number number that you want to format
     * @param decimalPlaces how many digits will be after '.'
     */
    public static String formatDecimalPlaces(double number, int decimalPlaces) {
        String zeros = "";
        for (int i = 0; i < decimalPlaces; i++) {
            zeros += "0";
        }
        return new DecimalFormat("0." + zeros).format(decimalPlaces).replace(',', '.');
    }

    /**
     * Makes sure each object in given panel (and its children) has specified color.
     */
    public static void setAllBackgroundsColorsOfComponent(Container container, Color desiredBackgroundColor) {
        container.setBackground(desiredBackgroundColor);
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                setAllBackgroundsColorsOfComponent((Container) component, desiredBackgroundColor);
            } else {
                component.setBackground(desiredBackgroundColor);
            }
        }
    }

    /**
     * Makes sure each object in given panel (and its children) has specified color.
     */
    public static void setAllBackgroundsColorsOfJTextField(Container container, Color desiredBackgroundColor) {
        if (container instanceof JTextField || container instanceof JTextArea) {
            container.setBackground(desiredBackgroundColor);
        }

        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                setAllBackgroundsColorsOfComponent((Container) component, desiredBackgroundColor);
            } else {
                component.setBackground(desiredBackgroundColor);
            }
        }
    }

    /**
     * Makes sure each object in given panel (and its children) has specified color.
     */
    public static void setAllBackgroundsColorsOfButtons(Container container, Color desiredBackgroundColor) {
        container.setBackground(desiredBackgroundColor);
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                setAllBackgroundsColorsOfComponent((Container) component, desiredBackgroundColor);
            } else if (component instanceof Button || component instanceof JButton) {
                component.setBackground(desiredBackgroundColor);
            }
        }
    }

    /**
     * Makes sure each object in given panel (and its children) has specified color.
     */
    public static void setAllForegroundsColorsOfComponent(Container container, Color desiredForegroundColor) {
        container.setForeground(desiredForegroundColor);

        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                setAllForegroundsColorsOfComponent((Container) component, desiredForegroundColor);
            } else {
                component.setForeground(desiredForegroundColor);
            }
        }
    }

    /**
     * Makes sure each object in given panel (and its children) has specified color.
     */
    public static void setAllButtonsOfComponent(Container container, Color backgroundColor, Color fontColor,
            Border border) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                component.setBackground(backgroundColor);
                component.setForeground(fontColor);
                ((JButton) component).setBorder(border);
            } else if (component instanceof JPanel) {
                setAllButtonsOfComponent((JButton) component, backgroundColor, fontColor, border);
            }
        }
    }

    /**
     * @return true if the string has at least one character
     */
    public static boolean isStringNotEmpty(String string) {
        return string != null && !string.isEmpty() && string.charAt(0) != ' ';
    }

    /**
     * @return true if given extension (like "png", "txt") is equal png, jpg, jpeg, bmp or gif
     */
    public static boolean isImage(String extension) {
        extension = extension.toLowerCase();
        if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg") || extension.equals("bmp")
                || extension.equals("gif")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if given file's extension (like "png", "txt") is equal png, jpg, jpeg, bmp or gif
     */
    public static boolean isFileImage(File file) {
        return isImage(file.getName().substring(file.getName().lastIndexOf('.') + 1));
    }

    /**
     * Displays given exception in user friendly way (with exception name and stack).
     */
    public static void displayException(Exception e) {
        displayException(e, "Błąd", "Wystąpił błąd!");
    }

    /**
     * Displays given exception in user friendly way (with exception name and stack).
     */
    public static void displayException(Exception e, String title, String preText) {
        AtlantisUtilities.displayError(title,
                preText + "\n\n" + e.getMessage() + "\n\n" + AtlantisUtilities.convertStackToString(10, e.getStackTrace()));
    }

    /**
     * Displays popup with title @title and content @text with possible options yes and no.
     *
     * @return true if user clicked yes
     * @return false if user clicked no
     */
    public static boolean displayYesNoPopup(String title, String text) {
        if (JOptionPane.showConfirmDialog(new JOptionPane(), text, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Displays new JFrame with given panel, as centered window.
     */
    public static JFrame displayPanelAsCenteredFrame(JPanel panel, String frameTitle, Image frameIcon) {
        return displayPanelAsCenteredFrame(panel, frameTitle, frameIcon, true);
    }

    /**
     * Displays new JFrame with given panel, as centered window.
     */
    public static JFrame displayPanelAsCenteredFrame(JPanel panel, String frameTitle, Image frameIcon, boolean visible) {
        JFrame frame = new JFrame();
        if (frameIcon != null) {
            frame.setIconImage(frameIcon);
        }
        frame.setTitle(frameTitle);
        panel.setSize(panel.getPreferredSize());
        panel.setVisible(true);
        frame.add(panel);
        frame.setSize(panel.getSize());
        AtlantisUtilities.centerFrameOnScreen(frame);
        frame.setVisible(visible);
        return frame;
    }

    /**
     * Displays given frame exactly centered in the screen.
     */
    public static JFrame centerFrameOnScreen(JFrame frame) {
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenDimension.width / 2 - frame.getWidth() / 2;
        int y = screenDimension.height / 2 - frame.getHeight() / 2;
        frame.setLocation(x, y);
        return frame;
    }

    /**
     * @return string like "2011-09-03"
     */
    public static String getCurrentDateInFormatYMD() {
        GregorianCalendar date = new GregorianCalendar();
        String month = date.get(Calendar.MONTH) + "";
        String day = date.get(Calendar.DAY_OF_MONTH) + "";

        if (month.length() < 2) {
            month = "0" + month;
        }
        if (day.length() < 2) {
            day = "0" + day;
        }

        return date.get(Calendar.YEAR) + "-" + month + "-" + day;
    }

    /**
     * @return string like "2011-09-03"
     */
    public static String getDateInFormatYMD(Calendar date) {
        String month = date.get(Calendar.MONTH) + "";
        String day = date.get(Calendar.DAY_OF_MONTH) + "";

        if (month.length() < 2) {
            month = "0" + month;
        }
        if (day.length() < 2) {
            day = "0" + day;
        }

        String text = date.get(Calendar.YEAR) + "-" + month + "-" + day;
        String result = "";
        for (String part : AtlantisUtilities.implodeList(text, '-')) {
            if (result.length() > 0) {
                result += ".";
            }
            if (part.length() < 2) {
                result += "0" + part;
            } else {
                result += part;
            }
        }
        return result;
    }

    /**
     * @return string without last character or empty string if there was one or less characters.
     */
    public static String removeLastChar(String inputString) {
        if (inputString.length() > 1) {
            return inputString.substring(0, inputString.length() - 1);
        } else {
            return "";
        }
    }

    /**
     * @return today's date.
     */
    public static GregorianCalendar getToday() {
        return new GregorianCalendar();
    }

    /**
     * @return string like "2011-03-07", based on today's date.
     */
    public static String getTodayAsString() {
        GregorianCalendar today = new GregorianCalendar();
        return today.get(GregorianCalendar.YEAR) + "-" + today.get(GregorianCalendar.MONTH) + "-"
                + today.get(GregorianCalendar.DAY_OF_MONTH);
    }

    /**
     * @return string like "2011-03-07", based on yesterday's date.
     */
    public static String getYesterdayAsString() {
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
        return yesterday.get(GregorianCalendar.YEAR) + "-" + yesterday.get(GregorianCalendar.MONTH) + "-"
                + yesterday.get(GregorianCalendar.DAY_OF_MONTH);
    }

    /**
     * Returns string like 21:12:59
     */
    private static String getCurrentTimeAsString() {
        GregorianCalendar today = new GregorianCalendar();
        String hour = today.get(GregorianCalendar.HOUR_OF_DAY) + "";
        return (hour.length() < 2 ? ("0" + hour) : hour) + ":" + today.get(GregorianCalendar.MINUTE) + ":"
                + today.get(GregorianCalendar.SECOND);
    }

    /**
     * Returns string like 2011-06-09 21:20:59
     */
    public static String getDateAndTime() {
        return AtlantisUtilities.getTodayAsString() + " " + AtlantisUtilities.getCurrentTimeAsString();
    }

    /**
     * Returns list of strings made by splitting string <b>string</b> wherever c occurrs
     */
    public static ArrayList<String> implodeList(String string, char c) {
        ArrayList<String> result = new ArrayList<String>();
        int pointer = 0;
        while (pointer < string.length()) {
            int indexOfChar = string.indexOf(c);
            if (indexOfChar > -1) {
                String cut = string.substring(0, indexOfChar);
                result.add(cut);
                string = string.substring(indexOfChar + 1);
                pointer = 0;
            } else {
                pointer++;
            }
        }
        result.add(string);
        return result;
    }

    /**
     * Returns list of strings made by merging strings in the list with char.
     */
    public static String explodeList(ArrayList<String> list, char character) {
        String result = "";
        for (String element : list) {
            result += element + character;
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * Returns random element of given list.
     */
    public static Object getRandomListElement(List<?> list) {
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Returns random element of given list.
     */
    public static Object getRandomElement(Collection<?> collection) {
        int indexToPick = random.nextInt(collection.size());
        int counter = 0;
        for (Object object : collection) {
            if (indexToPick == counter++) {
                return object;
            }
        }
        return null;
    }

    /**
     * Returns extension of given file e.g. "jpg", "txt".
     */
    public static String getFileExtension(File fileImage) {
        return fileImage.getName().substring(fileImage.getName().lastIndexOf('.') + 1);
    }

    /**
     * Returns just the file name, without extension part e.g. for file "images/horse.png" it will return
     * "horse".
     */
    public static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName().replace("/", "\\");

        if (fileName.lastIndexOf("\\") == -1) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName.substring(fileName.lastIndexOf("\\"), fileName.lastIndexOf("."));
        }
    }

    /**
     * Returns scaled image.
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Saves given string to file with path filePath.
     */
    public static PrintWriter saveToFile(String filePath, String stringToWrite, boolean closeTheStream) {
        try {
            File file = new File(filePath);
            file.createNewFile();
            PrintWriter out = new PrintWriter(file);
            out.print(stringToWrite);
            if (closeTheStream) {
                out.close();
            } else {
                return out;
            }
        } catch (Exception e) {
            AtlantisUtilities.displayException(e, "Błąd", "Błąd przy zapisywaniu do pliku\n" + "saveToFile(\"" + filePath
                    + "\", \"" + stringToWrite + "\")");
        }
        return null;
    }

    /**
     * @return number of all files (directory is not a file) in all these directory and all its
     * subdirectories.
     */
    public static int countNumberOfFiles(File directory) {
        int total = 0;
        if (!directory.exists()) {
            return 0;
        }
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                total++;
            } else {
                total += countNumberOfFiles(file);
            }
        }
        return total;
    }

    /**
     * @return number of all files (directory is not a file) in all these directory and all its
     * subdirectories, having given extension.
     * @see Rafaelles.getFileExtension
     */
    public static int countNumberOfFiles(File directory, String extension) {
        int total = 0;
        if (!directory.exists()) {
            return 0;
        }
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                if (getFileExtension(file).equals(extension)) {
                    total++;
                }
            } else {
                total += countNumberOfFiles(file);
            }
        }
        return total;
    }

    /**
     * Returns something like: 1d 3h 2m 53s
     */
    public static String convertSecondsToDisplayableFormat(int numberOfSeconds) {
        if (numberOfSeconds < 60) {
            return numberOfSeconds + "s";
        } else if (numberOfSeconds < 3600) {
            return numberOfSeconds / 60 + "m " + convertSecondsToDisplayableFormat(numberOfSeconds % 60);
        } else if (numberOfSeconds < 86400) {
            return numberOfSeconds / 3600 + "h " + convertSecondsToDisplayableFormat(numberOfSeconds % 3600);
        } else {
            return numberOfSeconds / 86400 + "d " + convertSecondsToDisplayableFormat(numberOfSeconds % 86400);
        }
    }

    /**
     * Displays report concerning total amount of time that some task has taken, also it displays average per
     * one "task object". Number of atomic tasks is <b>totalToProcess</b>. You need to pass timeStart
     * (System.getCurrentTimeInMillis()).
     */
    public static void displayTimeReport(long timeStart, int totalToProcess) {
        long timeEnd = System.currentTimeMillis();

        System.out.println();
        System.out.println(totalToProcess + " objects have been processed.");
        System.out.println("Processing took " + convertSecondsToDisplayableFormat((int) (timeEnd - timeStart) / 1000)
                + " seconds  (" + String.format("%.2f", (double) (timeEnd - timeStart) / (1000 * totalToProcess))
                + "s per file)");
        System.out.println("################################################");
        System.out.println();
    }

    /**
     * According to current objects processed and total to process it displays estimated time to finish all
     * tasks.
     */
    public static void displayETA(long timeStart, int alreadyProcessed, int totalToProcess) {
        double seconds = ((double) (System.currentTimeMillis() - timeStart) / (1000 * alreadyProcessed));
        String eta = AtlantisUtilities
                .convertSecondsToDisplayableFormat((int) ((totalToProcess - alreadyProcessed) * seconds));
        System.out.println("It took " + String.format("%.1f", seconds) + "s. " + alreadyProcessed * 100
                / totalToProcess + "% objects (" + alreadyProcessed + "/" + totalToProcess + ") ready. ETA: " + eta);
    }

    /**
     * Returns map containing number of occurences of each element in given collection.
     */
    public static TreeMap<String, Integer> getOccurenceMap(Collection<String> collection) {
        TreeMap<String, Integer> occurences = new TreeMap<String, Integer>();
        for (String string : collection) {
            if (occurences.containsKey(string)) {
                occurences.put(string, occurences.get(string) + 1);
            } else {
                occurences.put(string, 1);
            }
        }
        return occurences;
    }

    /**
     * Returns element occurring highest amount of times in given collection.
     */
    public static String getMostOccurringElement(Collection<String> collection) {
        TreeMap<String, Integer> occurences = getOccurenceMap(collection);
        int max = 0;
        for (Integer occurrences : occurences.values()) {
            if (occurrences > max) {
                max = occurrences;
            }
        }

        for (String key : occurences.keySet()) {
            if (occurences.get(key) == max) {
                return key;
            }
        }
        return null;
    }

    /**
     * Reads every line of given file into the array list.
     */
    public static ArrayList<String> readTextFileToList(String filePath) {
        ArrayList<String> resultList = new ArrayList<String>();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                resultList.add(scanner.nextLine());
            }

            scanner.close();
        } catch (Exception e) {
            AtlantisUtilities.displayException(e);
        }
        return resultList;
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
     *
     */
    public static int daysBetween(Calendar c1, Calendar c2) {
        return (int) Math.abs((c1.getTimeInMillis() - c2.getTimeInMillis()) / 3600000 / 24);
    }

    /**
     *
     */
    public static void display2DList(java.util.List<java.util.List<Object>> list) {
        System.out.println("### START OF LIST");
        for (java.util.List<Object> arrayList : list) {
            for (Object string : arrayList) {
                System.out.print(string + "/");
            }
            System.out.println();
        }
        System.out.println("### END OF LIST");
    }

    /**
     * Prints out all element of this list.
     */
    public static void displayList(Collection<?> list, String header, String footer, boolean useNewLines) {
        if (header == null) {
            header = "### START OF LIST";
        }
        if (footer == null) {
            footer = "### END OF LIST";
        }
        
        System.out.println(header);
        for (Object object : list) {
            System.out.print(object + "/");
            if (useNewLines) {
                System.out.println();
            }
        }
        if (!useNewLines) {
            System.out.println();
        }
        
        if (!"".equals(footer)) {
            System.out.println(footer);
        }
    }

    /**
     *
     */
    public static void displayArray(Object[][] array) {
        System.out.println("### START OF LIST");
        for (Object[] row : array) {
            for (Object string : row) {
                System.out.print(string + "/");
            }
            System.out.println();
        }
        System.out.println("### END OF LIST");
    }

    /**
     *
     */
    public static void displayArray(Object[] array) {
        System.out.println("### START OF LIST");
        for (Object value : array) {
            System.out.print(value + "/");
            System.out.println();
        }
        System.out.println("### END OF LIST");
    }

    /**
     *
     */
    public static void setColorOnHover(final JLabel label, final Color color) {
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Color defaultColor = label.getForeground();
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Global.underlineLabel(label);
                label.setForeground(color);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Global.deunderlineLabel(label);
                label.setForeground(defaultColor);
            }
        });
    }

    /**
     *
     */
    public static void setActionOnClick(JLabel label, final Callable<Object> callable) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    callable.call();
                } catch (Exception ex) {
                    AtlantisUtilities.displayException(ex);
                }
            }
        });
    }

    /**
     * Returns String value, according to the relative comparison of the provided value to the possible range
     * e.g. we can assign letters from A to Z for numbers 1-10.
	 *
     */
    public static String assignStringForValue(double value, double max, double min, String[] strings) {
        value -= min;
        max -= min;
        min = 0;

        int ranges = strings.length;
        int i;
        for (i = 1; i < ranges; i++) {
            if (value > (max * (ranges - i) / ranges)) {
                return strings[strings.length - i];
            } else {
            }
        }
        return strings[0];
    }

    /**
     *
     */
    public static String randomElement(String[] array) {
        return array[random.nextInt(array.length)];
    }

    /**
     *
     */
    public static Object randomElement(ArrayList<?> list) {
        return list.get(rand(0, list.size() - 1));
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

    /**
     * Returns median of given double list.
	 *
     */
    public static double median(Collection<Double> list) {
        return median(list, true);
    }

    /**
     * Returns median of given double list.
     *
     * @param mathematicMedian If true it will return normal median. If it is set to false and number of
     * elements is even the center (but lesser) element will be returned e.g. for [1 2 3 4] it would return 2.
	 *
     */
    public static double median(Collection<Double> list, boolean mathematicMedian) {
        if (list.isEmpty()) {
            AtlantisUtilities.displayMessage("List for computing a median is empty!");
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
        } else if (mathematicMedian) {
            return (sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2 + 1)) / 2;
        } else {
            return (sorted.get(sorted.size() / 2));
        }
    }

    /**
     * Returns index of option chosen according to the option weights. Higher the weight is, greater the
     * chance for the option to be chosen.
	 *
     */
    public static int chooseOptionRandomlyWithWeights(boolean areValuesNormalizedToOne, double... weights) {
        double[] normalized;

        if (!areValuesNormalizedToOne) {
            normalized = new double[weights.length];

            double total = 0;
            for (double value : weights) {
                total += value;
            }

            int counter = 0;
            for (double value : weights) {
                normalized[counter] = value / total;
                counter++;
            }
        } else {
            normalized = weights;
        }

        double randomValue = random.nextDouble();
        int index = 0;
        for (double weight : normalized) {
            if (randomValue < weight) {
                return index;
            } else {
                randomValue -= weight;
                index++;
            }
        }

        String log = "";
        for (double d : weights) {
            log += d + " / ";
        }
        displayError("chooseOptionRandomlyWithWeights:\n" + log);
        return -1;
    }

    /**
     *
     */
    public static HashMap<Object, Double> normalizeTo(Map<?, Double> map, int toValue) {
        HashMap<Object, Double> normalized = new HashMap<Object, Double>();

        double total = 0;
        for (Iterator<Double> it = map.values().iterator(); it.hasNext();) {
            double value = it.next();
            total += value;
        }

        for (Iterator<? extends Object> it = map.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            normalized.put(key, map.get(key) / total);
        }

        return normalized;
    }

    /**
     * Returns index-th element of the given set or null if there's no element "at" given index.
     *
     * @return
	 *
     */
    public static Object getSetElement(Set<?> set, int index) {
        int counter = 0;
        for (Object object : set) {
            if (counter++ == index) {
                return object;
            }
        }
        return null;
    }

    /**
     * Returns the first key from the given map or null if map is empty.
	 *
     */
    public static Object getFirstMapElement(Map<?, ?> map) {
        for (Object object : map.keySet()) {
            return object;
        }
        return null;
    }

    public static double getMaxElement(Collection<Double> collection) {
        double max = -9999999;
        for (double number : collection) {
            if (max < number) {
                max = number;
            }
        }
        return max;
    }

    /**
     * Loads .csv file or file formatted on csv base i.e. value1 delimiter value2 delimiter value3.
     */
    public static String[][] loadCsv(String path, int numberOfFields) {
        ArrayList<String[]> listOfArrays = new ArrayList<>();
        Scanner inputStream;
        try {
            inputStream = new Scanner(new File(path));

            while (inputStream.hasNextLine()) {
                String line = inputStream.nextLine();
                String[] fields = line.split(";");
                
                if (fields.length == 1 && line.contains(" - ")) {
                    fields = line.split(" - ");
                }

//                if (!line.isEmpty() && line.charAt(0) != '#' && !line.startsWith("//")) {
//                    if (numberOfFields > 0 && fields.length < numberOfFields && !line.contains(" - ")) {
//                        System.err.println("Invalid record in '" + path + "' CSV file: '" + line + "'");
//                        System.err.println("fields.length = " + fields.length);
//                        System.err.println("numberOfFields = " + numberOfFields);
//                        System.exit(-1);
//                    }
//                }

                if (!line.isEmpty() && !line.startsWith("//")) {
                    listOfArrays.add(fields);
                }
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error parsing CSV file: '" + path + "'");
            System.exit(-1);
            return null;
        }
        
        // =========================================================

        String[][] result = new String[listOfArrays.size()][numberOfFields];

        int counter = 0;
        for (String[] columns : listOfArrays) {
            result[counter] = columns;
            counter++;
        }

        return result;
    }
    
}
