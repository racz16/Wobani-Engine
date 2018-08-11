package wobani.resource.opengl.shader;

import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

import java.io.*;
import java.nio.*;
import java.util.*;

import static wobani.toolbox.EngineInfo.Library.*;

/**
 This abstract class is the base all of the shaders. It loads the shaders' source, creates the shader program, binds the
 attribute arrays and loads the uniform variables.
 */
public abstract class Shader implements Resource{

    /**
     Helps loading matrices as uniform variables.
     */
    private static final FloatBuffer temp = BufferUtils.createFloatBuffer(16);
    /**
     The shader's uniform variables.
     */
    private final Map<String, Integer> uniforms = new HashMap<>();
    /**
     The shader program's id.
     */
    private int programId;

    /**
     It creates the shader program and stores the ids of the uniform variables.

     @param vertexFile         relative path of the vertex shader's source (with extension like
     "res/shaders/myShader.glsl")
     @param fragmentFile       relative path of the fragment shader's source (with extension like
     "res/shaders/myShader.glsl")
     @param geometryFile       relative path of the geometry shader's source (with extension like
     "res/shaders/myShader.glsl")
     @param tessControlFil     relative path of the tessellation control shader's source (with extension like
     "res/shaders/myShader.glsl")
     @param tessEvaluationFile relative path of the tessellation evaluation shader's source (with extension like
     "res/shaders/myShader.glsl")
     */
    public Shader(@NotNull String vertexFile, @NotNull String fragmentFile, @Nullable String geometryFile, @Nullable String tessControlFil, @Nullable String tessEvaluationFile){
        //load, compile, check shaders
        int[] shaders = {-1, -1, -1, -1, -1};
        shaders[0] = loadShader(vertexFile, ShaderStage.VERTEX_SHADER);
        shaders[1] = loadShader(fragmentFile, ShaderStage.FRAGMENT_SHADER);
        shaders[2] = loadShader(geometryFile, ShaderStage.GEOMETRY_SHADER);
        shaders[3] = loadShader(tessControlFil, ShaderStage.TESS_CONTROL_SHADER);
        shaders[4] = loadShader(tessEvaluationFile, ShaderStage.TESS_EVALUATION_SHADER);
        //attach
        programId = GL20.glCreateProgram();
        for(int shaderId : shaders){
            if(shaderId != -1){
                GL20.glAttachShader(programId, shaderId);
            }
        }
        //link, validate
        GL20.glLinkProgram(programId);
        GL20.glValidateProgram(programId);
        //detach, delete
        for(int shaderId : shaders){
            if(shaderId != -1){
                //GL20.glDetachShader(programId, shaderId);//???
                GL20.glDeleteShader(shaderId);
            }
        }
        //uniforms
        connectUniforms();
    }

    /**
     Specifies all the locations of the shader's uniform variables.
     */
    protected abstract void connectUniforms();

    /**
     Connects the texture units to the shader's uniform variables.
     */
    protected void connectTextureUnits(){
    }

    /**
     Loads a float as a uniform variable to the shader.

     @param uniform uniform variable
     @param value   value
     */
    protected void loadFloat(@NotNull String uniform, float value){
        GL20.glUniform1f(getUniformId(uniform), value);
    }

    /**
     Loads an int as a uniform variable to the shader.

     @param uniform uniform variable
     @param value   value
     */
    protected void loadInt(@NotNull String uniform, int value){
        GL20.glUniform1i(getUniformId(uniform), value);
    }

    /**
     Loads a 2D vector as a uniform variable to the shader.

     @param uniform uniform variable
     @param vector  vector
     */
    protected void loadVector2(@NotNull String uniform, @NotNull Vector2f vector){
        GL20.glUniform2f(getUniformId(uniform), vector.x, vector.y);
    }

    /**
     Loads a 3D vector as a uniform variable to the shader.

     @param uniform uniform variable
     @param vector  vector
     */
    protected void loadVector3(@NotNull String uniform, @NotNull Vector3f vector){
        GL20.glUniform3f(getUniformId(uniform), vector.x, vector.y, vector.z);
    }

    /**
     Loads a 4D vector as a uniform variable to the shader.

     @param uniform uniform variable
     @param vector  vector
     */
    protected void loadVector4(@NotNull String uniform, @NotNull Vector4f vector){
        GL20.glUniform4f(getUniformId(uniform), vector.x, vector.y, vector.z, vector.w);
    }

    /**
     Loads a boolean as a uniform variable to the shader.

     @param uniform uniform variable
     @param value   value
     */
    protected void loadBoolean(@NotNull String uniform, boolean value){
        GL20.glUniform1f(getUniformId(uniform), value ? 1 : 0);
    }

    /**
     Loads a 4x4 matrix as a uniform variable to the shader.

     @param uniform uniform variable
     @param matrix  matrix
     */
    protected void loadMatrix4(@NotNull String uniform, @NotNull Matrix4f matrix){
        temp.position(0);
        matrix.get(temp);
        GL20.glUniformMatrix4fv(getUniformId(uniform), false, temp);
    }

