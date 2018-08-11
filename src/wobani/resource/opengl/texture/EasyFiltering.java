package wobani.resource.opengl.texture;

/**
 Easy filtering settings to textures.
 */
public interface EasyFiltering extends Texture{

    /**
     Returns the texture's filtering.

     @return the texture's filtering mode
     */
    public TextureFiltering getTextureFiltering();

    /**
     Sets the texture's filtering to the given value.

     @param tf texture's filtering mode
     */
    public void setTextureFiltering(TextureFiltering tf);

    /**
     Texture filtering modes.
     */
    public enum TextureFiltering{

        /**
         No texture filtering.
         */
        NONE(0), /**
         Bilinear texture filtering.
         */
        BILINEAR(1), /**
         Trilinear texture filtering.
         */
        TRILINEAR(2), /**
         2x anisotropic texture filtering.
         */
        ANISOTROPIC_2X(3), /**
         4x anisotropic texture filtering.
         */
        ANISOTROPIC_4X(4), /**
         8x anisotropic texture filtering.
         */
        ANISOTROPIC_8X(5), /**
         16x anisotropic texture filtering.
         */
        ANISOTROPIC_16X(6);

        /**
         Filtering type's index.
         */
        private final int index;

        /**
         Initializes a new TextureFiltering to the given value.

         @param index index
         */
        TextureFiltering(int index){
            this.index = index;
        }

        /**
         Returns the filtering type's index.

         @return index
         */
        public int getIndex(){
            return index;
        }
    }

}
