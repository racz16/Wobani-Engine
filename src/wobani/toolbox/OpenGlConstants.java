package wobani.toolbox;

import org.lwjgl.opengl.*;

/**
 Collects useful OpenGL constants.
 */
public class OpenGlConstants{

    //general
    public static final int MAJOR_VERSION;
    public static final int MINOR_VERSION;
    public static final String VENDOR;
    public static final String RENDERER;
    public static final int MAX_LABEL_LENGTH;
    public static final int MAX_LABEL_LENGTH_SAFE = 256;
    //buffer
    public static final int MAX_UNIFORM_BUFFER_BINDINGS;
    public static final int MAX_UNIFORM_BUFFER_BINDINGS_SAFE = 84;
    public static final int MAX_SHADER_STORAGE_BUFFER_BINDINGS;
    public static final int MAX_SHADER_STORAGE_BUFFER_BINDINGS_SAFE = 8;
    public static final int MAX_VERTEX_ATTRIBS;
    public static final int MAX_VERTEX_ATTRIBS_SAFE = 16;
    public static final int MAX_UNIFORM_BLOCK_SIZE;
    public static final int MAX_UNIFORM_BLOCK_SIZE_SAFE = 16384;
    public static final int MAX_SHADER_STORAGE_BLOCK_SIZE;
    public static final int MAX_SHADER_STORAGE_BLOCK_SIZE_SAFE = 134217728;
    //texture, RBO
    public static final int MAX_TEXTURE_SIZE;
    public static final int MAX_TEXTURE_SIZE_SAFE = 16384;
    public static final int MAX_SAMPLES;
    public static final int MAX_SAMPLES_SAFE = 4;
    public static final boolean ANISOTROPIC_FILTER_ENABLED;
    public static final float MAX_ANISOTROPIC_FILTER_LEVEL;
    public static final float MAX_ANISOTROPIC_FILTER_LEVEL_SAFE = 16;
    public static final int MAX_RENDERBUFFER_SIZE;
    public static final int MAX_RENDERBUFFER_SIZE_SAFE = 16384;
    //FBO
    public static final int MAX_FRAMEBUFFER_WIDTH;
    public static final int MAX_FRAMEBUFFER_WIDTH_SAFE = 16384;
    public static final int MAX_FRAMEBUFFER_HEIGHT;
    public static final int MAX_FRAMEBUFFER_HEIGHT_SAFE = 16384;
    public static final int MAX_FRAMEBUFFER_SAMPLES;
    public static final int MAX_FRAMEBUFFER_SAMPLES_SAFE = 4;
    public static final int MAX_COLOR_ATTACHMENTS;
    public static final int MAX_COLOR_ATTACHMENTS_SAFE = 8;
    public static final int MAX_DRAW_BUFFERS;
    public static final int MAX_DRAW_BUFFERS_SAFE = 8;

    static{
        //general
        MAJOR_VERSION = GL11.glGetInteger(GL30.GL_MAJOR_VERSION);
        MINOR_VERSION = GL11.glGetInteger(GL30.GL_MINOR_VERSION);
        VENDOR = GL11.glGetString(GL11.GL_VENDOR);
        RENDERER = GL11.glGetString(GL11.GL_RENDERER);
        MAX_LABEL_LENGTH = GL11.glGetInteger(GL43.GL_MAX_LABEL_LENGTH);
        //buffer
        MAX_UNIFORM_BUFFER_BINDINGS = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS);
        MAX_SHADER_STORAGE_BUFFER_BINDINGS = GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);
        MAX_VERTEX_ATTRIBS = GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
        MAX_UNIFORM_BLOCK_SIZE = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BLOCK_SIZE);
        MAX_SHADER_STORAGE_BLOCK_SIZE = GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
        //texture, RBO
        MAX_SAMPLES = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
        MAX_TEXTURE_SIZE = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        MAX_RENDERBUFFER_SIZE = GL11.glGetInteger(GL30.GL_MAX_RENDERBUFFER_SIZE);
        ANISOTROPIC_FILTER_ENABLED = GL.getCapabilities().GL_EXT_texture_filter_anisotropic;
        MAX_ANISOTROPIC_FILTER_LEVEL = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
        //FBO
        MAX_FRAMEBUFFER_WIDTH = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_WIDTH);
        MAX_FRAMEBUFFER_HEIGHT = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_HEIGHT);
        MAX_FRAMEBUFFER_SAMPLES = GL11.glGetInteger(GL43.GL_MAX_FRAMEBUFFER_SAMPLES);
        MAX_COLOR_ATTACHMENTS = GL11.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS);
        MAX_DRAW_BUFFERS = GL11.glGetInteger(GL30.GL_MAX_DRAW_BUFFERS);
    }
}
