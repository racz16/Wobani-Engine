package resources;

import components.camera.*;
import components.light.*;
import core.*;
import java.util.*;
import org.joml.*;
import renderers.*;
import resources.audio.*;
import resources.meshes.*;
import resources.splines.*;
import resources.textures.*;
import shaders.*;
import toolbox.annotations.*;
import window.*;

/**
 * Manages the loaded models, textures and splines.
 *
 */
public class ResourceManager {

    /**
     * Resource's state.
     */
    public enum ResourceState {
        /**
         * ACTION (means that the resource ready to use, stored like in VRAM or
         * in the sound system).
         */
        ACTION,
        /**
         * RAM.
         */
        RAM,
        /**
         * HDD.
         */
        HDD
    }

    /**
     * Contains all the textures.
     */
    private static final Map<String, Texture2D> textures = new HashMap<>();
    /**
     * Contains all the meshes.
     */
    private static final Map<String, Mesh> meshes = new HashMap<>();
    /**
     * Contains all the splines.
     */
    private static final Map<String, Spline> splines = new HashMap<>();
    /**
     * Contains all the FBOs.
     */
    private static final Map<String, Fbo> fbos = new HashMap<>();
    /**
     * Contains all the UBOs.
     */
    private static final Map<String, Ubo> ubos = new HashMap<>();
    /**
     * Contains all the VAOs.
     */
    private static final Map<String, Vao> vaos = new HashMap<>();
    /**
     * Contains all the sound programs.
     */
    private static final Map<String, Shader> shaders = new HashMap<>();
    /**
     * Contains all the audioBuffers.
     */
    private static final Map<String, AudioBuffer> audioBuffers = new HashMap<>();
    /**
     * Contains all the audioBuffers.
     */
    private static final Map<String, AudioSource> audioSources = new HashMap<>();
    /**
     * Resource's last update time (in miliseconds).
     */
    private static long lastUpdateTime = System.currentTimeMillis();
    /**
     * Resources' update time period (in miliseconds).
     */
    private static long resourceUpdatePeriod = 5000;
    /**
     * The next resource id.
     */
    private static int nextId = 1;

    /**
     * To can't initialize a new ResourceManager.
     */
    private ResourceManager() {
    }

