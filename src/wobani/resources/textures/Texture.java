package wobani.resources.textures;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resources.*;

/**
 * Base interface for all types of textures.
 */
public interface Texture extends Resource {

    /**
     * Texture filter mode.
     */
    public enum TextureFilter {
        /**
         * Nearest filter.
         */
        NEAREST(GL11.GL_NEAREST),
        /**
         * Linear filter.
         */
        LINEAR(GL11.GL_LINEAR),
        /**
         * Nearest mipmap nearest filter.
         */
        NEAREST_MIPMAP_NEAREST(GL11.GL_NEAREST_MIPMAP_NEAREST),
        /**
         * Linear mipmap linear filter.
         */
        LINEAR_MIPMAP_LINEAR(GL11.GL_LINEAR_MIPMAP_LINEAR),
        /**
         * Nearest mipmap linear filter.
         */
        NEAREST_MIPMAP_LINEAR(GL11.GL_NEAREST_MIPMAP_LINEAR),
        /**
         * Linear mipmap nearest filter.
         */
        LINEAR_MIPMAP_NEAREST(GL11.GL_LINEAR_MIPMAP_NEAREST);

        /**
         * Texture filter's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new TextureFilter to the given value.
         *
         * @param code filter's OpenGL code
         */
        private TextureFilter(int code) {
            openGlCode = code;
        }

        /**
         * Returns the filter's OpenGL code.
         *
         * @return the filter's OpenGL code
         */
        public int getCode() {
            return openGlCode;
        }
    }

    /**
     * Texture filter type.
     */
    public enum TextureFilterType {
        /**
         * Magnification.
         */
        MAGNIFICATION(GL11.GL_TEXTURE_MAG_FILTER),
        /**
         * Minification.
         */
        MINIFICATION(GL11.GL_TEXTURE_MIN_FILTER);

        /**
         * Texture filter type's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new TextureFilterType to the given value.
         *
         * @param code texture filter type's OpenGL code
         */
        private TextureFilterType(int code) {
            openGlCode = code;
        }

        /**
         * Returns the texture filter type's OpenGL code.
         *
         * @return the texture filter type's OpenGL code
         */
        public int getCode() {
            return openGlCode;
        }
    }

    /**
     * Texture wrap mode.
     */
    public enum TextureWrap {
        /**
         * Repeat.
         */
        REPEAT(GL11.GL_REPEAT),
        /**
         * Mirrored repeat.
         */
        MIRRORED_REPEAT(GL14.GL_MIRRORED_REPEAT),
        /**
         * Clamp to edge.
         */
        CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE),
        /**
         * Clamp to border. It uses the border color.
         */
        CLAMP_TO_BORDER(GL13.GL_CLAMP_TO_BORDER);

        /**
         * Texture wrap mode's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new TextureWrap to the given value.
         *
         * @param code texture wrap mode's OpenGL code
         */
        private TextureWrap(int code) {
            openGlCode = code;
        }

        /**
         * Return the texture wrap mode's OpenGL code.
         *
         * @return the texture wrap mode's OpenGL code
         */
        public int getCode() {
            return openGlCode;
        }
    }

    /**
     * Texture wrap direction.
     */
    public enum TextureWrapDirection {
        /**
         * U direction in texture space.
         */
        WRAP_U(GL11.GL_TEXTURE_WRAP_S),
        /**
         * V direction in texture space.
         */
        WRAP_V(GL11.GL_TEXTURE_WRAP_T),
        /**
         * W direction in texture space.
         */
        WRAP_W(GL12.GL_TEXTURE_WRAP_R);

        /**
         * Texture wrap type's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new TextureWrapDirection to the given value.
         *
         * @param code texture wrap type's OpenGL code
         */
        private TextureWrapDirection(int code) {
            openGlCode = code;
        }

        /**
         * Returns the texture wrap type's OpenGL code.
         *
         * @return the texture wrap type's OpenGL code
         */
        public int getCode() {
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
    public int getId();

    /**
     * Unbinds the texture.
     */
    public void unbind();

    /**
     * Returns the texture's width and height.
     *
     * @return the texture's width and height
     */
    public Vector2i getSize();

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
}
