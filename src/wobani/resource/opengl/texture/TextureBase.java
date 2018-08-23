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
public abstract class TextureBase extends OpenGlObject implements Texture{
    /**
     Texture's width and height.
     */
    private final Vector2i size = new Vector2i(0);
    /**
     Determines whether the texture's data is allocated.
     */
    private boolean allocated;
    /**
     The texture's internal format.
     */
    private TextureInternalFormat internalFormat;
    /**
     The given data's format.
     */
    private TextureFormat format;
    /**
     Determines whether the texture is multisampled.
     */
    private boolean multisampled;
    /**
     Number of the texture's samples. Used for multisampling.
     */
    private int sampleCount = 1;
    /**
     Number of the texture's mipmap levels.
     */
    private int mipmapLevelCount = 1;
    /**
     The level of the texture's anisotropic filter.
     */
    private float anisotropicLevel = 1;
    /**
     Determines whether the texture is in sRGB color space.
     */
    private boolean sRgb;
    /**
     Texture's filter.
     */
    private TextureFilter filter = TextureFilter.NONE;
    /**
     Textures' default filter.
     */
    private static TextureFilter defaultFilter = TextureFilter.NONE;
    /**
     Texture wrap along the U direction.
     */
    private TextureWrap wrapU = TextureWrap.REPEAT;
    /**
     Texture wrap along the V direction.
     */
    private TextureWrap wrapV = TextureWrap.REPEAT;
    /**
     Texture's border color.
     */
    private final Vector4f borderColor = new Vector4f(0);

    /**
     Initializes a new TextureBase to the given values.

     @param resourceId   texture's unique id
     @param multisampled true if the texture should be multisampled, false otherwise
     */
    public TextureBase(@NotNull ResourceId resourceId, boolean multisampled){
        super(resourceId);
        setMultisampled(multisampled);
        createTextureId();
    }

    /**
     Creates a new native OpenGL texture id to the texture.
     */
    protected abstract void createTextureId();

    //
    //allocate----------------------------------------------------------------------------------------------------------
    //

    /**
     Checks whether the allocation is possible and stores the given parameters.

     @param internalFormat the texture's internal foramt
     @param size           the texture's width and height
     */
    protected void allocationGeneral(@NotNull TextureInternalFormat internalFormat, @NotNull Vector2i size){
        checkCreation();
        checkReallocation();
        setInternalFormat(internalFormat);
        setSize(size);
        allocated = true;
    }

    /**
     Allocates memory for the texture based on the given parameters. Note that after calling this method, you can't
     allocate the texture again.

     @param internalFormat texture's internal format
     @param size           texture's width, and height
     @param mipmaps        true if texture should use mipmaps, false otherwise (must be false if multisample the
     texture)

     @see #isMultisampled()
     */
    protected void allocateImmutable2D(@NotNull TextureInternalFormat internalFormat, @NotNull Vector2i size, boolean mipmaps){
        allocationGeneral(internalFormat, size);
        setMipmapCount(mipmaps);
        allocateImmutable2DUnsafe();
    }

    /**
     Allocates memory for the texture based on the given parameters. Note that after calling this method, you can't
     allocate the texture again.

     @param internalFormat texture's internal format
     @param size           texture's width, and height
     @param sampleCount    number of samples (must be 1, if not multisample the texture)

     @see #isMultisampled()
     */
    protected void allocateImmutable2D(@NotNull TextureInternalFormat internalFormat, @NotNull Vector2i size, int sampleCount){
        allocationGeneral(internalFormat, size);
        setSamples(sampleCount);
        allocateImmutable2DUnsafe();
    }

    /**
     Allocates memory for the texture.
     */
    private void allocateImmutable2DUnsafe(){
        setActiveDataSize(computeActiveDataSize());
        if(isMultisampled()){
            GL45.glTextureStorage2DMultisample(getId(), sampleCount, internalFormat.getCode(), size.x, size.y, true);
        }else{
            GL45.glTextureStorage2D(getId(), mipmapLevelCount, internalFormat.getCode(), size.x, size.y);
        }
        initializeAfterAllocation();
    }

    /**
     Called after you allocate memory for the texture. It initializes some specific parts of the texture like wrap or
     filter.
     */
    protected abstract void initializeAfterAllocation();

    /**
     Checks whether the texture is created.

     @throws IllegalStateException if texture is not yet created
     */
    protected void checkCreation(){
        if(!isIdValid()){
            throw new IllegalStateException("Texture is not yet created");
        }
    }

    /**
     Checks whether the texture is already allocated.

     @throws IllegalStateException if the texture is already allocated
     */
    protected void checkReallocation(){
        if(allocated){
            throw new IllegalStateException("The texture is already allocated");
        }
    }

