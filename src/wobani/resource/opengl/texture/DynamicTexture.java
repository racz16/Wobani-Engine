package wobani.resource.opengl.texture;

import org.joml.*;
import wobani.toolbox.annotation.*;

public abstract class DynamicTexture extends AbstractTexture{

    /**
     The texture data size (in bytes).
     */
    protected int dataSize;

    //
    //texture wrapping----------------------------------------------------------
    //

    /**
     Returns the texture's specified wrap mode.

     @param type texture wrap direction

     @return the texture's specified wrap mode
     */
    @NotNull
    public TextureWrap getTextureWrap(@NotNull TextureWrapDirection type){
        return glGetWrap(type);
    }

    /**
     Sets the texture's specified wrap mode to the given value.

     @param type texture wrap direction
     @param tw   texture wrap
     */
    @Bind
    public void setTextureWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap tw){
        glSetWrap(type, tw);
    }

    /**
     Returns the texture's specified filter mode.

     @param type texture filter type

     @return the texture's specified filter mode
     */
    @NotNull
    public TextureFilter getFilter(@NotNull TextureFilterType type){
        return glGetFilter(type);
    }

    /**
     Sets the texture's specified filter to the given value.

     @param type  texture filter type
     @param value texture filter
     */
    @Bind
    public void setFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value){
        glSetFilter(type, value);
    }

    /**
     Returns the texture's border color.

     @return the texture's border color
     */
    @NotNull
    @ReadOnly
    public Vector4f getBorderColor(){
        return new Vector4f(glGetBorderColor());
    }

    /**
     Sets the texture's border color to the given value.

     @param borderColor border color
     */
    @Bind
    public void setBorderColor(@NotNull Vector4f borderColor){
        glSetBorderColor(borderColor);
    }

    @Override
    public void bind(){
        glBind();
    }

    //TODO: to protected
    public int getId(){
        return glGetId();
    }

    @Override
    public void unbind(){
        glUnbind();
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        glActivate(textureUnit);
        glBind();
    }

    @Override
    public boolean issRgb(){
        return false;
    }

    @Override
    public int getCachedDataSize(){
        return 0;
    }

    @Override
    public int getActiveDataSize(){
        return dataSize;
    }

    /**
     Releases the texture's data. After calling this method, you can't use this texture for anything.
     */
    @Override
    public void release(){
        glRelease();
        dataSize = 0;
    }

    @Override
    public boolean isUsable(){
        return getId() != 0;
    }

}
