package toolbox;

import java.io.*;
import java.nio.*;
import org.joml.*;
import org.lwjgl.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
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
     *
     * @throws IllegalArgumentException if the file doesn't exists
     * @throws RuntimeException stbi can't load the image
     */
    public Image(@NotNull String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException(path + " file doesn't exist");
        }
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        image = stbi_load(path, w, h, comp, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load an image file!\n" + stbi_failure_reason());
        }
        size.set(w.get(), h.get());
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
