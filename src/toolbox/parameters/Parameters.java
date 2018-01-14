package toolbox.parameters;

import java.util.*;
import org.joml.*;
import resources.textures.*;
import resources.textures.cubeMapTexture.*;
import resources.textures.texture2D.*;
import toolbox.annotations.*;

public class Parameters {

    private final Map<String, Parameter<Float>> floatParameters;
    private final Map<String, Parameter<Integer>> intParameters;
    private final Map<String, Parameter<Boolean>> booleanParameters;
    private final Map<String, Parameter<Vector4f>> vectorParameters;
    private final Map<String, Parameter<Matrix4f>> matrixParameters;
    private final Map<String, Parameter<Texture2D>> textureParameters;
    private final Map<String, Parameter<CubeMapTexture>> cubeMapTextureParameters;

    public Parameters() {
        floatParameters = new HashMap<>();
        intParameters = new HashMap<>();
        booleanParameters = new HashMap<>();
        vectorParameters = new HashMap<>();
        matrixParameters = new HashMap<>();
        textureParameters = new HashMap<>();
        cubeMapTextureParameters = new HashMap<>();
    }

    private <T> void addRemove(@Nullable Parameter<T> removed, @Nullable Parameter<T> added) {
        if (removed != null) {
            removed.removedFromParameters(added);
        }
        if (added != null) {
            added.addedToParameters(removed);
        }
    }

    //
    //float---------------------------------------------------------------------
    //
    public void setFloatParameter(@NotNull String key, @Nullable Parameter<Float> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<Float> oldValue = getFloatParameter(key);
        floatParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<Float> getFloatParameter(@NotNull String key) {
        return floatParameters.get(key);
    }

    @NotNull
    public Set<String> getFloatParameterKeys() {
        return floatParameters.keySet();
    }

    //
    //int-----------------------------------------------------------------------
    //
    public void setIntParameter(@NotNull String key, @Nullable Parameter<Integer> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<Integer> oldValue = getIntParameter(key);
        intParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<Integer> getIntParameter(@NotNull String key) {
        return intParameters.get(key);
    }

    @NotNull
    public Set<String> getIntParameterKeys() {
        return intParameters.keySet();
    }

    //
    //boolean-------------------------------------------------------------------
    //
    public void setBooleanParameter(@NotNull String key, @Nullable Parameter<Boolean> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<Boolean> oldValue = getBooleanParameter(key);
        booleanParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<Boolean> getBooleanParameter(@NotNull String key) {
        return booleanParameters.get(key);
    }

    @NotNull
    public Set<String> getBooleanParameterKeys() {
        return booleanParameters.keySet();
    }

    //
    //vector--------------------------------------------------------------------
    //
    public void setVectorParameter(@NotNull String key, @Nullable Parameter<Vector4f> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<Vector4f> oldValue = getVectorParameter(key);
        vectorParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<Vector4f> getVectorParameter(@NotNull String key) {
        return vectorParameters.get(key);
    }

    @NotNull
    public Set<String> getVectorParameterKeys() {
        return vectorParameters.keySet();
    }

    //
    //matrix--------------------------------------------------------------------
    //
    public void setMatrixParameter(@NotNull String key, @Nullable Parameter<Matrix4f> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<Matrix4f> oldValue = getMatrixParameter(key);
        matrixParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<Matrix4f> getMatrixParameter(@NotNull String key) {
        return matrixParameters.get(key);
    }

    @NotNull
    public Set<String> getMatrixParameterKeys() {
        return matrixParameters.keySet();
    }

    //
    //texture-------------------------------------------------------------------
    //
    public void setTextureParameter(@NotNull String key, @Nullable Parameter<Texture2D> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<Texture2D> oldValue = getTextureParameter(key);
        textureParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<Texture2D> getTextureParameter(@NotNull String key) {
        return textureParameters.get(key);
    }

    @NotNull
    public Set<String> getTextureParameterKeys() {
        return textureParameters.keySet();
    }

    //
    //cubemap texture-----------------------------------------------------------
    //
    public void setCubeMapTextureParameter(@NotNull String key, @Nullable Parameter<CubeMapTexture> param) {
        if (key == null) {
            throw new NullPointerException();
        }
        Parameter<CubeMapTexture> oldValue = getCubeMapTextureParameter(key);
        cubeMapTextureParameters.put(key, param);
        addRemove(oldValue, param);
    }

    @Nullable
    public Parameter<CubeMapTexture> getCubeMapTextureParameter(@NotNull String key) {
        return cubeMapTextureParameters.get(key);
    }

    @NotNull
    public Set<String> getCubeMapTextureParameterKeys() {
        return cubeMapTextureParameters.keySet();
    }

    //
    //release-------------------------------------------------------------------
    //
    public void release() {
        intParameters.clear();
        floatParameters.clear();
        booleanParameters.clear();
        vectorParameters.clear();
        matrixParameters.clear();
        for (String key : getTextureParameterKeys()) {
            Parameter<Texture2D> param = getTextureParameter(key);
            releaseTexture(param);
        }
        textureParameters.clear();
        for (String key : getCubeMapTextureParameterKeys()) {
            Parameter<CubeMapTexture> param = getCubeMapTextureParameter(key);
            releaseTexture(param);
        }
        cubeMapTextureParameters.clear();
    }

    private void releaseTexture(@Nullable Parameter<? extends Texture> parameter) {
        if (parameter != null) {
            Texture texture = parameter.getValue();
            if (texture.isUsable()) {
                texture.release();
            }
        }
    }

}
