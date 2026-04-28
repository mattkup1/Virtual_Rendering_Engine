package scene;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Concrete implementation of {@link SceneLoader} for JSON source files.
 * <p>
 * This class handles the extraction of scene data from a JSON structure,
 * mapping JSON keys and objects into a format the base loader understands.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class JsonSceneLoader extends SceneLoader {

    /**
     * The root JSON object containing all scene data
     */
    private final JSONObject root;

    /**
     * Constructs a JSON scene loader and parses the file into a JSON object.
     *
     * @param sceneName the name of the scene to be created
     * @param jsonName  the path to the JSON file
     * @throws RuntimeException if the file cannot be read or parsed
     */
    public JsonSceneLoader(String sceneName, String jsonName) {
        super(sceneName);
        try {
            String content = Files.readString(Paths.get(jsonName));
            this.root = new JSONObject(content);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize JSON from: " + jsonName, e);
        }
    }

    @Override
    protected String getBackgroundColor() {
        // Returns the string value of "background-color" or null if missing
        return root.optString("background-color", null);
    }

    @Override
    protected String getAmbientLight() {
        // Navigates to the "ambient-light" object and extracts the "color" string
        return root.has("ambient-light") ?
                root.getJSONObject("ambient-light").optString("color", null) : null;
    }

    @Override
    protected List<Map<String, String>> getGeometries() {
        List<Map<String, String>> list = new ArrayList<>();
        JSONArray geometries = root.optJSONArray("geometries");

        if (geometries != null) {
            for (int i = 0; i < geometries.length(); i++) {
                JSONObject obj = geometries.getJSONObject(i);
                Map<String, String> map = new HashMap<>();

                // Map every key-value pair in the JSON object to the string-based attribute map
                for (String key : obj.keySet()) {
                    map.put(key, String.valueOf(obj.get(key)));
                }
                list.add(map);
            }
        }
        return list;
    }
}