    /**
     Returns the texture's data size based on it's size, internal format, number of mipmap levels and number of samples.

     @return the texture's data size (in bytes)
     */
    protected int computeActiveDataSize(){
        int pixelSizeInBits = getInternalFormat().getBitDepth() * sampleCount;
        int numberOfPixels = size.x * size.y;
        double mipmapMultiplier = isMipmapped() ? 1d / 3d : 1;
        double dataSizeInBits = pixelSizeInBits * numberOfPixels * mipmapMultiplier;
        double dataSizeInBytes = dataSizeInBits / 8;
        return (int) (dataSizeInBytes);
    }

    //
    //store-------------------------------------------------------------------------------------------------------------
    //

    /**
     Stores the given data in the texture and generates the mipmaps.

     @param format the given data's format
     @param data   data to store
     */
    protected void store(@NotNull TextureFormat format, @NotNull ByteBuffer data){
        store(new Vector2i(0), getSize(), format, data);
    }

    /**
     Stores the given data in the texture and generates the mipmaps.

     @param offset data's offset
     @param size   data's width and height
     @param format the given data's format
     @param data   data to store
     */
    protected void store(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        checkCreation();
        checkAllocation();
        checkSubImage(offset, size);
        setFormat(format);
        storeUnsafe(offset, data);
    }

