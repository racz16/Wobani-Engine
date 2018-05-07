package wobani.toolbox;

import wobani.toolbox.annotation.NotNull;
import wobani.toolbox.annotation.ReadOnly;
import java.io.*;
import java.nio.*;
import java.util.logging.*;
import org.joml.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import org.lwjgl.system.*;
import static org.lwjgl.system.MemoryStack.stackPush;

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
    private ByteBuffer image;
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(Image.class.getName());

    /**
     * Initializes a new Image by loading the specified image from file using
     * the STBI library.
     *
     * @param path image's relative path (with extension like
     *             "res/textures/myTexture.png")
     * @param flip true if you want to flip the image upside down, false
     *             otherwise
     */
    public Image(@NotNull File path, boolean flip) {
        loadImage(path, flip);
    }

    /**
     * Loads the specified image from file.
     *
     * @param path the image's path
     * @param flip true if you want to flip the image upside down, false
     *             otherwise
     *
     * @throws RuntimeException stbi can't load the image
     */
    private void loadImage(@NotNull File path, boolean flip) {
        stbi_set_flip_vertically_on_load(flip);
        loadImageWithoutInspection(path);
        if (image == null) {
            throw new RuntimeException("Failed to load an image file!\n" + stbi_failure_reason());
        }
        LOG.log(Level.FINE, "Image loaded");
    }

    /**
     * Loads the specified image from file.
     *
     * @param path the image's path
     */
    private void loadImageWithoutInspection(@NotNull File path) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            image = stbi_load(path.getPath(), width, height, comp, 4);
            size.set(width.get(), height.get());
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
    public ByteBuffer getData() {
        return image;
    }

    /**
     * Releases the image's data.
     */
    public void release() {
        stbi_image_free(image);
        image = null;
        LOG.log(Level.FINE, "Image released");
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("Image(")
                .append(" dimensions: ").append(size)
                .append(")");
        return res.toString();
    }

}
