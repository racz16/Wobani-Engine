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

    //TODO: to openglobject? :D
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
    private TextureFormat format;

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
    private TextureWrap wrapU = TextureWrap.REPEAT;
    /**
     Texture wrap along the V direction.
     */
    private TextureWrap wrapV = TextureWrap.REPEAT;
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
        this.id = createTextureId();
    }

    protected abstract int createTextureId();

    protected void checkCreation(){
        if(id == -1){
            throw new IllegalStateException();
        }
    }

    protected void allocateImmutable(@NotNull TextureInternalFormat internalFormat, @NotNull Vector2i size, boolean mipmaps){
        checkRelease();
        checkCreation();
        checkReallocation();
        setInternalFormat(internalFormat);
        setSize(size);
        setMipmapCount(mipmaps);
        allocated = true;
        allocateImmutableUnsafe();
    }

    private void allocateImmutableUnsafe(){
        if(isMultisampled()){
            GL45.glTextureStorage2DMultisample(id, samples, internalFormat.getCode(), size.x, size.y, true);
        }else{
            GL45.glTextureStorage2D(id, levels, internalFormat.getCode(), size.x, size.y);
        }
        setFilterToNone();
    }

    protected void checkReallocation(){
        if(allocated){
            throw new UnsupportedOperationException();
        }
    }

    private void setMipmapCount(boolean mipmaps){
        if(isMultisampled() && mipmaps){
            throw new IllegalArgumentException();
        }
        levels = mipmaps ? computeMaxMipmapCount(size) : 1;
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
        checkRelease();
        checkCreation();
        checkAllocation();
        checkSubImage(offset, size);
        setFormat(format);
        GL45.glTextureSubImage2D(id, 0, offset.x, offset.y, size.x, size.y, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        GL45.glGenerateTextureMipmap(id);
    }

    protected void checkAllocation(){
        if(!allocated){
            throw new UnsupportedOperationException();
        }
    }

    protected void checkSubImage(@NotNull Vector2i offset, @NotNull Vector2i size){
        if(offset.x < 0 || offset.y < 0 || size.x <= 0 || size.y <= 0 || offset.x + size.x > getSize().x || offset.y + size.y > getSize().y){
            throw new IllegalArgumentException();
        }
    }

    protected void setFormat(@NotNull TextureFormat format){
        if(format.getComponentCount() != internalFormat.getComponentCount() ||
                format.isDepth() && !internalFormat.isDepth() || format.isStencil() && !internalFormat.isStencil() ||
                format.isDepthStencil() && !internalFormat.isDepthStencil() || format.isColor() && !internalFormat.isColor()){
            throw new IllegalArgumentException();
        }
        this.format = format;
    }

    @Nullable
    public TextureInternalFormat getInternalFormat(){
        return internalFormat;
    }

    @Nullable
    public TextureFormat getFormat(){
        return format;
    }

    public float getAnisotropicLevel(){
        return anisotropicLevel;
    }

    public boolean isAnisotropicFilterEnabled(){
        return OpenGlConstants.ANISOTROPIC_FILTER_ENABLED;
    }

    public void setAnisotropicLevel(int level){
        if(OpenGlConstants.ANISOTROPIC_FILTER_ENABLED){
            anisotropicLevel = Math.min(level, OpenGlConstants.ANISOTROPIC_FILTER_MAX_LEVEL);
            GL45.glTextureParameterf(getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropicLevel);
        }else{
            throw new IllegalStateException();
        }
    }

    //TODO: glCopyImageSubData

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
        //FIXME: multisapled?
        //calculate based on internal format
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
        //TODO: delete?
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
                return wrapU;
            case WRAP_V:
                return wrapV;
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
                wrapU = value;
                break;
            case WRAP_V:
                wrapV = value;
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
