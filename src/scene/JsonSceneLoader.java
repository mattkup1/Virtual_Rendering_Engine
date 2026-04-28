package scene;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;


public class JsonSceneLoader extends SceneLoader {

    private final JSONObject root;

    public JsonSceneLoader(String sceneName, String jsonName) {
        super(sceneName);
        try {
            String content = Files.readString(Paths.get(jsonName));
            this.root = new JSONObject(content);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize JSON", e);
        }
    }

    @Override
    protected String getBackgroundColor() {
        return root.optString("background-color", null);
    }

    @Override
    protected String getAmbientLight() {
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
                for (String key : obj.keySet()) {
                    map.put(key, String.valueOf(obj.get(key)));
                }
                list.add(map);
            }
        }
        return list;
    }
}