package resources;

import components.camera.*;
import components.light.*;
import core.*;
import java.util.*;
import org.joml.*;
import renderers.*;
import resources.audio.*;
import resources.meshes.*;
import resources.shaders.*;
import resources.splines.*;
import resources.textures.*;
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
    private static final Map<ResourceId, Texture> textures = new HashMap<>();
    /**
     * Contains all the meshes.
     */
    private static final Map<ResourceId, Mesh> meshes = new HashMap<>();
    /**
     * Contains all the splines.
     */
    private static final Map<ResourceId, Spline> splines = new HashMap<>();
    /**
     * Contains all the FBOs.
     */
    private static final Map<ResourceId, Fbo> fbos = new HashMap<>();
    /**
     * Contains all the UBOs.
     */
    private static final Map<ResourceId, Ubo> ubos = new HashMap<>();
    /**
     * Contains all the VAOs.
     */
    private static final Map<ResourceId, Vao> vaos = new HashMap<>();
    /**
     * Contains all the sound programs.
     */
    private static final Map<ResourceId, Shader> shaders = new HashMap<>();
    /**
     * Contains all the audioBuffers.
     */
    private static final Map<ResourceId, AudioBuffer> audioBuffers = new HashMap<>();
    /**
     * Contains all the audioBuffers.
     */
    private static final Map<ResourceId, AudioSource> audioSources = new HashMap<>();
    /**
     * Resource's last update time (in miliseconds).
     */
    private static long lastUpdateTime = System.currentTimeMillis();
    /**
     * Resources' update time period (in miliseconds).
     */
    private static long resourceUpdatePeriod = 5000;

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
    private static <T extends Resource> void updateResourceMap(@NotNull Map<ResourceId, T> resources) {
        if (resources == null) {
            throw new NullPointerException();
        }
        ResourceId[] keys = new ResourceId[resources.keySet().size()];
        resources.keySet().toArray(keys);

        for (ResourceId key : keys) {
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
     */
    @Nullable
    public static Texture getTexture(@Nullable ResourceId key) {
        return textures.get(key);
    }

    /**
     * Adds the given texture to the list of textures.
     *
     * @param texture texture
     */
    public static void addTexture(@NotNull Texture texture) {
        if (!textures.containsKey(texture.getResourceId())) {
            textures.put(texture.getResourceId(), texture);
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
        for (ResourceId key : textures.keySet()) {
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
        for (ResourceId key : textures.keySet()) {
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
        for (Texture texture : textures.values()) {
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
     */
    @Nullable
    public static Mesh getMesh(@Nullable ResourceId key) {
        return meshes.get(key);
    }

    /**
     * Adds the given mesh to the list of meshes.
     *
     * @param mesh mesh
     */
    public static void addMesh(@NotNull Mesh mesh) {
        if (!meshes.containsKey(mesh.getResourceId())) {
            meshes.put(mesh.getResourceId(), mesh);
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
     */
    @Nullable
    public static Spline getSpline(@Nullable ResourceId key) {
        return splines.get(key);
    }

    /**
     * Adds the given spline to the list of spline.
     *
     * @param spline spline
     *
     */
    public static void addSpline(@NotNull Spline spline) {
        if (!splines.containsKey(spline.getResourceId())) {
            splines.put(spline.getResourceId(), spline);
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
     */
    @Nullable
    public static Fbo getFbo(@Nullable ResourceId key) {
        return fbos.get(key);
    }

    /**
     * Adds the given FBO to the list of FBOs.
     *
     * @param fbo FBO
     */
    public static void addFbo(@NotNull Fbo fbo) {
        if (!fbos.containsKey(fbo.getResourceId())) {
            fbos.put(fbo.getResourceId(), fbo);
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

    /**
     * Returns data about the RBOs. The x coordinate means the number of usable
     * RBOs, the y means the data size in bytes, stored in the RAM, the z means
     * the data size in bytes, stored in the ACTION.
     *
     * @return data about the RBOs
     */
    @NotNull @ReadOnly
    public static Vector3i getRboData() {
        int count = 0;
        int vram = 0;
        for (Fbo fbo : fbos.values()) {
            if (fbo.isUsable()) {
                int attachmentSize = fbo.getSize().x() * fbo.getSize().y() * 4 * 4 * fbo.getNumberOfSamples();
                for (int i = 0; i < 8; i++) {
                    if (fbo.isThereAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.RBO, i)) {
                        count++;
                        vram += attachmentSize;
                    }
                }
                if (fbo.isThereAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.RBO, 0)) {
                    count++;
                    vram += attachmentSize;
                }
                if (fbo.isThereAttachment(Fbo.FboAttachmentSlot.STENCIL, Fbo.FboAttachmentType.RBO, 0)) {
                    count++;
                    vram += attachmentSize;
                }
                if (fbo.isThereAttachment(Fbo.FboAttachmentSlot.DEPTH_STENCIL, Fbo.FboAttachmentType.RBO, 0)) {
                    count++;
                    vram += attachmentSize;
                }
            }
        }
        return new Vector3i(count, 0, vram);
    }

    //
    //UBOs----------------------------------------------------------------------
    //
    /**
     * Returns the specified UBO.
     *
     * @param key UBO's key
     * @return UBO
     */
    @Nullable
    public static Ubo getUbo(@Nullable ResourceId key) {
        return ubos.get(key);
    }

    /**
     * Adds the given UBO to the list of UBOs.
     *
     * @param ubo UBO
     */
    public static void addUbo(@NotNull Ubo ubo) {
        if (!ubos.containsKey(ubo.getResourceId())) {
            ubos.put(ubo.getResourceId(), ubo);
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
     */
    @Nullable
    public static Vao getVao(@Nullable ResourceId key) {
        return vaos.get(key);
    }

    /**
     * Adds the given VAO to the list of VAOs.
     *
     * @param vao VAO
     */
    public static void addVao(@NotNull Vao vao) {
        if (!vaos.containsKey(vao.getResourceId())) {
            vaos.put(vao.getResourceId(), vao);
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
     */
    @Nullable
    public static Shader getShader(@Nullable ResourceId key) {
        return shaders.get(key);
    }

    /**
     * Adds the given sound to the list of shaders.
     *
     * @param shader sound
     */
    public static void addShader(@NotNull Shader shader) {
        if (!shaders.containsKey(shader.getResourceId())) {
            shaders.put(shader.getResourceId(), shader);
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
     */
    @Nullable
    public static AudioBuffer getAudioBuffer(@Nullable ResourceId key) {
        return audioBuffers.get(key);
    }

    /**
     * Adds the given audio buffer to the list of audio buffers.
     *
     * @param sound audio buffer
     */
    public static void addAudioBuffer(@NotNull AudioBuffer sound) {
        if (!audioBuffers.containsKey(sound.getResourceId())) {
            audioBuffers.put(sound.getResourceId(), sound);
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
     */
    @Nullable
    public static AudioSource getAudioSource(@Nullable ResourceId key) {
        return audioSources.get(key);
    }

    /**
     * Adds the given audio source to the list of audio sources.
     *
     * @param source audio source
     */
    public static void addAudioSource(@NotNull AudioSource source) {
        if (!audioSources.containsKey(source.getResourceId())) {
            audioSources.put(source.getResourceId(), source);
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
    private static <T extends Resource> void releaseResourceMap(@NotNull Map<ResourceId, T> resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        ResourceId[] keys = new ResourceId[resources.keySet().size()];
        resources.keySet().toArray(keys);

        for (ResourceId key : keys) {
            Resource resource = resources.get(key);
            if (resource.isUsable()) {
                resource.release();
            }
            resources.remove(key);
        }
    }

}
