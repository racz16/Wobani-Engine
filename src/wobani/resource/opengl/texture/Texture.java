package wobani.resource.opengl.texture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.buffer.*;
import wobani.toolbox.annotation.*;

import static wobani.resource.opengl.buffer.Fbo.FboAttachment.*;

/**
 Base interface for all types of textures.
 */
public interface Texture extends Resource{

    /**
     Returns the texture's width and height. If it's a Cube Map Texture, it returns one face's width and height.

     @return the texture's width and height
     */
    Vector2i getSize();

    /**
     Activates the texture in the specified texture unit.

     @param textureUnit texture unit (0;31)
     */
    void bindToTextureUnit(int textureUnit);

    //FIXME: sRGB gamma correction problem
    /**
     Determines whether the texture is in sRGB color space.

     @return true if the texture's color space is sRGB, false otherwise
     */
    boolean issRgb();

    /**
     Returns the texture's native OpenGL id.

     @return the texture's native OpenGL id
     */
    int getId();

    /**
     Texture filter mode.
     */
    enum TextureFilter{
        /**
         No filter.
         */
        NONE(GL11.GL_NEAREST, GL11.GL_NEAREST_MIPMAP_NEAREST),
        /**
         Bilinear filter.
         */
        BILINEAR(GL11.GL_LINEAR, GL11.GL_LINEAR_MIPMAP_NEAREST),
        /**
         Trilinear filter.
         */
        TRILINEAR(GL11.GL_LINEAR, GL11.GL_LINEAR_MIPMAP_LINEAR);

        /**
         The magnification filter's OpenGL code.
         */
        private final int magnificationCode;
        /**
         The minification filter's OpenGL code.
         */
        private final int minificationCode;

        /**
         Initializes a new TextureFilter to the given values.

         @param magnificationCode magnification filter's OpenGL code
         @param minificationCode  minification filter's OpenGL code
         */
        TextureFilter(int magnificationCode, int minificationCode){
            this.magnificationCode = magnificationCode;
            this.minificationCode = minificationCode;
        }

        /**
         Returns the magnification filter's OpenGL code.

         @return the magnification filter's OpenGL code
         */
        public int getMagnificationCode(){
            return magnificationCode;
        }

