package wobani.resources.texture;

/**
 Interface for textures where allowed to change the color space.
 */
public interface ChangableColorSpace{

    /**
     Sets whether or not the texture is in sRGB color space.

     @param sRgb sRGB
     */
    void setsRgb(boolean sRgb);
}
