package toolbox;

import java.io.*;
import java.nio.*;
import org.joml.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import org.lwjgl.system.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import toolbox.annotations.*;

/**
 * Simple class for loading images from file using the STBI library.
 */
public class Image {

    /**
     * Image's size.
     */
    private final Vector2i size = new Vector2i();
    /**
     * Image's data.
     */
    private final ByteBuffer image;

    /**
     * Initializes a new Image by loading the specified image from file using
     * the STBI library.
     *
     * @param path image's relative path (with extension like
     * "res/textures/myTexture.png")
     * @param flip true if you want to flip the image upside down, false
     * otherwise
     *
     * @throws RuntimeException stbi can't load the image
     */
    public Image(@NotNull File path, boolean flip) {
        stbi_set_flip_vertically_on_load(flip);
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            image = stbi_load(path.getPath(), w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load an image file!\n" + stbi_failure_reason());
            }
            size.set(w.get(), h.get());
        }
    }

    /**
     * Returns the image's size.
     *
     * @return image's size
     */
    @NotNull @ReadOnly
    public Vector2i getSize() {
        return new Vector2i(size);
    }

    /**
     * Returns the image's data.
     *
     * @return image's data
     */
    @NotNull
    public ByteBuffer getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Image{" + "size=" + size + ", image=" + image + '}';
    }

}
