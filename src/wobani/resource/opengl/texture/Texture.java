package wobani.resource.opengl.texture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.toolbox.annotation.*;

/**
 * Base interface for all types of textures.
 */
public interface Texture extends Resource {

    /**
     * Binds the texture.
     */
    void bind();

    /**
     * Unbinds the texture.
     */
    void unbind();

    /**
     * Returns the texture's width and height.
     *
     * @return the texture's width and height
     */
    Vector2i getSize();

    /**
     * Activates the texture in the specified texture unit.
     *
     * @param textureUnit texture unit (0;31)
     */
    void bindToTextureUnit(int textureUnit);

    /**
     * Determines whether the texture is in sRGB color space.
     *
     * @return true if the texture's color space is sRGB, false otherwise
     */
    boolean issRgb();

    int getId();

    /**
     * Texture filter mode.
     */
    enum TextureFilter {
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
        TextureFilter(int code) {
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
    enum TextureFilterType {
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
        TextureFilterType(int code) {
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
    enum TextureWrap {
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
        CLAMP_TO_BORDER(GL13.GL_CLAMP_TO_BORDER),
        /**
         * Mirrors the image once each direction than clams to the edge.
         */
        MIRROR_CLAMP_TO_EDGE(GL44.GL_MIRROR_CLAMP_TO_EDGE);

        /**
         * Texture wrap mode's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new TextureWrap to the given value.
         *
         * @param code texture wrap mode's OpenGL code
         */
        TextureWrap(int code) {
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
    enum TextureWrapDirection {
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
         * Initializes a new TextureWrapDirection to the given value.
         *
         * @param code texture wrap type's OpenGL code
         */
        TextureWrapDirection(int code) {
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

    enum TextureInternalFormat{
        R8(GL30.GL_R8, 1),
        R16(GL30.GL_R16, 1),
        RG8(GL30.GL_RG8, 2),
        RG16(GL30.GL_RG16, 2),
        RGB4(GL30.GL_RGB4, 3),
        RGB5(GL30.GL_RGB5, 3),
        RGB8(GL30.GL_RGB8, 3),
        RGB10(GL30.GL_RGB10, 3),
        RGB12(GL30.GL_RGB12, 3),
        RGB16(GL30.GL_RGB16, 3),
        RGBA2(GL30.GL_RGBA2, 4),
        RGBA4(GL30.GL_RGBA4, 4),
        RGB5_A1(GL30.GL_RGB5_A1, 4),
        RGBA8(GL30.GL_RGBA8, 4),
        RGB10_A2(GL30.GL_RGB10_A2, 4),
        RGBA12(GL30.GL_RGBA12, 4),
        RGBA16(GL30.GL_RGBA16, 4),
        SRGB8(GL30.GL_SRGB8, 3),
        SRGB8_A8(GL30.GL_SRGB8_ALPHA8, 4),
        R16F(GL30.GL_R16F, 1),
        RG16F(GL30.GL_RG16F, 2),
        RGB16F(GL30.GL_RGB16F, 3),
        RGBA16F(GL30.GL_RGBA16F, 4),
        R32F(GL30.GL_R32F, 1),
        RG32F(GL30.GL_RG32F, 2),
        RGB32F(GL30.GL_RGB32F, 3),
        RGBA32F(GL30.GL_RGBA32F, 4),
        R8I(GL30.GL_R8I, 1),
        R8UI(GL30.GL_R8UI, 1),
        R16I(GL30.GL_R16I, 1),
        R16UI(GL30.GL_R16UI, 1),
        R32I(GL30.GL_R32I, 1),
        R32UI(GL30.GL_R32UI, 1),
        RG8I(GL30.GL_RG8I, 2),
        RG8UI(GL30.GL_RG8UI, 2),
        RG16I(GL30.GL_RG16I, 2),
        RG16UI(GL30.GL_RG16UI, 2),
        RG32I(GL30.GL_RG32I, 2),
        RG32UI(GL30.GL_RG32UI, 2),
        RGB8I(GL30.GL_RGB8I, 3),
        RGB8UI(GL30.GL_RGB8UI, 3),
        RGB16I(GL30.GL_RGB16I, 3),
        RGB16UI(GL30.GL_RGB16UI, 3),
        RGB32I(GL30.GL_RGB32I, 3),
        RGB32UI(GL30.GL_RGB32UI, 3),
        RGBA8I(GL30.GL_RGBA8I, 4),
        RGBA8UI(GL30.GL_RGBA8UI, 4),
        RGBA16I(GL30.GL_RGBA16I, 4),
        RGBA16UI(GL30.GL_RGBA16UI, 4),
        RGBA32I(GL30.GL_RGBA32I, 4),
        RGBA32UI(GL30.GL_RGBA32UI, 4),
        DEPTH32F(GL30.GL_DEPTH_COMPONENT32F, 1),
        DEPTH24(GL14.GL_DEPTH_COMPONENT24, 1),
        DEPTH16(GL14.GL_DEPTH_COMPONENT16, 1),
        DEPTH32F_STENCIL8(GL30.GL_DEPTH32F_STENCIL8, 2),
        DEPTH24_STENCIL8(GL30.GL_DEPTH24_STENCIL8, 2),
        STENCIL8(GL30.GL_STENCIL_INDEX8, 1);

        /**
         Buffer Object usage's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new BufferObjectUsage to the given value.

         @param code Buffer Object usage's OpenGL code
         */
        TextureInternalFormat(int code, int componentCount){
            this.code = code;
        }

        /**
         Returns the BufferObjectUsage of the given OpenGL code.

         @param code OpenGL Buffer Object usage

         @return the BufferObjectUsage of the given OpenGL code

         @throws IllegalArgumentException if the given parameter is not a Buffer Object usage
         */
        @NotNull
        public static TextureInternalFormat valueOf(int code){
            for(TextureInternalFormat mode : TextureInternalFormat.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a Buffer Object usage");
        }

        /**
         Returns the Buffer Object usage's OpenGL code.

         @return the Buffer Object usage's OpenGL code
         */
        public int getCode(){
            return code;
        }

    }

    enum TextureDataType{
        UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE),
        BYTE(GL11.GL_BYTE),
        UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT),
        SHORT(GL11.GL_SHORT),
        UNSIGNED_INT(GL11.GL_UNSIGNED_INT),
        INT(GL11.GL_INT),
        FLOAT(GL11.GL_FLOAT);

        /**
         Buffer Object usage's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new BufferObjectUsage to the given value.

         @param code Buffer Object usage's OpenGL code
         */
        TextureDataType(int code){
            this.code = code;
        }

        /**
         Returns the BufferObjectUsage of the given OpenGL code.

         @param code OpenGL Buffer Object usage

         @return the BufferObjectUsage of the given OpenGL code

         @throws IllegalArgumentException if the given parameter is not a Buffer Object usage
         */
        @NotNull
        public static TextureDataType valueOf(int code){
            for(TextureDataType mode : TextureDataType.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a Buffer Object usage");
        }

        /**
         Returns the Buffer Object usage's OpenGL code.

         @return the Buffer Object usage's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    enum TextureFormat{
        RED(GL11.GL_RED),
        RG(GL30.GL_RG),
        RGB(GL11.GL_RGB),
        BGR(GL12.GL_BGR),
        RGBA(GL11.GL_RGBA),
        BGRA(GL12.GL_BGRA),
        DEPTH(GL11.GL_DEPTH_COMPONENT),
        STENCILL(GL11.GL_STENCIL_INDEX),
        DEPTH_STENCIL(GL30.GL_DEPTH_STENCIL);

        /**
         Buffer Object usage's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new BufferObjectUsage to the given value.

         @param code Buffer Object usage's OpenGL code
         */
        TextureFormat(int code){
            this.code = code;
        }

        /**
         Returns the BufferObjectUsage of the given OpenGL code.

         @param code OpenGL Buffer Object usage

         @return the BufferObjectUsage of the given OpenGL code

         @throws IllegalArgumentException if the given parameter is not a Buffer Object usage
         */
        @NotNull
        public static TextureFormat valueOf(int code){
            for(TextureFormat mode : TextureFormat.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a Buffer Object usage");
        }

        /**
         Returns the Buffer Object usage's OpenGL code.

         @return the Buffer Object usage's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }
}
