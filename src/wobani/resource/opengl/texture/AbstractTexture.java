package wobani.resource.opengl.texture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.lang.Math;
import java.nio.*;

/**
 Basic data and methods for implementing a texture.
 */
public abstract class AbstractTexture extends OpenGlObject implements Texture{

    /**
     Texture's id.
     */
    private int id = -1;

    private int target;

    /**
     The texture data size (in bytes).
     */
    private int dataSize;
    /**
     Number of the texture's samples.
     */
    private int samples = 1;

    private boolean allocated;

    private TextureInternalFormat internalFormat;

    private int levels = 1;
    private float anisotropicLevel = 1;

    /**
     Texture's width and height.
     */
    private final Vector2i size = new Vector2i(-1);
    /**
     Texture's border color.
     */
    private final Vector4f borderColor = new Vector4f(0);
    /**
     Determines whether the texture is in sRGB color space.
     */
    private boolean sRgb;
    /**
     Texture wrap along the U direction.
     */
    private TextureWrap wrappingU = TextureWrap.REPEAT;
    /**
     Texture wrap along the V direction.
     */
    private TextureWrap wrappingV = TextureWrap.REPEAT;
    /**
     Texture's magnification filter.
     */
    private TextureFilter magnification = TextureFilter.NEAREST;
    /**
     Texture's minification filter.
     */
    private TextureFilter minification = TextureFilter.NEAREST;

    public AbstractTexture(@NotNull ResourceId resourceId){
        super(resourceId);
    }

    protected void createTexture(int target, int samples){
        this.target = target;
        setSamples(samples);
        this.id = GL45.glCreateTextures(target);
    }

    protected void allocateImmutable(@NotNull TextureInternalFormat internalFormat, @NotNull Vector2i size, boolean mipmaps){
        setInternalFormat(internalFormat);
        setSize(size);
        allocated = true;
        if(isMultisampled()){
            GL45.glTextureStorage2DMultisample(id, samples, internalFormat.getCode(), size.x, size.y, true);
        }else{
            levels = mipmaps ? computeMaxMipmapCount(size) : 1;
            GL45.glTextureStorage2D(id, levels, internalFormat.getCode(), size.x, size.y);
        }
        //setFilterToNone();
    }

    private int computeMaxMipmapCount(@NotNull Vector2i size){
        return (int) Math.floor(Math.log(Math.max(size.x, size.y)) / Math.log(2)) + 1;
    }

    private void setSamples(int samples){
        if(samples < 1 || samples > OpenGlConstants.MAX_SAMPLES){
            throw new IllegalArgumentException();
        }
        this.samples = samples;
    }

    private void setInternalFormat(@NotNull TextureInternalFormat internalFormat){
        if(internalFormat == null){
            throw new NullPointerException();
        }
        this.internalFormat = internalFormat;
    }

    protected void setSize(@NotNull Vector2i size){
        if(size.x <= 0 || size.y <= 0 || size.x > OpenGlConstants.MAX_TEXTURE_SIZE || size.y > OpenGlConstants.MAX_TEXTURE_SIZE){
            throw new IllegalArgumentException();
        }
        this.size.set(size);
    }

