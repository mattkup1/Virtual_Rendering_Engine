package gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the scene launcher/viewer GUI: a desktop front end that picks a scene
 * source file, renders it through the existing {@link renderer.Camera} pipeline, and
 * displays the result - without needing to write or run a JUnit test.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class RenderApp {
    /**
     * Private constructor - this is a launcher class, not meant to be instantiated.
     */
    private RenderApp() { /* to satisfy Javadoc generator */ }

    /**
     * Launches the GUI on the Swing event dispatch thread.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to Swing's default cross-platform look and feel.
        }
        SwingUtilities.invokeLater(() -> new RenderWindow().setVisible(true));
    }
}