    //
    //update--------------------------------------------------------------------
    //
    /**
     * Updates all the resources.
     */
    public static void updateResources() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > resourceUpdatePeriod) {
            updateResourceMap(meshes);
            updateResourceMap(textures);
            updateResourceMap(splines);
            updateResourceMap(fbos);
            updateResourceMap(ubos);
            updateResourceMap(vaos);
            updateResourceMap(shaders);
            updateResourceMap(audioBuffers);
            updateResourceMap(audioSources);

            lastUpdateTime = currentTime;
        }
    }

    /**
     * Updates the specified map of resources.
     *
     * @param resources map of resources
     * @param <T> resource type
     *
     * @throws NullPointerException map can't be null
     */
    private static <T extends Resource> void updateResourceMap(@NotNull Map<String, T> resources) {
        if (resources == null) {
            throw new NullPointerException();
        }
        String[] keys = new String[resources.keySet().size()];
        resources.keySet().toArray(keys);

        for (String key : keys) {
            Resource resource = resources.get(key);
            if (resource.isUsable()) {
                resource.update();
            } else {
                resources.remove(key);
            }
        }
    }

    /**
     * Returns the resources' update time period.
     *
     * @return the resources' update time period (in miliseconds)
     */
    public static long getResourceUpdatePeroid() {
        return resourceUpdatePeriod;
    }

    /**
     * Sets the resources' update time period to the given value.
     *
     * @param updatePeriod the resources' update time period
     *
     * @throws IllegalArgumentException update period can't be negative
     */
    public static void setResourceUpdatePeriod(long updatePeriod) {
        if (updatePeriod < 0) {
            throw new IllegalArgumentException("Update period can't be negative");
        }
        resourceUpdatePeriod = updatePeriod;
    }

    //
    //textures------------------------------------------------------------------
    //
    /**
     * Returns the specified texture.
     *
     * @param key texture's key
     * @return texture
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Texture2D getTexture(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return textures.get(key);
    }

    /**
     * Adds the given texture to the list of textures.
     *
     * @param key texture's key
     * @param texture texture
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addTexture(@NotNull String key, @NotNull Texture2D texture) {
        if (key == null || texture == null) {
            throw new NullPointerException();
        }
        if (!textures.containsKey(key)) {
            textures.put(key, texture);
        }
    }

    /**
     * Changes all texture's (that implements EasyFiltering) filtering based on
     * what you set in Settings.
     *
     * @see EasyFiltering
     * @see Settings#getTextureFiltering()
     */
    public static void changeTextureFiltering() {
        for (String key : textures.keySet()) {
            if (EasyFiltering.class.isInstance(textures.get(key))) {
                EasyFiltering texture = (EasyFiltering) textures.get(key);
                texture.bind();
                texture.setTextureFiltering(Settings.getTextureFiltering());
                texture.unbind();
            }
        }
    }

    /**
     * Changes all texture's (that implements ChangableColorSpace) color space
     * based on what you set in Settings.
     *
     * @see ChangableColorSpace
     * @see Settings#getGamma()
     */
    public static void changeTextureColorSpace() {
        boolean sRgb = Settings.getGamma() != 1;
        for (String key : textures.keySet()) {
            if (ChangableColorSpace.class.isInstance(textures.get(key))) {
                ChangableColorSpace texture = (ChangableColorSpace) textures.get(key);
                texture.setsRgb(sRgb);
            }
        }
    }

    /**
     * Returns data about the textures. The x coordinate means the number of
     * usable textures, the y means the data size in bytes, stored in the RAM,
     * the z means the data size in bytes, stored in the ACTION.
     *
     * @return data about the textures
     */
    @NotNull @ReadOnly
    public static Vector3i getTextureData() {
        int ram = 0;
        int vram = 0;
        int count = 0;
        for (Texture2D texture : textures.values()) {
            if (texture.isUsable()) {
                count++;
                ram += texture.getDataSizeInRam();
                vram += texture.getDataSizeInAction();
            }
        }
        return new Vector3i(count, ram, vram);
    }

    //
    //meshes--------------------------------------------------------------------
    //
    /**
     * Returns the specified mesh.
     *
     * @param key mesh's key
     * @return mesh
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Mesh getMesh(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return meshes.get(key);
    }

    /**
     * Adds the given mesh to the list of meshes.
     *
     * @param key mesh's key
     * @param mesh mesh
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addMesh(@NotNull String key, @NotNull Mesh mesh) {
        if (key == null || mesh == null) {
            throw new NullPointerException();
        }
        if (!meshes.containsKey(key)) {
            meshes.put(key, mesh);
        }
    }

    /**
     * Returns data about the meshes. The x coordinate means the number of
     * usable meshes, the y means the data size in bytes, stored in the RAM, the
     * z means the data size in bytes, stored in the ACTION.
     *
     * @return data about the meshes
     */
    @NotNull @ReadOnly
    public static Vector3i getMeshData() {
        int ram = 0;
        int vram = 0;
        int count = 0;
        for (Mesh mesh : meshes.values()) {
            if (mesh.isUsable()) {
                count++;
                ram += mesh.getDataSizeInRam();
                vram += mesh.getDataSizeInAction();
            }
        }
        return new Vector3i(count, ram, vram);
    }

    //
    //splines-------------------------------------------------------------------
    //
    /**
     * Returns the specified spline.
     *
     * @param key spline's key
     * @return spline
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Spline getSpline(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return splines.get(key);
    }

    /**
     * Adds the given spline to the list of spline.
     *
     * @param key spline's key
     * @param spline spline
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addSpline(@NotNull String key, @NotNull Spline spline) {
        if (key == null || spline == null) {
            throw new NullPointerException();
        }
        if (!splines.containsKey(key)) {
            splines.put(key, spline);
        }
    }

    /**
     * Returns data about the splines. The x coordinate means the number of
     * usable splines, the y means the data size in bytes, stored in the RAM,
     * the z means the data size in bytes, stored in the ACTION.
     *
     * @return data about the splines
     */
    @NotNull @ReadOnly
    public static Vector3i getSplineData() {
        int ram = 0;
        int vram = 0;
        int count = 0;
        for (Spline spline : splines.values()) {
            if (spline.isUsable()) {
                count++;
                ram += spline.getDataSizeInRam();
                vram += spline.getDataSizeInAction();
            }
        }
        return new Vector3i(count, ram, vram);
    }

    //
    //FBOs----------------------------------------------------------------------
    //
    /**
     * Returns the specified FBO.
     *
     * @param key FBO's key
     * @return FBO
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Fbo getFbo(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return fbos.get(key);
    }

    /**
     * Adds the given FBO to the list of FBOs.
     *
     * @param key FBO's key
     * @param fbo FBO
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addFbo(@NotNull String key, @NotNull Fbo fbo) {
        if (key == null || fbo == null) {
            throw new NullPointerException();
        }
        if (!fbos.containsKey(key)) {
            fbos.put(key, fbo);
        }
    }

    /**
     * Returns data about the FBOs. The x coordinate means the number of usable
     * FBOs, the y means the data size in bytes, stored in the RAM, the z means
     * the data size in bytes, stored in the ACTION.
     *
     * @return data about the FBOs
     */
    @NotNull @ReadOnly
    public static Vector3i getFboData() {
        int count = 0;
        for (Fbo fbo : fbos.values()) {
            if (fbo.isUsable()) {
                count++;
            }
        }
        return new Vector3i(count, 0, 0);
    }

    //
    //UBOs----------------------------------------------------------------------
    //
    /**
     * Returns the specified UBO.
     *
     * @param key UBO's key
     * @return UBO
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Ubo getUbo(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return ubos.get(key);
    }

    /**
     * Adds the given UBO to the list of UBOs.
     *
     * @param key UBO's key
     * @param ubo UBO
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addUbo(@NotNull String key, @NotNull Ubo ubo) {
        if (key == null || ubo == null) {
            throw new NullPointerException();
        }
        if (!ubos.containsKey(key)) {
            ubos.put(key, ubo);
        }
    }

    /**
     * Returns data about the UBOs. The x coordinate means the number of usable
     * UBOs, the y means the data size in bytes, stored in the RAM, the z means
     * the data size in bytes, stored in the ACTION.
     *
     * @return data about the UBOs
     */
    @NotNull @ReadOnly
    public static Vector3i getUboData() {
        int ram = 0;
        int vram = 0;
        int count = 0;
        for (Ubo ubo : ubos.values()) {
            if (ubo.isUsable()) {
                count++;
                ram += ubo.getDataSizeInRam();
                vram += ubo.getDataSizeInAction();
            }
        }
        return new Vector3i(count, ram, vram);
    }

    //
    //VAOs----------------------------------------------------------------------
    //
    /**
     * Returns the specified VAO.
     *
     * @param key VAO's key
     * @return VAO
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Vao getVao(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return vaos.get(key);
    }

    /**
     * Adds the given VAO to the list of VAOs.
     *
     * @param key VAO's key
     * @param vao VAO
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addVao(@NotNull String key, @NotNull Vao vao) {
        if (key == null || vao == null) {
            throw new NullPointerException();
        }
        if (!vaos.containsKey(key)) {
            vaos.put(key, vao);
        }
    }

    /**
     * Returns data about the VAOs. The x coordinate means the number of usable
     * VAOs, the y means the data size in bytes, stored in the RAM, the z means
     * the data size in bytes, stored in the ACTION.
     *
     * @return data about the VAOs
     */
    @NotNull @ReadOnly
    public static Vector3i getVaoData() {
        int count = 0;
        for (Vao vao : vaos.values()) {
            if (vao.isUsable()) {
                count++;
            }
        }
        return new Vector3i(count, 0, 0);
    }

    //
    //shaders----------------------------------------------------------------------
    //
    /**
     * Returns the specified sound.
     *
     * @param key sound's key
     * @return sound
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static Shader getShader(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return shaders.get(key);
    }

    /**
     * Adds the given sound to the list of shaders.
     *
     * @param key sound's key
     * @param shader sound
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addShader(@NotNull String key, @NotNull Shader shader) {
        if (key == null || shader == null) {
            throw new NullPointerException();
        }
        if (!shaders.containsKey(key)) {
            shaders.put(key, shader);
        }
    }

    /**
     * Returns data about the shaders. The x coordinate means the number of
     * usable shaders, the y means the data size in bytes, stored in the RAM,
     * the z means the data size in bytes, stored in the ACTION.
     *
     * @return data about the shaders
     */
    @NotNull @ReadOnly
    public static Vector3i getShaderData() {
        int count = 0;
        for (Shader shader : shaders.values()) {
            if (shader.isUsable()) {
                count++;
            }
        }
        return new Vector3i(count, 0, 0);
    }

    //
    //audio buffers-------------------------------------------------------------
    //
    /**
     * Returns the specified audio buffer.
     *
     * @param key audio buffer's key
     * @return audio buffer
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static AudioBuffer getAudioBuffer(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return audioBuffers.get(key);
    }

    /**
     * Adds the given audio buffer to the list of audio buffers.
     *
     * @param key audio buffer's key
     * @param sound audio buffer
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addAudioBuffer(@NotNull String key, @NotNull AudioBuffer sound) {
        if (key == null || sound == null) {
            throw new NullPointerException();
        }
        if (!audioBuffers.containsKey(key)) {
            audioBuffers.put(key, sound);
        }
    }

    /**
     * Returns data about the audio buffers. The x coordinate means the number
     * of usable audio buffers, the y means the data size in bytes, stored in
     * the RAM, the z means the data size in bytes, stored in the ACTION.
     *
     * @return data about the audio buffers
     */
    @NotNull @ReadOnly
    public static Vector3i getAudioBufferData() {
        int ram = 0;
        int vram = 0;
        int count = 0;
        for (AudioBuffer sound : audioBuffers.values()) {
            if (sound.isUsable()) {
                count++;
                ram += sound.getDataSizeInRam();
                vram += sound.getDataSizeInAction();
            }
        }
        return new Vector3i(count, ram, vram);
    }

    //
    //audio buffers-------------------------------------------------------------
    //
    /**
     * Returns the specified audio source.
     *
     * @param key audio source's key
     * @return audio source
     *
     * @throws NullPointerException key can't be null
     */
    @Nullable
    public static AudioSource getAudioSource(@NotNull String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return audioSources.get(key);
    }

    /**
     * Adds the given audio source to the list of audio sources.
     *
     * @param key audio source's key
     * @param source audio source
     *
     * @throws NullPointerException arguments can't be null
     */
    public static void addAudioSource(@NotNull String key, @NotNull AudioSource source) {
        if (key == null || source == null) {
            throw new NullPointerException();
        }
        if (!audioSources.containsKey(key)) {
            audioSources.put(key, source);
        }
    }

    /**
     * Returns data about the audio sources. The x coordinate means the number
     * of usable audio sources, the y means the data size in bytes, stored in
     * the RAM, the z means the data size in bytes, stored in the ACTION.
     *
     * @return data about the audio sources
     */
    @NotNull @ReadOnly
    public static Vector3i getAudioSourceData() {
        int count = 0;
        for (AudioSource sound : audioSources.values()) {
            if (sound.isUsable()) {
                count++;
            }
        }
        return new Vector3i(count, 0, 0);
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Returns the next resource id.
     *
     * @return the next resource id
     */
    public static int getNextId() {
        return nextId++;
    }

    /**
     * Releases the textures, meshes, splines, FBOs and the window.
     */
    public static void releaseResources() {
        releaseResourceMap(meshes);
        releaseResourceMap(textures);
        releaseResourceMap(splines);
        releaseResourceMap(fbos);
        releaseResourceMap(ubos);
        releaseResourceMap(vaos);
        releaseResourceMap(shaders);
        releaseResourceMap(audioBuffers);
        releaseResourceMap(audioSources);
        DefaultLightComponent.releaseUbo();
        CameraComponent.releaseUbo();
        RenderingPipeline.release();
        Input.release();
        Window.release();
    }

    /**
     * Releases the specified map of resources.
     *
     * @param resources map of resources
     * @param <T> resource type
     *
     * @throws NullPointerException the map can't be null
     */
    private static <T extends Resource> void releaseResourceMap(@NotNull Map<String, T> resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        String[] keys = new String[resources.keySet().size()];
        resources.keySet().toArray(keys);

        for (String key : keys) {
            Resource resource = resources.get(key);
            if (resource.isUsable()) {
                resource.release();
            }
            resources.remove(key);
        }
    }

}
