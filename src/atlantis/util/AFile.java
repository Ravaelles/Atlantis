package atlantis.util;

import atlantis.util.log.ErrorLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * File and directory access, extracted from {@code atlantis.game.A} so that the
 * god utility stops owning a second reason to change (REVIEW §4, Stage H).
 *
 * <p>Lives in {@code atlantis.util} so that it can serve the logging and
 * reporting classes in {@code atlantis.util.log} without making the shared
 * kernel depend on {@code atlantis.game} (ArchUnit rule
 * {@code utilMustNotDependOnHigherLayers}). Errors go to {@code System.err}
 * exactly as {@code A.errPrintln} did.</p>
 */
public class AFile {

    /**
     * Saves given string to file with path filePath.
     */
    public static PrintWriter saveToFile(String filePath, String stringToWrite, boolean closeTheStream) {
        try {
            File file = new File(filePath);
            PrintWriter out = new PrintWriter(file);
            out.print(stringToWrite);
            if (closeTheStream) {
                out.close();
            }
            else {
                return out;
            }
        } catch (Exception e) {
            System.err.println("Error while saving to file\n" + "Path(\"" + filePath
                + "\", \"" + stringToWrite + "\")");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean appendToFile(String filePath, String stringToWrite) {
        try {
            File file = new File(filePath);
            PrintWriter out = new PrintWriter(new FileOutputStream(file, true));
            out.print(stringToWrite);
            out.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error while appending to file\n" + "Path(\"" + filePath
                + "\", \"" + stringToWrite + "\")");
            e.printStackTrace();
        }
        return false;
    }

    public static void writeToFileWithHeader(String filePath, String content, String[] headers) {
        try {
            if (!fileExists(filePath)) {
                content = String.join(";", headers) + "\n" + content;
            }
            FileWriter fw = new FileWriter(filePath, true);
            fw.write(content + "\n");
            fw.close();
        } catch (IOException exception) {
            ErrorLog.printErrorOnce("IOException: " + exception.getMessage());
        }
    }

    /**
     * Reads every line of given file into the array list.
     */
    public static ArrayList<String> readTextFileToList(String filePath) {
        ArrayList<String> resultList = new ArrayList<>();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                resultList.add(scanner.nextLine());
            }

            scanner.close();
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * Loads .csv file or file formatted on csv base i.e. value1 delimiter value2 delimiter value3.
     *
     * @throws UncheckedIOException if the file cannot be read. A leaf utility
     *     must not decide policy: it reports the failure, the caller decides
     *     whether that means quitting the game (see
     *     {@code ABuildOrderLoader}) or degrading.
     */
    public static String[][] loadFile(String path, int numberOfFields, String delimiter) {
        if (delimiter == null) {
            delimiter = ";";
        }

        ArrayList<String[]> listOfArrays = new ArrayList<>();
        Scanner inputStream;
        try {
            inputStream = new Scanner(new File(path));

            while (inputStream.hasNextLine()) {
                String line = inputStream.nextLine();
                line = line.replace("—", "-"); // Replace em dashes with hyphens - omfg, that hurt

                String[] fields = line.split(delimiter);

                if (fields.length == 1 && line.contains(" - ")) {
                    fields = line.split(" - ");
                }

                if (!line.isEmpty() && !line.startsWith("//")) {
                    listOfArrays.add(fields);
                }
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            // Was System.exit(-1) here, which meant a file-reading helper could
            // kill the JVM on a path nothing else could handle or test.
            throw new UncheckedIOException("Error parsing CSV file: '" + path + "'", e);
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

    public static boolean fileExists(String file) {
        File f = new File(file);
        return f.exists() && !f.isDirectory();
    }

    public static boolean directoryExists(String file) {
        File f = new File(file);
        return f.exists() && f.isDirectory();
    }

    public static boolean createDirectory(String file) {
        File f = new File(file);
        return f.mkdirs();
    }

    public static void removeFile(String filePath) {
        if (fileExists(filePath)) {
            File file = new File(filePath);
            file.delete();
        }
    }

    public static String currentPath() {
        return (new File("")).getAbsolutePath();
    }

    public static long fileSize(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return -1;
        }

        try {
            long size = Files.size(file.toPath());
            return size > 0 ? size : fileContent(filename).length();
        } catch (IOException e) {
            System.err.println("Error getting file size: " + e.getMessage());
            return file.length();
        }
    }

    /**
     * Copies a file, refusing to overwrite an existing target. Returns whether
     * the copy happened.
     */
    public static boolean copy(String source, String target) {
        File sourceFile = new File(source);
        File targetFile = new File(target);

        if (!sourceFile.exists()) {
            return false;
        }

        if (targetFile.exists()) {
            return false;
        }

        // Copy file
        try {
            Files.copy(sourceFile.toPath(), targetFile.toPath());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to copy file from " + source + " to " + target);
        }
        return false;
    }

    /**
     * Reads the whole file as a string. Only used by {@link #fileSize(String)}
     * for the rare case where the reported size is zero.
     */
    private static String fileContent(String filename) {
        try {
            return new String(Files.readAllBytes(new File(filename).toPath()));
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            return "";
        }
    }
}
