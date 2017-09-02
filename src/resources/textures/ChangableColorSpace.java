package resources.textures;

/**
 * Interface for textures where allowed to change the color space.
 */
public interface ChangableColorSpace {

    /**
     * Sets whether or not the texture is in sRGB color space.
     *
     * @param sRgb sRGB
     */
    public void setsRgb(boolean sRgb);
}
