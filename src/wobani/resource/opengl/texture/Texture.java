package wobani.resource.opengl.texture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;

/**
 Base interface for all types of textures.
 */
public interface Texture extends Resource{

    /**
     Binds the texture.
     */
    void bind();

    /**
     Unbinds the texture.
     */
    void unbind();

    /**
     Returns the texture's width and height.

     @return the texture's width and height
     */
    Vector2i getSize();

    /**
     Activates the texture in the specified texture unit.

     @param textureUnit texture unit (0;31)
     */
    void bindToTextureUnit(int textureUnit);

    /**
     Determines whether the texture is in sRGB color space.

     @return true if the texture's color space is sRGB, false otherwise
     */
    boolean issRgb();

    int getId();

    /**
     Texture filter mode.
     */
    enum TextureFilter{
        /**
         Nearest filter.
         */
        NEAREST(GL11.GL_NEAREST),
        /**
         Linear filter.
         */
        LINEAR(GL11.GL_LINEAR),
        /**
         Nearest mipmap nearest filter.
         */
        NEAREST_MIPMAP_NEAREST(GL11.GL_NEAREST_MIPMAP_NEAREST),
        /**
         Linear mipmap linear filter.
         */
        LINEAR_MIPMAP_LINEAR(GL11.GL_LINEAR_MIPMAP_LINEAR),
        /**
         Nearest mipmap linear filter.
         */
        NEAREST_MIPMAP_LINEAR(GL11.GL_NEAREST_MIPMAP_LINEAR),
        /**
         Linear mipmap nearest filter.
         */
        LINEAR_MIPMAP_NEAREST(GL11.GL_LINEAR_MIPMAP_NEAREST);

        /**
         Texture filter's OpenGL code.
         */
        private final int openGlCode;

        /**
         Initializes a new TextureFilter to the given value.

         @param code filter's OpenGL code
         */
        TextureFilter(int code){
            openGlCode = code;
        }

        /**
         Returns the filter's OpenGL code.

         @return the filter's OpenGL code
         */
        public int getCode(){
            return openGlCode;
        }
    }

    /**
     Texture filter type.
     */
    enum TextureFilterType{
        /**
         Magnification.
         */
        MAGNIFICATION(GL11.GL_TEXTURE_MAG_FILTER),
        /**
         Minification.
         */
        MINIFICATION(GL11.GL_TEXTURE_MIN_FILTER);

        /**
         Texture filter type's OpenGL code.
         */
        private final int openGlCode;

        /**
         Initializes a new TextureFilterType to the given value.

         @param code texture filter type's OpenGL code
         */
        TextureFilterType(int code){
            openGlCode = code;
        }

        /**
         Returns the texture filter type's OpenGL code.

         @return the texture filter type's OpenGL code
         */
        public int getCode(){
            return openGlCode;
        }
    }

    /**
     Texture wrap mode.
     */
    enum TextureWrap{
        /**
         Repeat.
         */
        REPEAT(GL11.GL_REPEAT),
        /**
         Mirrored repeat.
         */
        MIRRORED_REPEAT(GL14.GL_MIRRORED_REPEAT),
        /**
         Clamp to edge.
         */
        CLAMP_TO_EDGE(GL12.GL_CLAMP_TO_EDGE),
        /**
         Clamp to border. It uses the border color.
         */
        CLAMP_TO_BORDER(GL13.GL_CLAMP_TO_BORDER);

        /**
         Texture wrap mode's OpenGL code.
         */
        private final int openGlCode;

        /**
         Initializes a new TextureWrap to the given value.

         @param code texture wrap mode's OpenGL code
         */
        TextureWrap(int code){
            openGlCode = code;
        }

        /**
         Return the texture wrap mode's OpenGL code.

         @return the texture wrap mode's OpenGL code
         */
        public int getCode(){
            return openGlCode;
        }
    }

    /**
     Texture wrap direction.
     */
    enum TextureWrapDirection{
        /**
         U direction in texture space.
         */
        WRAP_U(GL11.GL_TEXTURE_WRAP_S),
        /**
         V direction in texture space.
         */
        WRAP_V(GL11.GL_TEXTURE_WRAP_T),
        /**
         W direction in texture space.
         */
        WRAP_W(GL12.GL_TEXTURE_WRAP_R);

        /**
         Texture wrap type's OpenGL code.
         */
        private final int openGlCode;

        /**
         Initializes a new TextureWrapDirection to the given value.

         @param code texture wrap type's OpenGL code
         */
        TextureWrapDirection(int code){
            openGlCode = code;
        }

        /**
         Returns the texture wrap type's OpenGL code.

         @return the texture wrap type's OpenGL code
         */
        public int getCode(){
            return openGlCode;
        }
    }
}
