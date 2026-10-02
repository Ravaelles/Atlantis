package atlantis.util;

import javax.swing.JOptionPane;

/**
 * Modal popups, extracted from the {@code A} god utility so that the game
 * logic stops carrying a GUI dependency (REVIEW §4, Stage H).
 *
 * <p>Only the reporting popups survive: the colour/panel/frame helpers that
 * used to live in {@code A} had zero callers (superseded by
 * {@code atlantis.util.CenterCamera} / {@code PauseAndCenter}) and were
 * deleted rather than moved.</p>
 *
 * <p>These are developer-facing dialogs; during a game run they are only
 * triggered by broken invariants, exactly as before.</p>
 */
public class AGui {

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
}
