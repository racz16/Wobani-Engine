package core;

import resources.*;
import resources.textures.EasyFiltering.TextureFiltering;
import toolbox.annotations.*;

/**
 * Stores the game's settings.
 */
public class Settings {

    //
    //user----------------------------------------------------------------------
    //
    /**
     * Determines whether the shadows are enabled.
     */
    private static boolean shadowMapping = true;
    /**
     * Shadow map's size.
     */
    private static int shadowMapResolution = 1024;
    /**
     * Multisample antialiasing level.
     */
    private static int msaaLevel = 2;
    /**
     * Texture filtering mode.
     */
    private static TextureFiltering textureFiltering = TextureFiltering.ANISOTROPIC_2X;
    /**
     * Gamma value.
     */
    private static float gamma = 2.2f;
    //
    //engine--------------------------------------------------------------------
    //
    /**
     * Determines whether the wireframe mode is enabled.
     */
    private static boolean wireframeMode;
    /**
     * Determines whether the frustum culling is enabled.
     */
    private static boolean frustumCulling = true;
    /**
     * The directional light's shadow camera's distance from the user's camera's
     * center.
     */
    private static float shadowCameraDistance = 400;
    /**
     * The directional light's shadow camera's near plane's distance.
     */
    private static float shadowCameraNearDistance = 0.1f;
    /**
     * The directional light's shadow camera's far plane's distance.
     */
    private static float shadowCameraFarDistance = 10000;

    /**
     * To can't create Settings instance.
     */
    private Settings() {
    }

    /**
     * Determines whether shadow mapping is enabled.
     *
     * @return true if shadow mapping is enabled, false otherwise
     */
    public static boolean isShadowMapping() {
        return shadowMapping;
    }

    /**
     * Sets whether or not objects cast shadows.
     *
     * @param shadows true if objects should cast shadows, false otherwise
     */
    public static void setShadowMapping(boolean shadows) {
        Settings.shadowMapping = shadows;
    }

    /**
     * Returns the shadow map's size.
     *
     * @return shadow map's size
     */
    public static int getShadowMapResolution() {
        return shadowMapResolution;
    }

