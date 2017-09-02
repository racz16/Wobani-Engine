package shaders;

import resources.*;
import toolbox.annotations.*;

/**
 * This shader can render an image to a quad.
 */
public class TexturedQuadShader extends Shader {

    /**
     * The only TexturedQuadShader instance.
     */
    private static TexturedQuadShader instance;

    /**
     * Initializes a new TexturedQuadShader.
     */
    private TexturedQuadShader() {
        super("res/shaders/texturedQuad/vertexShader.glsl", "res/shaders/texturedQuad/fragmentShader.glsl", null, null, null);
        ResourceManager.addShader("." + ResourceManager.getNextId(), this);
    }

    /**
     * Returns the TexturedQuadShader instance.
     *
     * @return the TexturedQuadShader instance
     */
    @NotNull
    public static TexturedQuadShader getInstance() {
        if (instance == null || !instance.isUsable()) {
            instance = new TexturedQuadShader();
        }
        return instance;
    }

    @Override
    public void connectTextureUnits() {
        connectTextureUnit("image", 0);
    }

    @Override
    protected void connectUniforms() {
        connectUniform("image");
    }

}
