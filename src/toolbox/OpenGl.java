package toolbox;

import org.joml.*;
import org.lwjgl.opengl.*;
import toolbox.annotations.*;

/**
 * Set of commonly used OpenGL functions.
 */
public class OpenGl {

    /**
     * Face culling mode.
     */
    public enum FaceCullingMode {
        /**
         * Front.
         */
        FRONT(GL11.GL_FRONT),
        /**
         * Back.
         */
        BACK(GL11.GL_BACK),
        /**
         * Front and back.
         */
        FRONT_AND_BACK(GL11.GL_FRONT_AND_BACK);

        /**
         * Face culling mode's OpenGL code.
         */
        private final int openGlCode;

        /**
         * Initializes a new FaceCullingMode to the given value.
         *
         * @param code face culling mode's OpenGL code
         */
        private FaceCullingMode(int code) {
            this.openGlCode = code;
        }

        /**
         * Returns the face culling mode's OpenGL code.
         *
         * @return the face culling mode's OpenGL code
         */
        public int getOpenGlCode() {
            return openGlCode;
        }
    }

    /**
     * To can't create OpenGl instance.
     */
    private OpenGl() {
    }

    /**
     * Sets whether or not enable the multisampling.
     *
     * @param multisample true if you would like to enable multisampling, false
     * otheriwse
     */
    public static void setMultisample(boolean multisample) {
        if (multisample) {
            GL11.glEnable(GL13.GL_MULTISAMPLE);
        } else {
            GL11.glDisable(GL13.GL_MULTISAMPLE);
        }
    }

    /**
     * Sets whether or not enable the depth testing.
     *
     * @param depthTest true if you would like to enable depth testing, false
     * otheriwse
     */
    public static void setDepthTest(boolean depthTest) {
        if (depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
    }

    /**
     * Sets whether or not enable the alpha blending (alpha - (1-alpa)).
     *
     * @param alphaBlending true if you would like to enable alpha blending,
     * false otheriwse
     */
    public static void setAlphaBlending(boolean alphaBlending) {
        if (alphaBlending) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    /**
     * Sets whether or not enable the wireframe rendering mode.
     *
     * @param wireframeMode true if you would like to enable wireframe mode,
     * false otheriwse
     */
    public static void setWireframe(boolean wireframeMode) {
        if (wireframeMode) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        } else {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }

    /**
     * Sets the rendering viewport to the given values.
     *
     * @param size rendering width and height
     * @param offset rendering offset
     *
     * @throws IllegalArgumentException width and height must be positive
     */
    public static void setViewport(@NotNull Vector2i size, @NotNull Vector2i offset) {
        if (size.x <= 0 || size.y <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        GL11.glViewport(offset.x, offset.y, size.x, size.y);
    }

    /**
     * Sets whether or not enable the face culling.
     *
     * @param faceCulling true if you would like to enable face culling, false
     * otheriwse
     */
    public static void setFaceCulling(boolean faceCulling) {
        if (faceCulling) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    /**
     * Sets the face culling mode to the given value.
     *
     * @param faceCulling face culling mode
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void setFaceCullingMode(@NotNull FaceCullingMode faceCulling) {
        if (faceCulling == null) {
            throw new NullPointerException();
        }
        GL11.glCullFace(faceCulling.getOpenGlCode());
    }

    /**
     * Unbinds the FBOs and binds the default frambuffer.
     */
    public static void bindDefaultFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     * Sets the clear color to the given value.
     *
     * @param clearColor clear color
     *
     * @throws IllegalArgumentException environtment color can't be lower than 0
     */
    public static void setClearColor(@NotNull Vector4f clearColor) {
        if (!Utility.isHdrColor(new Vector3f(clearColor.x, clearColor.y, clearColor.z))) {
            throw new IllegalArgumentException("Environtment color can't be lower than 0");
        }
        GL11.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
    }

    /**
     * Clears the currently bound FBO's specified attachmets.
     *
     * @param color color attachments
     * @param depth depth attachment
     * @param stencil stencil attachment
     */
    public static void clear(boolean color, boolean depth, boolean stencil) {
        int colorBit = color ? GL11.GL_COLOR_BUFFER_BIT : 0;
        int depthBit = depth ? GL11.GL_DEPTH_BUFFER_BIT : 0;
        int stencilBit = stencil ? GL11.GL_STENCIL_BUFFER_BIT : 0;
        GL11.glClear(colorBit | depthBit | stencilBit);
    }
}