    /**
     * Sets the shadow map's size to the given value.
     *
     * @param size shadow map's size
     *
     * @throws IllegalArgumentException shadow map size must be positive
     */
    public static void setShadowMapResolution(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Shadow map resolution must be positive");
        }
        shadowMapResolution = size;
    }

    /**
     * Returns the multisample antialiasing's level.
     *
     * @return multisample antialiasing's level
     */
    public static int getMsaaLevel() {
        return msaaLevel;
    }

    /**
     * Sets the MSAA level to the given value.
     *
     * @param msaaLevel MSAA level
     *
     * @throws IllegalArgumentException MSAA can't be lower than 1
     */
    public static void setMsaaLevel(int msaaLevel) {
        if (msaaLevel < 1) {
            throw new IllegalArgumentException("MSAA can't be lower than 1");
        }
        Settings.msaaLevel = msaaLevel;
    }

    /**
     * Returns the texture filtering mode.
     *
     * @return texture filtering mode
     */
    @NotNull
    public static TextureFiltering getTextureFiltering() {
        return textureFiltering;
    }

    /**
     * Sets the textures' filtering mode to the given value.
     *
     * @param tf textures' filtering mode
     *
     * @throws NullPointerException texture filtering can't be null
     */
    public static void setTextureFiltering(@NotNull TextureFiltering tf) {
        if (tf == null) {
            throw new NullPointerException();
        }
        if (textureFiltering != tf) {
            textureFiltering = tf;
            ResourceManager.changeTextureFiltering();
        }
    }

    /**
     * Returns the gamma value.
     *
     * @return gamma value
     */
    public static float getGamma() {
        return gamma;
    }

    /**
     * Sets the gamma to the given value.
     *
     * @param gamma gamma value
     *
     * @throws IllegalArgumentException gamma can't be lower than 1
     */
    public static void setGamma(float gamma) {
        if (gamma < 1) {
            throw new IllegalArgumentException("Gamma can't be lower than 1");
        }
        float oldGamma = getGamma();
        Settings.gamma = gamma;
        if (oldGamma == 1 && gamma != 1 || gamma == 1 && oldGamma != 1) {
            ResourceManager.changeTextureColorSpace();
        }
    }

    //
    //engine--------------------------------------------------------------------
    //
    /**
     * Determines whether wireframe mode is enabled.
     *
     * @return true if wireframe mode is enabled, false otherwise
     */
    public static boolean isWireframeMode() {
        return wireframeMode;
    }

    /**
     * Sets whether or not wireframe mode is enabled.
     *
     * @param wireframeMode true if wireframe mode should be enabled, false
     * otherwise
     */
    public static void setWireframeMode(boolean wireframeMode) {
        Settings.wireframeMode = wireframeMode;
    }

    /**
     * Determines whether frustum culling is enabled.
     *
     * @return true if frustum culling is enabled, false otherwise
     */
    public static boolean isFrustumCulling() {
        return frustumCulling;
    }

    /**
     * Sets whether or not frustum culling is enabled.
     *
     * @param frustumCulling true if frustum culling should be enabled, false
     * otherwise
     */
    public static void setFrustumCulling(boolean frustumCulling) {
        Settings.frustumCulling = frustumCulling;
    }

    /**
     * Returns the shadow camera's distance from the user's camera's center.
     *
     * @return the shadow camera's distance from the user's camera's center
     */
    public static float getShadowCameraDistance() {
        return shadowCameraDistance;
    }

    /**
     * Sets the shadow camera's distance from the user's camera's center to the
     * given value.
     *
     * @param shadowCameraDistance the shadow camera's distance from the user's
     * camera's center
     *
     * @throws IllegalArgumentException the parameter have to be higher than
     * zero
     */
    public static void setShadowCameraDistance(float shadowCameraDistance) {
        if (shadowCameraDistance <= 0) {
            throw new IllegalArgumentException("the parameter have to be higher than zero");
        }
        Settings.shadowCameraDistance = shadowCameraDistance;
    }

    /**
     * Returns the shadow camera's near plane's distance.
     *
     * @return the shadow camera's near plane's distance
     */
    public static float getShadowCameraNearDistance() {
        return shadowCameraNearDistance;
    }

    /**
     * Sets the shadow camera's near plane's distance to the given value.
     *
     * @param shadowCameraNearDistance the shadow camera's near plane's distance
     *
     * @throws IllegalArgumentException near plane's distance must be higher
     * than 0 and it can't be higher than the far plane's distance
     */
    public static void setShadowCameraNearDistance(float shadowCameraNearDistance) {
        if (shadowCameraNearDistance <= 0) {
            throw new IllegalArgumentException("Near plane's distance must be higher than 0");
        }
        if (shadowCameraNearDistance > shadowCameraFarDistance) {
            throw new IllegalArgumentException("Near plane's distance can't be higher than the far plane's distance");
        }
        Settings.shadowCameraNearDistance = shadowCameraNearDistance;
    }

    /**
     * Returns the shadow camera's far plane's distance.
     *
     * @return the shadow camera's far plane's distance
     */
    public static float getShadowCameraFarDistance() {
        return shadowCameraFarDistance;
    }

    /**
     * Sets the shadow camera's far plane's distance to the given value.
     *
     * @param shadowCameraFarDistance the shadow camera's far plane's distance
     *
     * @throws IllegalArgumentException far plane's distance must be higher than
     * the near plane's distance
     */
    public static void setShadowCameraFarDistance(float shadowCameraFarDistance) {
        if (shadowCameraNearDistance > shadowCameraFarDistance) {
            throw new IllegalArgumentException("Far plane's distance must be higher than the near plane's distance");
        }
        Settings.shadowCameraFarDistance = shadowCameraFarDistance;
    }

}
