package bytejam.project.turbo.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import bytejam.project.renderer.Shader;
import bytejam.project.renderer.Texture;

/* =======================================================================
 *  This function exists to manage all of our recources as well as 
 *  making sure that Java does not ever delete them. Doing this reduces 
 *  large lag spikes by making sure every resource is loaded and ready.
 *///=====================================================================
public class AssetPool {
    private static final Map<String, Shader> shaders = new HashMap<>();
    private static final Map<String, Texture> textures = new HashMap<>();

    public static Shader getShader(String resourceName) {
        File file = new File(resourceName);
        if (AssetPool.shaders.containsKey(file.getAbsolutePath())) {
            return shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(resourceName);
            shader.compile();
            AssetPool.shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    public static Texture getTexture(String resourceName) {
        File file = new File(resourceName);
        if(AssetPool.textures.containsKey(file.getAbsolutePath())) {
            return AssetPool.textures.get(file.getAbsolutePath());
        } else {
            Texture texture = new Texture(resourceName);
            AssetPool.textures.put(file.getAbsolutePath(), texture);
            return texture; 
        }
    }
}
