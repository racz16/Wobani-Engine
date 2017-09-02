package resources.textures;

import org.joml.*;
import org.lwjgl.opengl.*;

/**
 * Interface for the standard 2D textures.
 */
public interface Texture2D extends Texture {

    /**
     * Texture wrap type.
     */
    public enum TextureWrapType2D {
        /**
         * U direction in texture space.
         */
        WRAP_U(GL11.GL_TEXTURE_WRAP_S),
        /**
         * V direction in texture space.
         */
        WRAP_V(GL11.GL_TEXTURE_WRAP_T);

        /**
         * Texture wrap type's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new TextureWrapType2D to the given value.
         *
         * @param code texture wrap type's OpenGL code
         */
        private TextureWrapType2D(int code) {
            openGlCode = code;
        }

        /**
         * Returns the texture wrap type's OpenGL code.
         *
         * @return the texture wrap type's OpenGL code
         */
        public int getOpenGlCode() {
            return openGlCode;
        }
    }

    /**
     * Binds the texture.
     */
    public void bind();

    /**
     * Returns the texture's id.
     *
     * @return the texture's id
     */
    public int getTextureId();

    /**
     * Unbinds the texture.
     */
    public void unbind();

    /**
     * Activates the texture in the specified texture unit.
     *
     * @param textureUnit texture unit (0;31)
     */
    public void bindToTextureUnit(int textureUnit);

    /**
     * Determines whether the texture is in sRGB color space.
     *
     * @return true if the texture's color space is sRGB, false otherwise
     */
    public boolean issRgb();

    /**
     * Returns the texture's width and height.
     *
     * @return the texture's width and height
     */
    public Vector2i getSize();

}
