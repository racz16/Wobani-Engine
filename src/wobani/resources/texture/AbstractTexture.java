package wobani.resources.texture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.nio.*;

/**
 Basic data and methods for implementing a texture.
 */
public abstract class AbstractTexture implements Texture{

    /**
     Texture's width and height.
     */
    protected final Vector2i size = new Vector2i(-1);
    /**
     Texture's border color.
     */
    protected final Vector4f borderColor = new Vector4f(0);
    /**
     Texture's id.
     */
    protected int id = 0;
    /**
     Determines whether the texture is in sRGB color space.
     */
    protected boolean sRgb;
    /**
     Texture wrap along the U direction.
     */
    protected TextureWrap wrappingU = TextureWrap.REPEAT;
    /**
     Texture wrap along the V direction.
     */
    protected TextureWrap wrappingV = TextureWrap.REPEAT;
    /**
     Texture wrap along the W direction.
     */
    protected TextureWrap wrappingW = TextureWrap.REPEAT;
    /**
     Texture's magnification filter.
     */
    protected TextureFilter magnification = TextureFilter.NEAREST;
    /**
     Texture's minification filter.
     */
    protected TextureFilter minification = TextureFilter.NEAREST;

    @NotNull
    @ReadOnly
    @Override
    public Vector2i getSize(){
        return new Vector2i(size);
    }

    @Override
    public void update(){
    }

    /**
     Generates an id for the texture.
     */
    protected void glGenerateTextureId(){
        id = GL11.glGenTextures();
    }

    /**
     Returns the textures's id.

     @return texture's id
     */
    protected int glGetId(){
        return id;
    }

    /**
     Binds the texture.
     */
    protected void glBind(){
        GL11.glBindTexture(getTextureType(), id);
    }

    /**
     Activates the texture in the given texture unit.

     @param textureUnit texture unit (0;31)

     @throws IllegalArgumentException invalid texture unit
     */
    protected void glActivate(int textureUnit){
        if(textureUnit < 0 || textureUnit > 31){
            throw new IllegalArgumentException("Invalid texture unit");
        }

        GL13.glActiveTexture(textureUnit + 0x84C0);
    }

    /**
     Unbinds the texture.
     */
    protected void glUnbind(){
        GL11.glBindTexture(getTextureType(), 0);
    }

    /**
     Generates the texture's mipmaps.
     */
    @Bind
    protected void glGenerateMipmaps(){
        GL30.glGenerateMipmap(getTextureType());
    }

    /**
     Transfers image data to the texture based on the given values.

     @param internalFormat internal format
     @param format         format
     @param type           type
     @param data           image data
     */
    @Bind
    protected void glTexImage(int internalFormat, int format, int type, @Nullable ByteBuffer data){
        GL11.glTexImage2D(getTextureType(), 0, internalFormat, size.x, size.y, 0, format, type, data);
    }

    /**
     Returns the texture's border color.

     @return the texture's border color
     */
    @NotNull
    protected Vector4f glGetBorderColor(){
        return borderColor;
    }

    /**
     Sets the texture's border color to the given value.

     @param borderColor border color

     @throws NullPointerException     borderColor can't be null
     @throws IllegalArgumentException border color can't be lower than 0
     */
    @Bind
    protected void glSetBorderColor(@NotNull Vector4f borderColor){
        if(borderColor == null){
            throw new NullPointerException();
        }
        if(!Utility.isHdrColor(new Vector3f(borderColor.x, borderColor.y, borderColor.z))){
            throw new IllegalArgumentException("Border color can't be lower than 0");
        }
        this.borderColor.set(borderColor);
        float bc[] = {borderColor.x, borderColor.y, borderColor.z, borderColor.w};
        GL11.glTexParameterfv(getTextureType(), GL11.GL_TEXTURE_BORDER_COLOR, bc);

    }

    /**
     Returns the texture's specified wrap mode.

     @param type texture wrap direction

     @return the texture's specified wrap mode

     @throws NullPointerException parameter can't be null
     */
    @NotNull
    protected TextureWrap glGetWrap(@NotNull TextureWrapDirection type){
        if(type == null){
            throw new NullPointerException();
        }
        switch(type){
            case WRAP_U:
                return wrappingU;
            case WRAP_V:
                return wrappingV;
            case WRAP_W:
                return wrappingW;
        }
        return null;
    }

    /**
     Sets the texture's specified wrap mode to the given value.

     @param type  texture wrap direction
     @param value texture wrap

     @throws NullPointerException type and value can't be null
     */
    @Bind
    protected void glSetWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap value){
        if(type == null || value == null){
            throw new NullPointerException();
        }
        switch(type){
            case WRAP_U:
                wrappingU = value;
                break;
            case WRAP_V:
                wrappingV = value;
                break;
            case WRAP_W:
                wrappingW = value;
                break;
        }
        GL11.glTexParameteri(getTextureType(), type.getCode(), value.getCode());
    }

    /**
     Returns the texture's specified filter mode.

     @param type texture filter type

     @return the texture's specified filter mode

     @throws NullPointerException parameter can't be null
     */
    @NotNull
    protected TextureFilter glGetFilter(@NotNull TextureFilterType type){
        if(type == null){
            throw new NullPointerException();
        }
        if(type == TextureFilterType.MAGNIFICATION){
            return magnification;
        }else{
            return minification;
        }
    }

    /**
     Sets the texture's specified filter to the given value.

     @param type  texture filter type
     @param value texture filter

     @throws NullPointerException type and value can't be null
     */
    @Bind
    protected void glSetFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value){
        if(type == null || value == null){
            throw new NullPointerException();
        }
        if(type == TextureFilterType.MAGNIFICATION){
            magnification = value;
        }else{
            minification = value;
        }
        GL11.glTexParameteri(getTextureType(), type.getCode(), value.getCode());
    }

    /**
     Releases the texture's data.
     */
    protected void glRelease(){
        GL11.glDeleteTextures(id);
        id = 0;
    }

    /**
     Returns the texture's native OpenGL type.

     @return the texture's native OpenGL types
     */
    protected abstract int getTextureType();

    @Override
    public String toString(){
        return "AbstractTexture{" + "textureId=" + id + ", size=" + size + ", sRgb=" + sRgb + ", wrappingU=" + wrappingU + ", wrappingV=" + wrappingV + ", magnification=" + magnification + ", minification=" + minification + ", borderColor=" + borderColor + '}';
    }

}
