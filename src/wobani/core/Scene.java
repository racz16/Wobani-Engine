package wobani.core;

import wobani.component.audio.*;
import wobani.component.camera.*;
import wobani.resource.environmentprobe.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.parameter.*;

/**
 Scene contains all the GameObjectContainer, the RenderableContainer, unique lists of Components and parameters like the
 main Camera or the main DirectionalLight.
 */
public class Scene{

    /**
     Key of the main Camera Parameter.
     */
    public static final ParameterKey<Camera> MAIN_CAMERA = new ParameterKey<>(Camera.class, "MAIN_CAMERA");
    /**
     Key of the main skybox Parameter.
     */
    public static final ParameterKey<EnvironmentProbe> MAIN_SKYBOX = new ParameterKey<>(EnvironmentProbe.class, "MAIN_SKYBOX");
    /**
     Key of the main AudioListenerComponent Parameter.
     */
    public static final ParameterKey<AudioListenerComponent> MAIN_AUDIO_LISTENER = new ParameterKey<>(AudioListenerComponent.class, "MAIN_AUDIO_LISTENER");
    /**
     Stores all the GameObjectContainer.
     */
    private static final GameObjectContainer GAMEOBJECTS = new GameObjectContainer();
    /**
     Stores Components in user defined lists.
     */
    private static final ComponentLists COMPONENT_LISTS = new ComponentLists();
    /**
     Stores the Scene's parameters.
     */
    private static final ParameterContainer PARAMETERS = new ParameterContainer();

    /**
     To can't create Scene instance.
     */
    private Scene(){
    }

    /**
     Returns all the GameObjectContainer.

     @return all the GameObjectContainer
     */
    @NotNull
    public static GameObjectContainer getGameObjects(){
        return GAMEOBJECTS;
    }

    /**
     Returns all the user defined lists of Components.

     @return all the user defined lists of Components
     */
    @NotNull
    public static ComponentLists getComponentLists(){
        return COMPONENT_LISTS;
    }

    /**
     Returns the Scene's parameters.

     @return the Scene's parameters
     */
    @NotNull
    public static ParameterContainer getParameters(){
        return PARAMETERS;
    }

}