        /**
         Returns the minification filter's OpenGL code.

         @return the minification filter's OpenGL code
         */
        public int getMinificationCode(){
            return minificationCode;
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
        CLAMP_TO_BORDER(GL13.GL_CLAMP_TO_BORDER),
        /**
         Mirrors the image once each direction, then clamps to the edge.
         */
        MIRROR_CLAMP_TO_EDGE(GL44.GL_MIRROR_CLAMP_TO_EDGE);

        /**
         Texture wrap's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new TextureWrap to the given value.

         @param code texture wrap's OpenGL code
         */
        TextureWrap(int code){
            this.code = code;
        }

        /**
         Return the texture wrap's OpenGL code.

         @return the texture wrap's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     Internal format.
     */
    enum TextureInternalFormat{
        /**
         R8.
         */
        R8(GL30.GL_R8, 1, 8, COLOR),
        /**
         R16.
         */
        R16(GL30.GL_R16, 1, 16, COLOR),
        /**
         RG8.
         */
        RG8(GL30.GL_RG8, 2, 16, COLOR),
        /**
         RG16.
         */
        RG16(GL30.GL_RG16, 2, 32, COLOR),
        /**
         RGB4.
         */
        RGB4(GL30.GL_RGB4, 3, 12, COLOR),
        /**
         RGB5.
         */
        RGB5(GL30.GL_RGB5, 3, 15, COLOR),
        /**
         RGB8.
         */
        RGB8(GL30.GL_RGB8, 3, 24, COLOR),
        /**
         RGB10.
         */
        RGB10(GL30.GL_RGB10, 3, 30, COLOR),
        /**
         RGB12.
         */
        RGB12(GL30.GL_RGB12, 3, 36, COLOR),
        /**
         RGB16.
         */
        RGB16(GL30.GL_RGB16, 3, 48, COLOR),
        /**
         RGBA2.
         */
        RGBA2(GL30.GL_RGBA2, 4, 8, COLOR),
        /**
         RGBA4.
         */
        RGBA4(GL30.GL_RGBA4, 4, 16, COLOR),
        /**
         RGB5_A1.
         */
        RGB5_A1(GL30.GL_RGB5_A1, 4, 16, COLOR),
        /**
         RGBA8.
         */
        RGBA8(GL30.GL_RGBA8, 4, 32, COLOR),
        /**
         RGB10 A2.
         */
        RGB10_A2(GL30.GL_RGB10_A2, 4, 32, COLOR),
        /**
         RGBA12.
         */
        RGBA12(GL30.GL_RGBA12, 4, 48, COLOR),
        /**
         RGBA16.
         */
        RGBA16(GL30.GL_RGBA16, 4, 64, COLOR),
        /**
         SRGB8.
         */
        SRGB8(GL30.GL_SRGB8, 3, 24, COLOR),
        /**
         SRGB8 A8.
         */
        SRGB8_A8(GL30.GL_SRGB8_ALPHA8, 4, 32, COLOR),
        /**
         R16F.
         */
        R16F(GL30.GL_R16F, 1, 16, COLOR),
        /**
         RG16F.
         */
        RG16F(GL30.GL_RG16F, 2, 32, COLOR),
        /**
         RGB16F.
         */
        RGB16F(GL30.GL_RGB16F, 3, 48, COLOR),
        /**
         RGBA16F.
         */
        RGBA16F(GL30.GL_RGBA16F, 4, 64, COLOR),
        /**
         R32F.
         */
        R32F(GL30.GL_R32F, 1, 32, COLOR),
        /**
         RG32F.
         */
        RG32F(GL30.GL_RG32F, 2, 64, COLOR),
        /**
         RGB32F.
         */
        RGB32F(GL30.GL_RGB32F, 3, 96, COLOR),
        /**
         RGBA32F.
         */
        RGBA32F(GL30.GL_RGBA32F, 4, 128, COLOR),
        /**
         R8I.
         */
        R8I(GL30.GL_R8I, 1, 8, COLOR),
        /**
         R8UI.
         */
        R8UI(GL30.GL_R8UI, 1, 8, COLOR),
        /**
         R16I.
         */
        R16I(GL30.GL_R16I, 1, 16, COLOR),
        /**
         R16UI.
         */
        R16UI(GL30.GL_R16UI, 1, 16, COLOR),
        /**
         R32I.
         */
        R32I(GL30.GL_R32I, 1, 32, COLOR),
        /**
         R32UI.
         */
        R32UI(GL30.GL_R32UI, 1, 32, COLOR),
        /**
         RG8I.
         */
        RG8I(GL30.GL_RG8I, 2, 16, COLOR),
        /**
         RG8UI.
         */
        RG8UI(GL30.GL_RG8UI, 2, 16, COLOR),
        /**
         RG16I.
         */
        RG16I(GL30.GL_RG16I, 2, 32, COLOR),
        /**
         RG16UI.
         */
        RG16UI(GL30.GL_RG16UI, 2, 32, COLOR),
        /**
         RG32I.
         */
        RG32I(GL30.GL_RG32I, 2, 64, COLOR),
        /**
         RG32UI.
         */
        RG32UI(GL30.GL_RG32UI, 2, 64, COLOR),
        /**
         RGB8I.
         */
        RGB8I(GL30.GL_RGB8I, 3, 24, COLOR),
        /**
         RGB8UI.
         */
        RGB8UI(GL30.GL_RGB8UI, 3, 24, COLOR),
        /**
         RGB16I.
         */
        RGB16I(GL30.GL_RGB16I, 3, 48, COLOR),
        /**
         RGB16UI.
         */
        RGB16UI(GL30.GL_RGB16UI, 3, 48, COLOR),
        /**
         RGB32I.
         */
        RGB32I(GL30.GL_RGB32I, 3, 96, COLOR),
        /**
         RGB32UI.
         */
        RGB32UI(GL30.GL_RGB32UI, 3, 96, COLOR),
        /**
         RGBA8I.
         */
        RGBA8I(GL30.GL_RGBA8I, 4, 32, COLOR),
        /**
         RGBA8UI.
         */
        RGBA8UI(GL30.GL_RGBA8UI, 4, 32, COLOR),
        /**
         RGBA16I.
         */
        RGBA16I(GL30.GL_RGBA16I, 4, 64, COLOR),
        /**
         RGBA16UI.
         */
        RGBA16UI(GL30.GL_RGBA16UI, 4, 64, COLOR),
        /**
         RGBA32I.
         */
        RGBA32I(GL30.GL_RGBA32I, 4, 128, COLOR),
        /**
         RGBA32UI.
         */
        RGBA32UI(GL30.GL_RGBA32UI, 4, 128, COLOR),
        /**
         DEPTH32F.
         */
        DEPTH32F(GL30.GL_DEPTH_COMPONENT32F, 1, 32, DEPTH),
        /**
         DEPTH24.
         */
        DEPTH24(GL14.GL_DEPTH_COMPONENT24, 1, 24, DEPTH),
        /**
         DEPTH16.
         */
        DEPTH16(GL14.GL_DEPTH_COMPONENT16, 1, 16, DEPTH),
        /**
         DEPTH32F STENCIL8.
         */
        DEPTH32F_STENCIL8(GL30.GL_DEPTH32F_STENCIL8, 2, 40, DEPTH_STENCIL),
        /**
         DEPTH24 STENCIL8.
         */
        DEPTH24_STENCIL8(GL30.GL_DEPTH24_STENCIL8, 2, 32, DEPTH_STENCIL),
        /**
         STENCIL8.
         */
        STENCIL8(GL30.GL_STENCIL_INDEX8, 1, 8, STENCIL);

        /**
         Internal format's OpenGL code.
         */
        private final int code;
        /**
         Number of used color channels.
         */
        private final int colorChannelCount;
        /**
         The internal format's bit depth.
         */
        private final int bitDepth;
        /**
         The internal format's FBO attachment.
         */
        private final Fbo.FboAttachment attachmentSlot;

        /**
         Initializes a new TextureInternalFormat to the given values.

         @param code              internal format's OpenGL code
         @param colorChannelCount number of used color channels
         @param bitDepth          internal format's bit depth
         @param attachmentSlot    the internal format's FBO attachment
         */
        TextureInternalFormat(int code, int colorChannelCount, int bitDepth, Fbo.FboAttachment attachmentSlot){
            this.code = code;
            this.colorChannelCount = colorChannelCount;
            this.bitDepth = bitDepth;
            this.attachmentSlot = attachmentSlot;
        }

        /**
         Returns the TextureInternalFormat of the given OpenGL code.

         @param code internal format's OpenGL code

         @return the TextureInternalFormat of the given OpenGL code

         @throws IllegalArgumentException if the given parameter is not an internal format
         */
        @NotNull
        public static TextureInternalFormat valueOf(int code){
            for(TextureInternalFormat mode : TextureInternalFormat.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not an internal format");
        }

        /**
         Returns the internal format's OpenGL code.

         @return the internal format's OpenGL code
         */
        public int getCode(){
            return code;
        }

        /**
         Returns the number of used color channels in the internal format.

         @return the number of used color channels in the internal format.
         */
        public int getColorChannelCount(){
            return colorChannelCount;
        }

        /**
         Returns the internal format's bit depth.

         @return the internal format's bit depth
         */
        public int getBitDepth(){
            return bitDepth;
        }

        /**
         Returns the internal format's FBO attachment.

         @return the internal format's FBO attachment
         */
        @NotNull
        public Fbo.FboAttachment getAttachmentSlot(){
            return attachmentSlot;
        }
    }

    /**
     Texture format.
     */
    enum TextureFormat{
        /**
         Red.
         */
        RED(GL11.GL_RED, 1, COLOR),
        /**
         RG.
         */
        RG(GL30.GL_RG, 2, COLOR),
        /**
         RGB.
         */
        RGB(GL11.GL_RGB, 3, COLOR),
        /**
         BGR.
         */
        BGR(GL12.GL_BGR, 3, COLOR),
        /**
         RGBA.
         */
        RGBA(GL11.GL_RGBA, 4, COLOR),
        /**
         BGRA.
         */
        BGRA(GL12.GL_BGRA, 4, COLOR),
        /**
         Depth.
         */
        DEPTH(GL11.GL_DEPTH_COMPONENT, 1, Fbo.FboAttachment.DEPTH),
        /**
         Stencil.
         */
        STENCIL(GL11.GL_STENCIL_INDEX, 1, Fbo.FboAttachment.STENCIL),
        /**
         Depth-stencil.
         */
        DEPTH_STENCIL(GL30.GL_DEPTH_STENCIL, 2, Fbo.FboAttachment.DEPTH_STENCIL);

        /**
         Texture format's OpenGL code.
         */
        private final int code;
        /**
         Number of used color channels.
         */
        private final int colorChannelCount;
        /**
         The texture format's FBO attachment.
         */
        private final Fbo.FboAttachment attachmentSlot;

        /**
         Initializes a new TextureFormat to the given values.

         @param code              texture format's OpenGL code
         @param colorChannelCount number of used color channels
         @param attachmentSlot    the texture format's FBO attachment
         */
        TextureFormat(int code, int colorChannelCount, Fbo.FboAttachment attachmentSlot){
            this.code = code;
            this.colorChannelCount = colorChannelCount;
            this.attachmentSlot = attachmentSlot;
        }

        /**
         Returns the TextureFormat of the given OpenGL code.

         @param code texture format's OpenGL code

         @return the TextureFormat of the given OpenGL code

         @throws IllegalArgumentException if the given parameter is not a texture format
         */
        @NotNull
        public static TextureFormat valueOf(int code){
            for(TextureFormat mode : TextureFormat.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a texture format");
        }

        /**
         Returns the texture format's OpenGL code.

         @return the texture format's OpenGL code
         */
        public int getCode(){
            return code;
        }

        /**
         Returns the number of used color channels in the texture format.

         @return the number of used color channels in the texture format.
         */
        public int getColorChannelCount(){
            return colorChannelCount;
        }

        /**
         Returns the texture format's FBO attachment.

         @return the texture format's FBO attachment
         */
        @NotNull
        public Fbo.FboAttachment getAttachmentSlot(){
            return attachmentSlot;
        }
    }

    /**
     Texture data type.
     */
    enum TextureDataType{
        /**
         Unsigned byte.
         */
        UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE),
        /**
         Byte.
         */
        BYTE(GL11.GL_BYTE),
        /**
         Unsigned short.
         */
        UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT),
        /**
         Short.
         */
        SHORT(GL11.GL_SHORT),
        /**
         Unsigned int.
         */
        UNSIGNED_INT(GL11.GL_UNSIGNED_INT),
        /**
         Int.
         */
        INT(GL11.GL_INT),
        /**
         Float.
         */
        FLOAT(GL11.GL_FLOAT);

        /**
         Texture data type's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new TextureDataType to the given value.

         @param code texture data type's OpenGL code
         */
        TextureDataType(int code){
            this.code = code;
        }

        /**
         Returns the TextureDataType of the given OpenGL code.

         @param code texture data type's OpenGL code

         @return the TextureDataType of the given OpenGL code

         @throws IllegalArgumentException if the given parameter is not a texture data type
         */
        @NotNull
        public static TextureDataType valueOf(int code){
            for(TextureDataType mode : TextureDataType.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a texture data type");
        }

        /**
         Returns the texture data type's OpenGL code.

         @return the texture data type's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }
}