    /**
     Stores the given data in the texture and generates the mipmaps.

     @param offset data's offset
     @param data   data to store
     */
    private void storeUnsafe(@NotNull Vector2i offset, @NotNull ByteBuffer data){
        GL45.glTextureSubImage2D(getId(), 0, offset.x, offset.y, size.x, size.y, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        GL45.glGenerateTextureMipmap(getId());
    }

    /**
     Checks whether the texture is allocated.

     @throws IllegalStateException if the texture is not yet allocated
     */
    protected void checkAllocation(){
        if(!allocated){
            throw new IllegalStateException("The texture is not yet allocated");
        }
    }

    /**
     Checks whether the data with the given size and offset exceeds from the texture's allocated memory.

     @param offset data's offset
     @param size   data's width and height

     @throws IllegalArgumentException if the data with the given offset and size exceeds from the texture's allocated
     memory
     */
    protected void checkSubImage(@NotNull Vector2i offset, @NotNull Vector2i size){
        if(offset.x < 0 || offset.y < 0 || size.x <= 0 || size.y <= 0 || offset.x + size.x > getSize().x || offset.y + size.y > getSize().y){
            throw new IllegalArgumentException("The data with the given offset and size exceeds from the texture's allocated memory");
        }
    }

    /**
     Saves the given data's format.

     @param format the given data's format

     @throws IllegalArgumentException if the format isn't compatible with the internal format
     */
    protected void setFormat(@NotNull TextureFormat format){
        if(format.getColorChannelCount() != internalFormat.getColorChannelCount() ||
                format.getAttachmentSlot() != internalFormat.getAttachmentSlot()){
            throw new IllegalArgumentException("The format isn't compatible with the internal format");
        }
        this.format = format;
    }

    /***
     Fills the entire texture with the given color.

     @param clearColor clean color

     @throws IllegalArgumentException if the parameter is not a color
     */
    protected void clear(@NotNull Vector3f clearColor){
        checkCreation();
        checkAllocation();
        if(!Utility.isHdrColor(clearColor)){
            throw new IllegalArgumentException("The parameter clearColor is not a color");
        }
        float[] clear = {clearColor.x, clearColor.y, clearColor.z, 1};
        GL45.glClearTexImage(getId(), 0, format.getCode(), TextureDataType.FLOAT.getCode(), clear);
    }

    //
    //multisample-------------------------------------------------------------------------------------------------------
    //

    /**
     Determines whether this texture is multisampled.

     @return true if this texture is multisampled, false otherwise
     */
    public boolean isMultisampled(){
        return multisampled;
    }

    /**
     Sets whether or not the texture is multisampled.

     @param multisampled true if the texture should be multisampled, false otherwise
     */
    private void setMultisampled(boolean multisampled){
        this.multisampled = multisampled;
    }

    /**
     Returns the number of samples in this texture.

     @return the number of samples in this texture
     */
    public int getSampleCount(){
        return sampleCount;
    }

    /**
     Sets this texture's sample count.

     @param samples number of samples in a single pixel

     @throws IllegalArgumentException if samples are lower than 1 or higher than the maximum sample count, or if samples
     are higher than 1 but this texture isn't multisampled
     @see OpenGlConstants#MAX_SAMPLES
     @see #isMultisampled()
     */
    private void setSamples(int samples){
        if(samples < 1 || samples > OpenGlConstants.MAX_SAMPLES){
            throw new IllegalArgumentException("Samples are lower than 1 or higher than the maximum sample count");
        }
        if(samples > 1 && !isMultisampled()){
            throw new IllegalArgumentException("Samples are higher than 1 but this texture isn't multisampled");
        }
        this.sampleCount = samples;
    }

    //
    //mipmap------------------------------------------------------------------------------------------------------------
    //

    /**
     Determines whether this texture is mipmapped.

     @return true if this texture is mipmapped, false otherwise
     */
    public boolean isMipmapped(){
        return mipmapLevelCount > 1;
    }

    /**
     Returns the number of mipmap levels in this texture.

     @return the number of mipmap levels in this texture
     */
    public int getMipmapCount(){
        return mipmapLevelCount;
    }

    /**
     Sets whether or not the texture use mipmaps.

     @param mipmaps true if this texture should use mipmaps, false otherwise

     @throws IllegalArgumentException if you try to use mipmaps with multisampling
     */
    private void setMipmapCount(boolean mipmaps){
        if(isMultisampled() && mipmaps){
            throw new IllegalArgumentException("You try to use mipmaps with multisampling");
        }
        mipmapLevelCount = mipmaps ? computeMaxMipmapCount() : 1;
    }

    /**
     Computes the maximum number of mipmap levels based on the texture's size.

     @return the maximum number of mipmap levels
     */
    private int computeMaxMipmapCount(){
        return (int) Math.floor(Math.log(Math.max(size.x, size.y)) / Math.log(2)) + 1;
    }

    /**
     Returns the texture's anisotropic filter level.

     @return the texture's anisotropic filter level
     */
    public float getAnisotropicLevel(){
        return anisotropicLevel;
    }

    /**
     Determines whether the anisotropic filter is enabled in your GPU. If it's not, you shouldn't try to use {@link
    #setAnisotropicLevel(int)} method, because you get an exception.

     @return true if the anisotropic filter is enabled in your GPU, false otherwise
     */
    public static boolean isAnisotropicFilterEnabled(){
        return OpenGlConstants.ANISOTROPIC_FILTER_ENABLED;
    }

    /**
     Sets the anisotropic filter's level to the given value. Typical values are 2, 4, 8 and 16. By passing 1 to this
     method, you basically switch off the effect.

     @param level anisotropic filter's level

     @throws IllegalArgumentException if anisotropic filter is not enabled in your GPU or if the parameter is lower than
     1
     */
    public void setAnisotropicLevel(int level){
        if(!isAnisotropicFilterEnabled()){
            throw new IllegalStateException("Anisotropic filter is not enabled in your GPU");
        }
        if(level < 1){
            throw new IllegalArgumentException("Anisotropic filter's level is lower than 1");
        }
        anisotropicLevel = Math.min(level, OpenGlConstants.ANISOTROPIC_FILTER_MAX_LEVEL);
        GL45.glTextureParameterf(getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotropicLevel);
    }

    //
    //filter------------------------------------------------------------------------------------------------------------
    //

    /**
     Returns the texture's filter.

     @return the texture's filter
     */
    @NotNull
    public TextureFilter getFilter(){
        return filter;
    }

    /**
     Sets the texture's filter to the given value.

     @param filter texture filter
     */
    public void setFilter(@NotNull TextureFilter filter){
        GL45.glTextureParameteri(getId(), GL11.GL_TEXTURE_MAG_FILTER, filter.getMagnificationCode());
        GL45.glTextureParameteri(getId(), GL11.GL_TEXTURE_MIN_FILTER, filter.getMinificationCode());
        this.filter = filter;
    }

    /**
     Returns the textures' default filter

     @return the textures' default filter
     */
    @NotNull
    public static TextureFilter getDefaultFilter(){
        return defaultFilter;
    }

    /**
     Sets the textures' default filter to the give value.

     @param filter the textures' default filter

     @throws NullPointerException if filter is null
     */
    public static void setDefaultFilter(@NotNull TextureFilter filter){
        if(filter == null){
            throw new NullPointerException();
        }
        defaultFilter = filter;
    }

    //
    //wrap--------------------------------------------------------------------------------------------------------------
    //

    /**
     Returns the texture's specified wrap mode.

     @param type texture wrap direction

     @return the texture's specified wrap mode

     @throws NullPointerException if type is null
     */
    @NotNull
    public TextureWrap getWrap(@NotNull TextureWrapDirection type){
        switch(type){
            case WRAP_U:
                return wrapU;
            case WRAP_V:
                return wrapV;
            default:
                throw new NullPointerException();
        }
    }

    /**
     Sets the texture's specified wrap mode to the given value.

     @param type  texture wrap direction
     @param value texture wrap

     @throws NullPointerException if type or value is null
     */
    public void setWrap(@NotNull TextureWrapDirection type, @NotNull TextureWrap value){
        if(type == null || value == null){
            throw new NullPointerException();
        }
        setWrapUnsafe(type, value);
    }

    /**
     Sets the texture's specified wrap mode to the given value.

     @param type  texture wrap direction
     @param value texture wrap
     */
    private void setWrapUnsafe(@NotNull TextureWrapDirection type, @NotNull TextureWrap value){
        switch(type){
            case WRAP_U:
                wrapU = value;
                break;
            case WRAP_V:
                wrapV = value;
                break;
        }
        GL45.glTextureParameteri(getId(), type.getCode(), value.getCode());
    }

    /**
     Returns the texture's border color.

     @return the texture's border color
     */
    @ReadOnly
    @NotNull
    public Vector4f getBorderColor(){
        return new Vector4f(borderColor);
    }

    /**
     Sets the texture's border color to the given value.

     @param borderColor border color

     @throws IllegalArgumentException if border color is not a color
     */
    public void setBorderColor(@NotNull Vector4f borderColor){
        if(!Utility.isHdrColor(new Vector3f(borderColor.x, borderColor.y, borderColor.z))){
            throw new IllegalArgumentException("Border color is not a color");
        }
        this.borderColor.set(borderColor);
        GL45.glTextureParameterfv(getId(), GL11.GL_TEXTURE_BORDER_COLOR, new float[]{borderColor.x, borderColor.y, borderColor.z, borderColor.w});
    }

    //
    //misc--------------------------------------------------------------------------------------------------------------
    //

    /**
     Returns the texture's internal format.

     @return the texture's internal format
     */
    @Nullable
    public TextureInternalFormat getInternalFormat(){
        return internalFormat;
    }

    /**
     Sets the texture's internal format to the given value.

     @param internalFormat internal format

     @throws NullPointerException if internal format is null
     */
    private void setInternalFormat(@NotNull TextureInternalFormat internalFormat){
        if(internalFormat == null){
            throw new NullPointerException();
        }
        this.internalFormat = internalFormat;
    }

    @NotNull
    @ReadOnly
    @Override
    public Vector2i getSize(){
        return new Vector2i(size);
    }

    /**
     Sets the texture's size to the given value.

     @param size texture's width and height

     @throws IllegalArgumentException if texture size is negative or higher than the maximum size
     @see OpenGlConstants#MAX_TEXTURE_SIZE
     */
    protected void setSize(@NotNull Vector2i size){
        if(size.x <= 0 || size.y <= 0 || size.x > OpenGlConstants.MAX_TEXTURE_SIZE || size.y > OpenGlConstants.MAX_TEXTURE_SIZE){
            throw new IllegalArgumentException("Texture size is negative or higher than the maximum size");
        }
        this.size.set(size);
    }

    @Override
    public boolean issRgb(){
        return sRgb;
    }

    /**
     Sets whether or not the texture's color space is sRGB.

     @param sRgb true is the texture is in sRGB color space, false otherwise
     */
    protected void setsRgb(boolean sRgb){
        this.sRgb = sRgb;
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        if(textureUnit < 0 || textureUnit > 31){
            throw new IllegalArgumentException("Texture unit is outside the (0;31) interval");
        }
        GL45.glBindTextureUnit(textureUnit, getId());
    }

    @Override
    protected int getType(){
        return GL11.GL_TEXTURE;
    }

    @Override
    public int getId(){
        return super.getId();
    }

    @Override
    public void release(){
        GL11.glDeleteTextures(getId());
        setIdToInvalid();
        setActiveDataSize(0);
        allocated = false;
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                TextureBase.class.getSimpleName() + "(" +
                "size: " + size + ", " +
                "allocated: " + allocated + ", " +
                "internalFormat: " + internalFormat + ", " +
                "format: " + format + ", " +
                "multisampled: " + multisampled + ", " +
                "sampleCount: " + sampleCount + ", " +
                "mipmapLevelCount: " + mipmapLevelCount + ", " +
                "anisotropicLevel: " + anisotropicLevel + ", " +
                "sRgb: " + sRgb + ", " +
                "filter: " + filter + ", " +
                "wrapU: " + wrapU + ", " +
                "wrapV: " + wrapV + ", " +
                "borderColor: " + borderColor + ")";
    }
}
