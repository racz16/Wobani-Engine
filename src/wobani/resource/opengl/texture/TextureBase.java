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
     The texture's native OpenGL target.
     */
    private int target;
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
     The texture's format.
     */
    private TextureFormat format;
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
     Texture's magnification filter.
     */
    private TextureFilter magnification = TextureFilter.NEAREST;
    /**
     Texture's minification filter.
     */
    private TextureFilter minification = TextureFilter.NEAREST;
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
     Initializes a new TextureBase to the given value.

     @param resourceId texture's unique id
     */
    public TextureBase(@NotNull ResourceId resourceId){
        super(resourceId);
    }

    protected void createTexture(int target, int samples){
        this.target = target;
        setSamples(samples);
        setId(createTextureId());
    }

    protected abstract int createTextureId();

    protected void checkCreation(){
        if(!isIdValid()){
            throw new IllegalStateException();
        }
    }

    protected void allocateImmutable2D(@NotNull TextureInternalFormat internalFormat, @NotNull Vector2i size, boolean mipmaps){
        checkRelease();
        checkCreation();
        checkReallocation();
        setInternalFormat(internalFormat);
        setSize(size);
        setMipmapCount(mipmaps);
        setActiveDataSize(computeActiveDataSize());
        allocated = true;
        allocateImmutable2DUnsafe();
    }

    private void allocateImmutable2DUnsafe(){
        if(isMultisampled()){
            GL45.glTextureStorage2DMultisample(getId(), sampleCount, internalFormat.getCode(), size.x, size.y, true);
        }else{
            GL45.glTextureStorage2D(getId(), mipmapLevelCount, internalFormat.getCode(), size.x, size.y);
        }
        setWrapAndFilterToDefault();
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
        mipmapLevelCount = mipmaps ? computeMaxMipmapCount(size) : 1;
    }

    private int computeMaxMipmapCount(@NotNull Vector2i size){
        return (int) Math.floor(Math.log(Math.max(size.x, size.y)) / Math.log(2)) + 1;
    }

    private void setSamples(int samples){
        if(samples < 1 || samples > OpenGlConstants.MAX_SAMPLES){
            throw new IllegalArgumentException();
        }
        this.sampleCount = samples;
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

    protected void store2D(@NotNull TextureFormat format, @NotNull ByteBuffer data){
        store2D(new Vector2i(0), getSize(), format, data);
    }

    protected void store2D(@NotNull Vector2i offset, @NotNull Vector2i size, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        checkRelease();
        checkCreation();
        checkAllocation();
        checkSubImage(offset, size);
        setFormat(format);
        store2DUnsafe(offset, data);
    }

    private void store2DUnsafe(@NotNull Vector2i offset, @NotNull ByteBuffer data){
        GL45.glTextureSubImage2D(getId(), 0, offset.x, offset.y, size.x, size.y, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        GL45.glGenerateTextureMipmap(getId());
    }

    protected void store3D(@NotNull Vector3i offset, @NotNull Vector2i size, int depth, @NotNull TextureFormat format, @NotNull ByteBuffer data){
        checkRelease();
        checkCreation();
        checkAllocation();
        checkSubImage(new Vector2i(offset.x, offset.y), size);
        setFormat(format);
        store3DUnsafe(offset, depth, data);
    }


    private void store3DUnsafe(@NotNull Vector3i offset, int depth, @NotNull ByteBuffer data){
        GL45.glTextureSubImage3D(getId(), 0, offset.x, offset.y, offset.z, size.x, size.y, depth, format.getCode(), TextureDataType.UNSIGNED_BYTE.getCode(), data);
        GL45.glGenerateTextureMipmap(getId());
    }


    private void setWrapAndFilterToDefault(){
        setWrap(TextureWrapDirection.WRAP_U, TextureWrap.REPEAT);
        setWrap(TextureWrapDirection.WRAP_V, TextureWrap.REPEAT);
        if(isMipmapped()){
            //setFilterToNone();
            setFilterToBilinear();
        }else{
            setFilter(TextureFilterType.MINIFICATION, TextureFilter.NEAREST);
            setFilter(TextureFilterType.MAGNIFICATION, TextureFilter.NEAREST);
        }
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
        if(format.getColorChannelCount() != internalFormat.getColorChannelCount() || format.getAttachmentSlot() != internalFormat.getAttachmentSlot()){
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
        return mipmapLevelCount;
    }

    public boolean isMipmapped(){
        return mipmapLevelCount > 1;
    }

    protected void clear(@NotNull Vector3f clearColor){
        checkRelease();
        checkCreation();
        checkAllocation();
        if(!Utility.isHdrColor(clearColor)){
            throw new IllegalArgumentException();
        }
        float[] clear = {clearColor.x, clearColor.y, clearColor.z, 1};
        GL45.glClearTexImage(getId(), 0, format.getCode(), TextureDataType.FLOAT.getCode(), clear);
    }

    public boolean isMultisampled(){
        return sampleCount > 1;
    }

    public int getSampleCount(){
        return sampleCount;
    }

    @Override
    public void bindToTextureUnit(int textureUnit){
        GL45.glBindTextureUnit(textureUnit, getId());
    }

    protected int computeActiveDataSize(){
        int pixelSizeInBits = getInternalFormat().getBitDepth() * sampleCount;
        int numberOfPixels = size.x * size.y;
        double mipmapMultiplier = isMipmapped() ? 1d / 3d : 1;
        double dataSizeInBits = pixelSizeInBits * numberOfPixels * mipmapMultiplier;
        double dataSizeInBytes = dataSizeInBits / 8;
        return (int) (dataSizeInBytes);
    }

    @Override
    protected int getType(){
        return GL11.GL_TEXTURE;
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
        GL45.glTextureParameterfv(getId(), GL11.GL_TEXTURE_BORDER_COLOR, bc);
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
        GL45.glTextureParameteri(getId(), type.getCode(), value.getCode());
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
        if((type == TextureFilterType.MAGNIFICATION || !isMipmapped()) && value != TextureFilter.NEAREST && value != TextureFilter.LINEAR){
            throw new IllegalArgumentException();
        }
        setFilterUnsafe(type, value);
    }

    private void setFilterUnsafe(@NotNull TextureFilterType type, @NotNull TextureFilter value){
        if(type == TextureFilterType.MAGNIFICATION){
            magnification = value;
        }else{
            minification = value;
        }
        GL45.glTextureParameteri(getId(), type.getCode(), value.getCode());
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
        return super.getId();
    }

    /**
     Releases the texture's data.
     */
    public void release(){
        GL11.glDeleteTextures(getId());
        setIdToInvalid();
        setActiveDataSize(0);
    }

    @Override
    public String toString(){
        return super.toString() + "\n" +
                TextureBase.class.getSimpleName() + "(" +
                "target: " + target + ", " +
                "sampleCount: " + sampleCount + ", " +
                "allocated: " + allocated + ", " +
                "internalFormat: " + internalFormat + ", " +
                "format: " + format + ", " +
                "mipmapLevelCount: " + mipmapLevelCount + ", " +
                "anisotropicLevel: " + anisotropicLevel + ", " +
                "size: " + size + ", " +
                "borderColor: " + borderColor + ", " +
                "sRgb: " + sRgb + ", " +
                "wrapU: " + wrapU + ", " +
                "wrapV: " + wrapV + ", " +
                "magnification: " + magnification + ", " +
                "minification: " + minification + ")";
    }
}
