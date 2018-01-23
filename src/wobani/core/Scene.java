package wobani.core;

import wobani.components.audio.*;
import wobani.components.camera.*;
import wobani.components.light.lightTypes.*;
import wobani.resources.environmentProbes.*;
import wobani.toolbox.annotations.*;
import wobani.toolbox.parameters.*;

/**
 * Scene contains all the GameObjectContainer, the RenderableContainer, unique lists of
 Components and parameters like the main Camera or the main DirectionalLight.
 */
public class Scene {

    /**
     * Stores all the GameObjectContainer.
     */
    private static final GameObjectContainer GAMEOBJECTS = new GameObjectContainer();
    /**
     * Stores all the RenderableContainer groupped by Renderers and
 Renderables.
     */
    private static final RenderableContainer RENDERABLE_COMPONENTS = new RenderableContainer();
    /**
     * Stores Components in user defined lists.
     */
    private static final ComponentLists COMPONENT_LISTS = new ComponentLists();
    /**
     * Stores the Scene's parameters.
     */
    private static final ParameterContainer PARAMETERS = new ParameterContainer();
    /**
     * Key of the main Camera Parameter.
     */
    public static final ParameterKey<Camera> MAIN_CAMERA = new ParameterKey<>(Camera.class, "MAIN_CAMERA");
    /**
     * Key of the main BlinnPhongDirectionalLight Parameter.
     */
    public static final ParameterKey<BlinnPhongDirectionalLight> MAIN_DIRECTIONAL_LIGHT = new ParameterKey<>(BlinnPhongDirectionalLight.class, "MAIN_CAMERA");
    /**
     * Key of the main skybox Parameter.
     */
    public static final ParameterKey<EnvironmentProbe> MAIN_SKYBOX = new ParameterKey<>(EnvironmentProbe.class, "MAIN_SKYBOX");
    /**
     * Key of the main AudioListenerComponent Parameter.
     */
    public static final ParameterKey<AudioListenerComponent> MAIN_AUDIO_LISTENER = new ParameterKey<>(AudioListenerComponent.class, "MAIN_AUDIO_LISTENER");

    /**
     * To can't create Scene instance.
     */
    private Scene() {
    }

    /**
     * Returns all the GameObjectContainer.
     *
     * @return all the GameObjectContainer
     */
    @NotNull
    public static GameObjectContainer getGameObjects() {
        return GAMEOBJECTS;
    }

    /**
     * Returns all the RenderableContainer.
     *
     * @return all the RenderableContainer
     */
    @NotNull
    public static RenderableContainer getRenderableComponents() {
        return RENDERABLE_COMPONENTS;
    }

    /**
     * Returns all the user defined lists of Components.
     *
     * @return all the user defined lists of Components
     */
    @NotNull
    public static ComponentLists getComponentLists() {
        return COMPONENT_LISTS;
    }

    /**
     * Returns the Scene's parameters.
     *
     * @return the Scene's parameters
     */
    @NotNull
    public static ParameterContainer getParameters() {
        return PARAMETERS;
    }

}
