package wobani.toolbox;

import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import wobani.toolbox.OpenGlEvent.*;
import wobani.toolbox.annotation.*;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

/**
 Set of commonly used OpenGL functions.
 */
public class OpenGl{

    /**
     Blend func factor.
     */
    public enum BlendFuncFactor{
        /**
         0
         */
        ZERO(GL11.GL_ZERO), /**
         1
         */
        ONE(GL11.GL_ONE), /**
         Source color
         */
        SRC_COLOR(GL11.GL_SRC_COLOR), /**
         1 - source color
         */
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR), /**
         Destination color
         */
        DST_COLOR(GL11.GL_DST_COLOR), /**
         1 - destination color
         */
        ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR), /**
         Source alpha
         */
        SRC_ALPHA(GL11.GL_SRC_ALPHA), /**
         1 - source alpha
         */
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA), /**
         Destination alpha
         */
        DST_ALPHA(GL11.GL_DST_ALPHA), /**
         1 - destination alpha
         */
        ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA), /**
         Constant color
         */
        CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR), /**
         1 - constant color
         */
        ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR), /**
         Constant alpha
         */
        CONSTANT_ALPHA(GL14.GL_CONSTANT_ALPHA), /**
         1 - constant alpha
         */
        ONE_MINUS_CONSTANT_ALPHA(GL14.GL_ONE_MINUS_CONSTANT_ALPHA), /**
         Source alpha saturate
         */
        SRC_ALPHA_SATURATE(GL11.GL_SRC_ALPHA_SATURATE), /**
         Source 1 color
         */
        SRC1_COLOR(GL33.GL_SRC1_COLOR), /**
         1 - source 1 color
         */
        ONE_MINUS_SRC1_COLOR(GL33.GL_ONE_MINUS_SRC1_COLOR), /**
         Source 1 alpha
         */
        SRC1_ALPHA(GL33.GL_ONE_MINUS_SRC1_ALPHA);

        /**
         Blend func factor's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new BlendFuncFactor to the given value.

         @param code blend func factor's OpenGL code
         */
        BlendFuncFactor(int code){
            this.code = code;
        }

        /**
         Returns the BlendFuncFactor of the given OpenGL code.

         @param code OpenGL blend func factor

         @return the BlendFuncFactor of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not a blend func factor
         */
        @NotNull
        public static BlendFuncFactor valueOf(int code){
            for(BlendFuncFactor mode : BlendFuncFactor.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a blend func factor");
        }

        /**
         Returns the blend func factor's OpenGL code.

         @return the blend func factor's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     Blend equation.
     */
    public enum BlendEquation{
        /**
         Add.
         */
        FUNC_ADD(GL14.GL_FUNC_ADD), /**
         Subtract.
         */
        FUNC_SUBTRACT(GL14.GL_FUNC_SUBTRACT), /**
         Reverse subtract.
         */
        FUNC_REVERSE_SUBTRACT(GL14.GL_FUNC_REVERSE_SUBTRACT), /**
         Minimum.
         */
        MIN(GL14.GL_MIN), /**
         Maximum.
         */
        MAX(GL14.GL_MAX);

        /**
         Blend equation's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new BlendEquation to the given value.

         @param code blend equation's OpenGL code
         */
        BlendEquation(int code){
            this.code = code;
        }

        /**
         Returns the BlendEquation of the given OpenGL code.

         @param code OpenGL blend equation

         @return the BlendEquation of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not a blend equation
         */
        @NotNull
        public static BlendEquation valueOf(int code){
            for(BlendEquation mode : BlendEquation.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a blend equation");
        }

        /**
         Returns the blend equation's OpenGL code.

         @return the blend equation's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     Face culling mode.
     */
    public enum FaceCullingMode{
        /**
         Front.
         */
        FRONT(GL11.GL_FRONT), /**
         Back.
         */
        BACK(GL11.GL_BACK), /**
         Front and back.
         */
        FRONT_AND_BACK(GL11.GL_FRONT_AND_BACK);

        /**
         Face culling mode's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new FaceCullingMode to the given value.

         @param code face culling mode's OpenGL code
         */
        FaceCullingMode(int code){
            this.code = code;
        }

        /**
         Returns the FaceCullingMode of the given OpenGL code.

         @param code OpenGL face culling mode

         @return the FaceCullingMode of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not a face culling mode
         */
        @NotNull
        public static FaceCullingMode valueOf(int code){
            for(FaceCullingMode mode : FaceCullingMode.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a face culling mode");
        }

        /**
         Returns the face culling mode's OpenGL code.

         @return the face culling mode's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     Depth test mode.
     */
    public enum DepthTestMode{
        /**
         Never pass the depth test.
         */
        NEVER(GL11.GL_NEVER), /**
         Pass the depth test if the new value is less.
         */
        LESS(GL11.GL_LESS), /**
         Pass the depth test if the new value is equal.
         */
        EQUAL(GL11.GL_EQUAL), /**
         Pass the depth test if the new value is less or equal.
         */
        LESS_OR_EQUAL(GL11.GL_LEQUAL), /**
         Pass the depth test if the new value is greater.
         */
        GREATER(GL11.GL_GREATER), /**
         Pass the depth test if the new value isn't equal.
         */
        NOT_EQUAL(GL11.GL_NOTEQUAL), /**
         Pass the depth test if the new value is greater or equal.
         */
        GREATER_OR_EQUAL(GL11.GL_GEQUAL), /**
         Always pass the depth test.
         */
        ALWAYS(GL11.GL_ALWAYS);

        /**
         Depth test mode's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new DepthTestMode to the given value.

         @param code depth test mode's OpenGL code
         */
        DepthTestMode(int code){
            this.code = code;
        }

        /**
         Returns the DepthTestMode of the given OpenGL code.

         @param code OpenGL depth test mode

         @return the DepthTestMode of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not a depth test mode
         */
        @NotNull
        public static DepthTestMode valueOf(int code){
            for(DepthTestMode mode : DepthTestMode.values()){
                if(mode.getCode() == code){
                    return mode;
                }
            }
            throw new IllegalArgumentException("The given parameter is not a depth test mode");
        }

        /**
         Returns the depth test mode's OpenGL code.

         @return the depth test mode's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     List of the registered OpenGL error event handlers.
     */
    private static final List<OpenGlDebugEventHandler> eventHandlers = new ArrayList<>();

    /**
     To can't create OpenGl instance.
     */
    private OpenGl(){
    }

    /**
     Initializes the OpenGL settings to the Engine's default values like enabling depth testing.
     */
    public static void initializeToDefaults(){
        setMultisample(true);
        setFaceCulling(true);
        setFaceCullingMode(OpenGl.FaceCullingMode.BACK);
        setAlphaBlendingToDefault();
        setDepthTest(true);
        initializeDebugEventHandling();
    }

    /**
     Initializes the OpenGL's error handling.
     */
    private static void initializeDebugEventHandling(){
        if(EngineInfo.isDebugMode()){
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
            GL43.glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
                OpenGlEvent event = new OpenGlEvent(source, type, id, severity, length, message);
                handleDebugEvent(event);
            }, 0);
        }
    }

    /**
     Handles the given OpenGL error.

     @param event OpenGL error
     */
    private static void handleDebugEvent(@NotNull OpenGlEvent event){
        logDebugEvent(event);
        for(OpenGlDebugEventHandler eventHandler : eventHandlers){
            eventHandler.openGlDebugCallback(event);
        }
    }

    //
    //Debug event handling
    //

    /**
     Logs the given OpenGL event.

     @param event OpenGL debug event
     */
    private static void logDebugEvent(@NotNull OpenGlEvent event){
        switch(event.getSeverity()){
            case HIGH:
                Utility.logError(event.toString());
                break;
            case MEDIUM:
                Utility.log(event.toString(), Level.WARNING);
                break;
            case LOW:
                Utility.log(event.toString(), Level.INFO);
                break;
            case NOTIFICATION:
                Utility.log(event.toString(), Level.CONFIG);
                break;
        }
    }

    /**
     Enables the logging of the OpenGL debug events with the given parameters. The null parameter means don't care.

     @param source   event's source
     @param type     event's type
     @param severity event's severity
     */
    public static void enableDebugEvents(@Nullable OpenGlEventSource source, @Nullable OpenGlEventType type, @Nullable OpenGlEventSeverity severity){
        int sou = source == null ? GL11.GL_DONT_CARE : source.getCode();
        int typ = type == null ? GL11.GL_DONT_CARE : type.getCode();
        int sev = severity == null ? GL11.GL_DONT_CARE : severity.getCode();
        GL43.glDebugMessageControl(sou, typ, sev, (int[]) null, true);
    }

    /**
     Disables the logging of the OpenGL debug events with the given parameters. The null parameter means don't care.

     @param source   event's source
     @param type     event's type
     @param severity event's severity
     */
    public static void disableDebugEvents(@Nullable OpenGlEventSource source, @Nullable OpenGlEventType type, @Nullable OpenGlEventSeverity severity){
        int sou = source == null ? GL11.GL_DONT_CARE : source.getCode();
        int typ = type == null ? GL11.GL_DONT_CARE : type.getCode();
        int sev = severity == null ? GL11.GL_DONT_CARE : severity.getCode();
        GL43.glDebugMessageControl(sou, typ, sev, (int[]) null, false);
    }

    /**
     Adds the given OpenGL error event handler to the list of event handlers.

     @param eh OpenGL error event handler

     @throws NullPointerException parameter can't be null
     */
    public static void addErrorEventHandler(@NotNull OpenGlDebugEventHandler eh){
        if(eh == null){
            throw new NullPointerException();
        }
        if(!Utility.containsReference(eventHandlers, eh)){
            eventHandlers.add(eh);
        }
    }

    /**
     Removes the given OpenGL error event handler from the list of event handlers.

     @param eh OpenGL error event handler

     @throws NullPointerException parameter can't be null
     */
    public static void removeErrorEventHandler(@NotNull OpenGlDebugEventHandler eh){
        if(eh == null){
            throw new NullPointerException();
        }
        Utility.removeReference(eventHandlers, eh);
    }

    /**
     Removes the specified OpenGL error event handler from the list of event handlers.

     @param index OpenGL error event handler's index
     */
    public static void removeErrorEventHandler(int index){
        eventHandlers.remove(index);
    }

    /**
     Removes all the OpenGL error event handlers.
     */
    public static void removeAllErrorEventHandlers(){
        eventHandlers.clear();
    }

    /**
     Returns the number of registered OpenGL error event handlers.

     @return number of registered OpenGL error event handlers
     */
    public static int getErrorEventHandlerCount(){
        return eventHandlers.size();
    }

    /**
     Determines whether multisampling is enabled.

     @return true if multisampling is enabled, false otherwise
     */
    public static boolean isMultisampling(){
        return GL11.glIsEnabled(GL13.GL_MULTISAMPLE);
    }

    /**
     Sets whether or not enable the multisampling.

     @param multisample true if you would like to enable multisampling, false otherwise
     */
    public static void setMultisample(boolean multisample){
        if(multisample){
            GL11.glEnable(GL13.GL_MULTISAMPLE);
        }else{
            GL11.glDisable(GL13.GL_MULTISAMPLE);
        }
    }

    //
    //Wrappers for common native OpenGL functions
    //

    /**
     Returns whether the depth test is enabled.

     @return true if the depth test is enabled, false otherwise
     */
    public static boolean isDepthTest(){
        return GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    }

    /**
     Sets whether or not enable the depth testing.

     @param depthTest true if you would like to enable depth testing, false otherwise
     */
    public static void setDepthTest(boolean depthTest){
        if(depthTest){
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }else{
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
    }

    /**
     Returns whether the depth mask is enabled.

     @return true if the depth mask is enabled, false otherwise
     */
    public static boolean isDepthMask(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(1);
            GL11.glGetIntegerv(GL11.GL_DEPTH_WRITEMASK, ib);
            return ib.get(0) == GL11.GL_TRUE;
        }
    }

    /**
     Sets whether or not enable the depth mask.

     @param depthMask true if you would like to enable the depth mask, false otherwise
     */
    public static void setDepthMask(boolean depthMask){
        GL11.glDepthMask(depthMask);
    }

    /**
     Returns the depth test mode.

     @return the depth test mode
     */
    @NotNull
    public static DepthTestMode getDepthTestMode(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(1);
            GL11.glGetIntegerv(GL11.GL_DEPTH_FUNC, ib);
            return DepthTestMode.valueOf(ib.get(0));
        }
    }

    /**
     Sets the depth test mode to the given value.

     @param depthMode depth test mode
     */
    public static void setDepthTestMode(@NotNull DepthTestMode depthMode){
        GL11.glDepthFunc(depthMode.getCode());
    }

    /**
     Returns whether the alpha blending is enabled.

     @return true if the alpha blending is enabled, false otherwise
     */
    public static boolean isAlphaBlending(){
        return GL11.glIsEnabled(GL11.GL_BLEND);
    }

    /**
     Enables alpha blending and sets the blending function to (alpha - (1-alpha)).
     */
    public static void setAlphaBlendingToDefault(){
        setBlending(true);
        setBlendingFunction(BlendFuncFactor.SRC_ALPHA, BlendFuncFactor.ONE_MINUS_SRC_ALPHA);
    }

    /**
     Returns the blending equation.

     @return the blending equation
     */
    @NotNull
    public static BlendEquation getBlendEquation(){
        return BlendEquation.valueOf(GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA));
    }

    /**
     Sets the blending equation to the given value.

     @param equation blending equation
     */
    public static void setBlendingEquation(@NotNull BlendEquation equation){
        GL14.glBlendEquation(equation.getCode());
    }

    /**
     Returns whether the alpha blending is enabled.

     @return true if the alpha blending is enabled, false otherwise
     */
    public static boolean isBlending(){
        return GL11.glIsEnabled(GL11.GL_BLEND);
    }

    /**
     Sets whether or not enable the alpha blending.

     @param blending true if you would like to enable the alpha blending, false otherwise
     */
    public static void setBlending(boolean blending){
        if(blending){
            GL11.glEnable(GL11.GL_BLEND);
        }else{
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    /**
     Returns the blending function's source factor.

     @return the blending function's source factor
     */
    @NotNull
    public static BlendFuncFactor getBlendingSourceFactor(){
        return BlendFuncFactor.valueOf(GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA));
    }

    /**
     Returns the blending function's destination factor.

     @return the blending function's destination factor
     */
    @NotNull
    public static BlendFuncFactor getBlendingDestinationFactor(){
        return BlendFuncFactor.valueOf(GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA));
    }

    /**
     Sets the blending function's parameters to the given values.

     @param sourceFactor      source factor
     @param destinationFactor destination factor
     */
    public static void setBlendingFunction(@NotNull BlendFuncFactor sourceFactor, @NotNull BlendFuncFactor destinationFactor){
        GL11.glBlendFunc(sourceFactor.getCode(), destinationFactor.getCode());
    }

    /**
     Returns whether the wireframe mode is enabled.

     @return true if the wireframe mode is enabled, false otherwise
     */
    public static boolean isWireframe(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(1);
            GL11.glGetIntegerv(GL11.GL_POLYGON_MODE, ib);
            return ib.get(0) != GL11.GL_FILL;
        }
    }

    /**
     Sets whether or not enable the wireframe rendering mode.

     @param wireframeMode true if you would like to enable wireframe mode, false otherwise
     */
    public static void setWireframe(boolean wireframeMode){
        if(wireframeMode){
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        }else{
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }

    /**
     Returns the rendering viewport size.

     @return the rendering viewport size
     */
    public static Vector2i getViewportSize(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, ib);
            return new Vector2i(ib.get(2), ib.get(3));
        }
    }

    /**
     Returns the rendering viewport offset.

     @return the rendering viewport offset
     */
    public static Vector2i getViewportOffset(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, ib);
            return new Vector2i(ib.get(0), ib.get(1));
        }
    }

    /**
     Sets the rendering viewport to the given values.

     @param size   rendering width and height
     @param offset rendering offset

     @throws IllegalArgumentException width and height must be positive
     */
    public static void setViewport(@NotNull Vector2i size, @NotNull Vector2i offset){
        if(size.x <= 0 || size.y <= 0){
            throw new IllegalArgumentException("Width and height must be positive");
        }
        GL11.glViewport(offset.x, offset.y, size.x, size.y);
    }

    /**
     Returns whether the face culling is enabled.

     @return true if the face culling is enabled, false otherwise
     */
    public static boolean isFaceCulling(){
        return GL11.glIsEnabled(GL11.GL_CULL_FACE);
    }

    /**
     Sets whether or not enable the face culling.

     @param faceCulling true if you would like to enable face culling, false otherwise
     */
    public static void setFaceCulling(boolean faceCulling){
        if(faceCulling){
            GL11.glEnable(GL11.GL_CULL_FACE);
        }else{
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    /**
     Returns the face culling mode.

     @return the face culling mode
     */
    @NotNull
    public static FaceCullingMode getFaceCullingMode(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(1);
            GL11.glGetIntegerv(GL11.GL_CULL_FACE_MODE, ib);
            return FaceCullingMode.valueOf(ib.get(0));
        }
    }

    /**
     Sets the face culling mode to the given value.

     @param faceCulling face culling mode
     */
    public static void setFaceCullingMode(@NotNull FaceCullingMode faceCulling){
        GL11.glCullFace(faceCulling.getCode());
    }

    /**
     Binds the default framebuffer.
     */
    public static void bindDefaultFrameBuffer(){
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     Returns the clear color.

     @return the clear color
     */
    @NotNull
    @ReadOnly
    public static Vector3f getClearColor(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer fb = stack.callocFloat(4);
            GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, fb);
            return new Vector3f(fb.get(0), fb.get(1), fb.get(2));
        }
    }

    /**
     Sets the clear color to the given value.

     @param clearColor clear color

     @throws IllegalArgumentException environment color can't be lower than 0
     */
    public static void setClearColor(@NotNull Vector4f clearColor){
        if(!Utility.isHdrColor(new Vector3f(clearColor.x, clearColor.y, clearColor.z))){
            throw new IllegalArgumentException("Environment color can't be lower than 0");
        }
        GL11.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
    }

    /**
     Clears the currently bound FBO's specified attachments.

     @param color   color attachments
     @param depth   depth attachment
     @param stencil stencil attachment
     */
    public static void clear(boolean color, boolean depth, boolean stencil){
        int colorBit = color ? GL11.GL_COLOR_BUFFER_BIT : 0;
        int depthBit = depth ? GL11.GL_DEPTH_BUFFER_BIT : 0;
        int stencilBit = stencil ? GL11.GL_STENCIL_BUFFER_BIT : 0;
        GL11.glClear(colorBit | depthBit | stencilBit);
    }

    /**
     Returns the company responsible for this GL implementation.

     @return name of the GPU's vendor
     */
    @NotNull
    public static String getVendor(){
        return GL11.glGetString(GL11.GL_VENDOR);
    }

    /**
     Returns the name of the renderer. This name is typically specific to a particular configuration of a hardware
     platform.

     @return name of the used GPU
     */
    @NotNull
    public static String getRenderer(){
        return GL11.glGetString(GL11.GL_RENDERER);
    }

    /**
     The major version number of the OpenGL API supported by the current context.

     @return the major version of OpenGL
     */
    public static int getMajorVersion(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(1);
            GL11.glGetIntegerv(GL30.GL_MAJOR_VERSION, ib);
            return ib.get(0);
        }
    }

    /**
     The minor version number of the OpenGL API supported by the current context.

     @return the minor version of OpenGL
     */
    public static int getMinorVersion(){
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer ib = stack.callocInt(1);
            GL11.glGetIntegerv(GL30.GL_MINOR_VERSION, ib);
            return ib.get(0);
        }
    }

    /**
     Returns a version or release number for the shading language.

     @return a version or release number for the shading language
     */
    @NotNull
    public static String getGlslVersion(){
        return GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
    }
}
