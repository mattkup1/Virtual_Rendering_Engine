package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import renderer.Camera;
import renderer.RayTracerType;
import scene.JsonSceneLoader;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * The scene launcher/viewer window: lets the user pick a JSON/XML scene file, configure
 * render settings, render it through the existing {@link Camera} pipeline on a background
 * thread, watch it fill in live, and save the result under {@code images/}.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
final class RenderWindow extends JFrame {
    /**
     * Directories scanned for scene source files to populate the scene picker.
     */
    private static final String[] SCENE_DIRS = {"sceneSourceFiles/json", "sceneSourceFiles/xml"};

    /**
     * How often, in milliseconds, the in-progress render preview is refreshed.
     */
    private static final int PREVIEW_REFRESH_MS = 250;

    /**
     * Picker for the scene source file to render.
     */
    private final JComboBox<File> _sceneCombo = new JComboBox<>();

    /**
     * Enables the resolution spinners to override the scene file's own resolution.
     */
    private final JCheckBox _overrideResolution = new JCheckBox("Override resolution");

    /**
     * Output image width, in pixels, used only when {@link #_overrideResolution} is checked.
     */
    private final JSpinner _resX = new JSpinner(new SpinnerNumberModel(800, 1, 4000, 50));

    /**
     * Output image height, in pixels, used only when {@link #_overrideResolution} is checked.
     */
    private final JSpinner _resY = new JSpinner(new SpinnerNumberModel(800, 1, 4000, 50));