    /**
     Loads a 3x3 matrix as a uniform variable to the shader.

     @param uniform uniform variable
     @param matrix  matrix
     */
    protected void loadMatrix3(@NotNull String uniform, @NotNull Matrix3f matrix){
        temp.position(0);
        matrix.get(temp);
        GL20.glUniformMatrix3fv(getUniformId(uniform), false, temp);
    }

    /**
     Connects the specified texture unit to the given uniform variable.

     @param uniform     uniform variable
     @param textureUnit texture unit (0;31)

     @throws IllegalArgumentException invalid texture unit
     */
    protected void connectTextureUnit(@NotNull String uniform, int textureUnit){
        if(textureUnit < 0 || textureUnit > 31){
            throw new IllegalArgumentException("Invalid texture unit");
        }
        GL20.glUniform1i(getUniformId(uniform), textureUnit);
    }

    /**
     Stores the specified uniform variable's id in a list and after that you can load values to this connected uniform.

     @param uniformName uniform variable

     @throws IllegalArgumentException the specified uniform variable doesn't exist
     */
    protected void connectUniform(@NotNull String uniformName){
        if(uniformName == null){
            throw new NullPointerException();
        }
        int uniformId = GL20.glGetUniformLocation(programId, uniformName);
        if(uniformId == -1){
            throw new IllegalArgumentException("There is no " + uniformName + " uniform vairable in this shader program");
        }
        uniforms.put(uniformName, uniformId);
    }

    /**
     Returns the specified uniform variable's id.

     @param uniformName uniform variable

     @return uniform variable's id

     @throws IllegalArgumentException the specified uniform variable doesn't exist
     */
    protected int getUniformId(@NotNull String uniformName){
        Integer id = uniforms.get(uniformName);
        if(id == null){
            throw new IllegalArgumentException("There is no " + uniformName + " uniform vairable in this shader program");
        }
        return id;
    }

    /**
     Starts the shader. You have to start a shader before load uniform variables or render objects.
     */
    public void start(){
        GL20.glUseProgram(programId);
        connectTextureUnits();
    }

    /**
     Stops the shader. When a shader isn't running, you can't load uniform variables or render objects.
     */
    public void stop(){
        GL20.glUseProgram(0);
    }

    /**
     Removes the shader program from the GPU's memory. After this method call you can't use this shader.
     */
    @Override
    public void release(){
        GL20.glDeleteProgram(programId);
        programId = -1;
    }

    /**
     Determines wheter this shader program is usable. If it returns false, you can't use if for anything.

     @return true if usable, false otherwise
     */
    @Override
    public boolean isUsable(){
        return programId != -1;
    }

    /**
     Loads a shader's source from the given path, creates the shader based on the given type and returns the shader's
     id.

     @param shaderFilePath relative path of the shader's source (with extension like "res/shaders/myShader.glsl")
     @param stage          shader's stage

     @return shader's id

     @throws NativeException if the shader's compilation failed
     */
    private int loadShader(@Nullable String shaderFilePath, @NotNull ShaderStage stage){
        if((stage == ShaderStage.VERTEX_SHADER && shaderFilePath == null) || (stage == ShaderStage.FRAGMENT_SHADER && shaderFilePath == null)){
            throw new NullPointerException();
        }
        if(shaderFilePath == null){
            return -1;
        }
        //getting the source code
        StringBuilder shaderSource = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(shaderFilePath))){
            while(reader.ready()){
                shaderSource.append(reader.readLine()).append("\n");
            }
        }catch(IOException ex){
            Utility.logException(ex);
        }

        //creating the shader, compiling
        int shaderId = GL20.glCreateShader(stage.getCode());
        GL20.glShaderSource(shaderId, shaderSource);
        GL20.glCompileShader(shaderId);
        if(GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE){
            int length = GL20.glGetShaderi(shaderId, GL20.GL_INFO_LOG_LENGTH);
            String errorMessage = GL20.glGetShaderInfoLog(shaderId, length);
            GL20.glDeleteShader(shaderId);
            throw new NativeException(OPENGL, shaderFilePath + "\n" + errorMessage);
        }
        return shaderId;
    }

    @Override
    public int getCachedDataSize(){
        return 0;
    }

    @Override
    public int getActiveDataSize(){
        return 0;
    }

    @Override
    public void update(){
    }

    @Override
    public String toString(){
        return "Shader{" + "programId=" + programId + ", uniforms=" + uniforms + '}';
    }

    /**
     Shader stage.
     */
    public enum ShaderStage{
        /**
         Vertex shader.
         */
        VERTEX_SHADER(GL20.GL_VERTEX_SHADER), /**
         Fragment shader.
         */
        FRAGMENT_SHADER(GL20.GL_FRAGMENT_SHADER), /**
         Geometry shader.
         */
        GEOMETRY_SHADER(GL32.GL_GEOMETRY_SHADER), /**
         Tessellation control shader.
         */
        TESS_CONTROL_SHADER(GL40.GL_TESS_CONTROL_SHADER), /**
         Tessellation evaluation shader.
         */
        TESS_EVALUATION_SHADER(GL40.GL_TESS_EVALUATION_SHADER);

        /**
         Stage's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new ShaderStage to the given value.

         @param code stage's OpenGL code
         */
        private ShaderStage(int code){
            this.code = code;
        }

        /**
         Returns the stage's OpenGL code.

         @return the stage's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

}