    protected void store(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        if(!allocated){
            throw new IllegalStateException();
        }
        //TODO: exceptions
        GL45.glTextureSubImage2D(id, 0, offset.x, offset.y, size.x, size.y, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        GL45.glGenerateTextureMipmap(id);
    }

    public float getAnisotropicLevel(){
        return anisotropicLevel;
    }

    public void setAnisotropicLevel(int level){
        if(OpenGlConstants.ANISOTROPIC_FILTERING_ENABLED){
            anisotropicLevel = Math.min(16, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
            GL45.glTextureParameterf(getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropicLevel);
        }else{
            throw new IllegalStateException();
        }
    }

    public int getMipmapCount(){
        return levels;
    }

    protected void clear(@NotNull TextureFormat format){
        if(!allocated){
            throw new IllegalStateException();
        }
        //FIXME: not working now
        GL45.glClearTexImage(id, 0, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), new int[]{255, 0, 0, 255});
    }

    public boolean isMultisampled(){
        return samples > 1;
    }

    public int getSampleCount(){
        return samples;
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        GL45.glBindTextureUnit(textureUnit, id);
    }

    protected void setDataSize(int dataSize){
        this.dataSize = dataSize;
    }

    @Override
    public int getActiveDataSize(){
        return dataSize;
    }

    @Override
    protected int getType(){
        return GL11.GL_TEXTURE;
    }

    /**
     Binds the texture.
     */
    @Override
    public void bind(){
        GL11.glBindTexture(target, id);
    }

    /**
     Unbinds the texture.
     */
    @Override
    public void unbind(){
        GL11.glBindTexture(target, 0);
    }

    /**
     Returns the texture's border color.

     @return the texture's border color
     */
    @NotNull
    public Vector4f getBorderColor(){
        return borderColor;
    }

    /**
     Sets the texture's border color to the given value.

     @param borderColor border color

     @throws NullPointerException     borderColor can't be null
     @throws IllegalArgumentException border color can't be lower than 0
     */
    public void setBorderColor(@NotNull Vector4f borderColor){
        if(borderColor == null){
            throw new NullPointerException();
        }
        if(!Utility.isHdrColor(new Vector3f(borderColor.x, borderColor.y, borderColor.z))){
            throw new IllegalArgumentException("Border color can't be lower than 0");
        }
        this.borderColor.set(borderColor);
        float bc[] = {borderColor.x, borderColor.y, borderColor.z, borderColor.w};
        GL45.glTextureParameterfv(id, GL11.GL_TEXTURE_BORDER_COLOR, bc);
    }

    public void setFilterToNone(){
        setFilter(TextureFilterType.MAGNIFICATION, TextureFilter.NEAREST);
        setFilter(TextureFilterType.MINIFICATION, TextureFilter.NEAREST_MIPMAP_NEAREST);
    }

    public void setFilterToBilinear(){
        setFilter(TextureFilterType.MAGNIFICATION, TextureFilter.LINEAR);
        setFilter(TextureFilterType.MINIFICATION, TextureFilter.LINEAR_MIPMAP_NEAREST);
    }

    public void setFilterToTrilinear(){
        setFilter(TextureFilterType.MAGNIFICATION, TextureFilter.LINEAR);
        setFilter(TextureFilterType.MINIFICATION, TextureFilter.LINEAR_MIPMAP_LINEAR);
    }

    /**
     Returns the texture's specified wrap mode.

     @param type texture wrap direction

     @return the texture's specified wrap mode

     @throws NullPointerException parameter can't be null
     */
    @NotNull
    public TextureWrap getWrap(@NotNull TextureWrapDirection type){
        switch(type){
            case WRAP_U:
                return wrappingU;
            case WRAP_V:
                return wrappingV;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     Sets the texture's specified wrap mode to the given value.

     @param type  texture wrap direction
     @param value texture wrap

     @throws NullPointerException type and value can't be null
     */
    public void setWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap value){
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
        }
        GL45.glTextureParameteri(id, type.getCode(), value.getCode());
    }

    /**
     Returns the texture's specified filter mode.

     @param type texture filter type

     @return the texture's specified filter mode

     @throws NullPointerException parameter can't be null
     */
    @NotNull
    public TextureFilter getFilter(@NotNull TextureFilterType type){
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
    public void setFilter(@NotNull TextureFilterType type, @NotNull TextureFilter value){
        if(type == null || value == null){
            throw new NullPointerException();
        }
        if(type == TextureFilterType.MAGNIFICATION){
            magnification = value;
        }else{
            minification = value;
        }
        GL45.glTextureParameteri(id, type.getCode(), value.getCode());
    }

    @NotNull
    @ReadOnly
    @Override
    public Vector2i getSize(){
        return new Vector2i(size);
    }

    @Override
    public boolean issRgb(){
        return sRgb;
    }

    protected void setsRgb(boolean sRgb){
        this.sRgb = sRgb;
    }

    @Override
    public int getId(){
        return id;
    }

    protected void setId(int id){
        //FIXME: NO NO
        this.id = id;
    }

    /**
     Releases the texture's data.
     */
    public void release(){
        GL11.glDeleteTextures(id);
        id = -1;
        dataSize = 0;
    }

}