    /**
     * Anti-aliasing samples per pixel axis; {@code 1} disables anti-aliasing.
     */
    private final JSpinner _antiAliasing = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));

    /**
     * Rendering thread strategy, mapped to a {@link Camera.Builder#setMultithreading(int)}
     * value by {@link #selectedThreadCount()}.
     */
    private final JComboBox<String> _threading =
            new JComboBox<>(new String[]{"Auto (recommended)", "Off (single-threaded)", "2", "4", "8"});

    /**
     * Starts a render of the selected scene.
     */
    private final JButton _renderButton = new JButton("Render");

    /**
     * Saves the last rendered image under {@code images/}; disabled until a render succeeds.
     */
    private final JButton _saveButton = new JButton("Save…");

    /**
     * Displays the rendered (or in-progress) image.
     */
    private final JLabel _imageLabel = new JLabel("Pick a scene and click Render", JLabel.CENTER);

    /**
     * Reports render status: idle, elapsed time while rendering, or the final result.
     */
    private final JLabel _statusLabel = new JLabel(" ");

    /**
     * Busy indicator shown while a render is in progress.
     */
    private final JProgressBar _progressBar = new JProgressBar();

    /**
     * Polls the in-progress camera's image buffer to refresh the preview while rendering.
     */
    private final Timer _previewTimer = new Timer(PREVIEW_REFRESH_MS, e -> refreshPreview());

    /**
     * The currently running (or most recently finished) render task, or {@code null} before
     * the first render.
     */
    private RenderTask _task;

    /**
     * Wall-clock time the current render started, for the elapsed-time display.
     */
    private long _renderStartMillis;

    /**
     * Builds and lays out the window. Does not make it visible.
     */
    RenderWindow() {
        super("ISE Ray Tracer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildControlPanel(), BorderLayout.WEST);
        add(buildImagePanel(), BorderLayout.CENTER);
        add(buildStatusPanel(), BorderLayout.SOUTH);

        _renderButton.addActionListener(e -> onRender());
        _saveButton.addActionListener(e -> onSave());
        _saveButton.setEnabled(false);

        populateSceneCombo();

        setPreferredSize(new Dimension(1200, 900));
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Builds the left-hand panel of scene and render-option controls.
     *
     * @return the control panel
     */
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        panel.add(new JLabel("Scene file"), c);
        c.gridy++;
        _sceneCombo.setRenderer(new SceneFileRenderer());
        panel.add(_sceneCombo, c);

        c.gridy++;
        JButton browse = new JButton("Browse…");
        browse.addActionListener(e -> onBrowse());
        panel.add(browse, c);

        c.gridy++;
        c.insets = new Insets(16, 4, 4, 4);
        panel.add(_overrideResolution, c);
        c.insets = new Insets(4, 4, 4, 4);

        c.gridwidth = 1;
        c.gridy++;
        panel.add(new JLabel("Width"), c);
        c.gridx = 1;
        panel.add(_resX, c);
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Height"), c);
        c.gridx = 1;
        panel.add(_resY, c);

        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Anti-aliasing"), c);
        c.gridx = 1;
        panel.add(_antiAliasing, c);

        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Threads"), c);
        c.gridx = 1;
        panel.add(_threading, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(20, 4, 4, 4);
        panel.add(_renderButton, c);

        c.gridy++;
        c.insets = new Insets(4, 4, 4, 4);
        panel.add(_saveButton, c);

        // Absorb any remaining vertical space so the controls stay top-aligned.
        c.gridy++;
        c.weighty = 1;
        panel.add(new JPanel(), c);

        return panel;
    }

    /**
     * Builds the center panel showing the rendered/in-progress image.
     *
     * @return the image panel
     */
    private JScrollPane buildImagePanel() {
        _imageLabel.setVerticalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(_imageLabel);
        scrollPane.setPreferredSize(new Dimension(900, 900));
        return scrollPane;
    }

    /**
     * Builds the bottom status/progress bar.
     *
     * @return the status panel
     */
    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        panel.add(_statusLabel, BorderLayout.CENTER);
        _progressBar.setIndeterminate(false);
        _progressBar.setPreferredSize(new Dimension(150, _progressBar.getPreferredSize().height));
        panel.add(_progressBar, BorderLayout.EAST);
        return panel;
    }

    /**
     * Scans {@link #SCENE_DIRS} for {@code .json}/{@code .xml} files and fills the scene
     * picker with the results, sorted by file name.
     */
    private void populateSceneCombo() {
        for (File file : findSceneFiles()) {
            _sceneCombo.addItem(file);
        }
    }

    /**
     * Finds all scene source files under {@link #SCENE_DIRS}.
     *
     * @return the found files, sorted by name
     */
    private static List<File> findSceneFiles() {
        List<File> files = new ArrayList<>();
        for (String dir : SCENE_DIRS) {
            File[] found = new File(dir)
                    .listFiles((d, name) -> name.endsWith(".json") || name.endsWith(".xml"));
            if (found != null) {
                files.addAll(Arrays.asList(found));
            }
        }
        files.sort(Comparator.comparing(File::getName));
        return files;
    }

    /**
     * Opens a file chooser rooted at {@code sceneSourceFiles} so the user can pick a scene
     * file outside {@link #SCENE_DIRS}, and adds it to the picker.
     */
    private void onBrowse() {
        JFileChooser chooser = new JFileChooser(new File("sceneSourceFiles"));
        chooser.setFileFilter(new FileNameExtensionFilter("Scene files (*.json, *.xml)", "json", "xml"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            _sceneCombo.addItem(chosen);
            _sceneCombo.setSelectedItem(chosen);
        }
    }

    /**
     * Returns the currently selected scene file.
     *
     * @return the selected file, or {@code null} if none is selected
     */
    private File getSelectedSceneFile() {
        Object item = _sceneCombo.getSelectedItem();
        return item instanceof File file ? file : null;
    }

    /**
     * Maps the {@link #_threading} selection to a {@link Camera.Builder#setMultithreading(int)}
     * value.
     *
     * @return the thread count, in the convention {@code setMultithreading} expects
     */
    private int selectedThreadCount() {
        return switch ((String) _threading.getSelectedItem()) {
            case "Off (single-threaded)" -> 0;
            case "2" -> 2;
            case "4" -> 4;
            case "8" -> 8;
            default -> -1;
        };
    }

    /**
     * Validates the current selection and starts a background render.
     */
    private void onRender() {
        File sceneFile = getSelectedSceneFile();
        if (sceneFile == null) {
            JOptionPane.showMessageDialog(this, "Choose a scene file first.",
                    "No scene selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setControlsEnabled(false);
        _saveButton.setEnabled(false);
        _imageLabel.setIcon(null);
        _imageLabel.setText("Rendering…");
        _statusLabel.setText("Rendering… 0s elapsed");
        _progressBar.setIndeterminate(true);
        _renderStartMillis = System.currentTimeMillis();

        _task = new RenderTask(sceneFile, _overrideResolution.isSelected(),
                (Integer) _resX.getValue(), (Integer) _resY.getValue(),
                (Integer) _antiAliasing.getValue(), selectedThreadCount());
        _task.addPropertyChangeListener(e -> {
            if ("state".equals(e.getPropertyName()) && SwingWorker.StateValue.DONE == e.getNewValue()) {
                onRenderFinished();
            }
        });

        _previewTimer.start();
        _task.execute();
    }

    /**
     * Refreshes the image preview and elapsed-time label from the running task's camera,
     * once it has progressed far enough to exist. Called on the {@link #_previewTimer} tick.
     */
    private void refreshPreview() {
        Camera camera = _task == null ? null : _task.getCamera();
        if (camera != null) {
            _imageLabel.setText(null);
            _imageLabel.setIcon(new ImageIcon(camera.getImage()));
        }
        long elapsedSeconds = (System.currentTimeMillis() - _renderStartMillis) / 1000;
        _statusLabel.setText("Rendering… " + elapsedSeconds + "s elapsed");
    }

    /**
     * Called once the background render task finishes (successfully or not): stops the
     * preview timer, re-enables controls, and shows the final image or an error.
     */
    private void onRenderFinished() {
        _previewTimer.stop();
        _progressBar.setIndeterminate(false);
        setControlsEnabled(true);

        long elapsedSeconds = (System.currentTimeMillis() - _renderStartMillis) / 1000;
        try {
            _task.get();
            BufferedImage image = _task.getCamera().getImage();
            _imageLabel.setText(null);
            _imageLabel.setIcon(new ImageIcon(image));
            _statusLabel.setText("Done in " + elapsedSeconds + "s - " + image.getWidth() + "x" + image.getHeight());
            _saveButton.setEnabled(true);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            _statusLabel.setText("Render failed after " + elapsedSeconds + "s");
            _imageLabel.setIcon(null);
            _imageLabel.setText("Render failed");
            JOptionPane.showMessageDialog(this, cause.getMessage(), "Render failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prompts for an output name and saves the last rendered image under {@code images/},
     * via the same {@link Camera#writeToImage(String)} path the rest of the project uses.
     */
    private void onSave() {
        Camera camera = _task == null ? null : _task.getCamera();
        if (camera == null) {
            return;
        }

        String suggested = stripExtension(getSelectedSceneFile().getName());
        String name = JOptionPane.showInputDialog(this,
                "Save as (under images/, no extension):", suggested);
        if (name == null || name.isBlank()) {
            return;
        }

        camera.writeToImage(name);
        JOptionPane.showMessageDialog(this, "Saved to images/" + name + ".png",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Enables or disables all render-configuration controls, e.g. while a render is running.
     *
     * @param enabled whether the controls should be enabled
     */
    private void setControlsEnabled(boolean enabled) {
        _sceneCombo.setEnabled(enabled);
        _overrideResolution.setEnabled(enabled);
        _resX.setEnabled(enabled);
        _resY.setEnabled(enabled);
        _antiAliasing.setEnabled(enabled);
        _threading.setEnabled(enabled);
        _renderButton.setEnabled(enabled);
    }

    /**
     * Strips the extension from a file name, for use as a default save name.
     *
     * @param fileName the file name
     * @return the file name without its extension
     */
    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? fileName : fileName.substring(0, dot);
    }

    /**
     * Loads a {@link Scene} from a {@code .json} or {@code .xml} scene source file.
     *
     * @param file the scene source file
     * @return the loaded scene
     */
    private static Scene loadScene(File file) {
        String path = file.getPath();
        String name = stripExtension(file.getName());
        if (path.endsWith(".json")) {
            return new JsonSceneLoader(name, path).loadScene();
        }
        if (path.endsWith(".xml")) {
            return new XmlSceneLoader(name, path).loadScene();
        }
        throw new IllegalArgumentException("Unsupported scene file type: " + path);
    }

    /**
     * Renders a {@link File} entry in {@link #_sceneCombo} as {@code <dir>/<name>}, so that
     * same-named JSON and XML files remain distinguishable.
     */
    private static final class SceneFileRenderer extends DefaultListCellRenderer {
        /**
         * Default constructor to satisfy Javadoc generator
         */
        SceneFileRenderer() { /* to satisfy Javadoc generator */ }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            String text = value instanceof File file
                    ? file.getParentFile().getName() + "/" + file.getName()
                    : String.valueOf(value);
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }

    /**
     * Background task that loads a scene, builds a {@link Camera} from it, and renders it.
     * <p>
     * The built camera is published to {@link #getCamera()} as soon as it exists (before the
     * potentially long {@link Camera#renderImage()} call), so the window's preview timer can
     * poll {@link Camera#getImage()} to show the render filling in live.
     * </p>
     */
    private static final class RenderTask extends SwingWorker<Void, Void> {
        /**
         * The scene source file to render.
         */
        private final File _sceneFile;
        /**
         * Whether {@link #_resX}/{@link #_resY} should override the scene's own resolution.
         */
        private final boolean _overrideResolution;
        /**
         * Output width, in pixels; used only when {@link #_overrideResolution} is set.
         */
        private final int _resX;
        /**
         * Output height, in pixels; used only when {@link #_overrideResolution} is set.
         */
        private final int _resY;
        /**
         * Anti-aliasing samples per pixel axis.
         */
        private final int _antiAliasing;
        /**
         * Thread count, in the convention {@link Camera.Builder#setMultithreading(int)} expects.
         */
        private final int _threadCount;
        /**
         * The camera under construction/render, visible to the UI thread as soon as it is
         * built so the preview timer can poll its image buffer.
         */
        private volatile Camera _camera;

        /**
         * Constructs a render task with the given scene file and render settings.
         *
         * @param sceneFile          the scene source file to render
         * @param overrideResolution whether to override the scene's own resolution
         * @param resX               output width, in pixels, if overriding
         * @param resY               output height, in pixels, if overriding
         * @param antiAliasing       anti-aliasing samples per pixel axis
         * @param threadCount        thread count, per {@link Camera.Builder#setMultithreading(int)}
         */
        RenderTask(File sceneFile, boolean overrideResolution, int resX, int resY,
                   int antiAliasing, int threadCount) {
            _sceneFile = sceneFile;
            _overrideResolution = overrideResolution;
            _resX = resX;
            _resY = resY;
            _antiAliasing = antiAliasing;
            _threadCount = threadCount;
        }

        /**
         * Returns the camera being (or having been) rendered.
         *
         * @return the camera, or {@code null} before it has been built
         */
        Camera getCamera() {
            return _camera;
        }

        @Override
        protected Void doInBackground() {
            Scene scene = loadScene(_sceneFile);
            if (scene.cameraSettings == null) {
                throw new IllegalStateException(
                        "\"" + _sceneFile.getName() + "\" has no camera settings in the scene file; "
                                + "pick a different scene.");
            }

            Camera.Builder builder = Camera.getBuilder()
                    .loadFrom(scene.cameraSettings)
                    .setRayTracer(scene, RayTracerType.SIMPLE)
                    .setAntiAliasing(_antiAliasing)
                    .setMultithreading(_threadCount);

            if (_overrideResolution) {
                builder.setResolution(_resX, _resY);
            }

            Camera camera = builder.build();
            _camera = camera;
            camera.renderImage();
            return null;
        }
    }
}
