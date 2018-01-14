package core;

import toolbox.annotations.*;
import toolbox.parameters.*;

/**
 * Scene contains all the GameObjects, the RenderableComponents, unique lists of
 * Components and parameters like the main Camera or the main DirectionalLight.
 */
public class Scene {

    /**
     * Stores all the GameObjects.
     */
    private static final GameObjects GAMEOBJECTS = new GameObjects();

    /**
     * Stores all the RenderableComponents groupped by Renderers and
     * Renderables.
     */
    private static final RenderableComponents RENDERABLE_COMPONENTS = new RenderableComponents();

    /**
     * Stores Components in user defined lists.
     */
    private static final ComponentLists COMPONENT_LISTS = new ComponentLists();

    /**
     * Stores the Scene's parameters.
     */
    private static final UniqueParameters PARAMETERS = new UniqueParameters();

    /**
     * To can't create Scene instance.
     */
    private Scene() {
    }

    /**
     * Returns all the GameObjects.
     *
     * @return all the GameObjects
     */
    @NotNull
    public static GameObjects getGameObjects() {
        return GAMEOBJECTS;
    }

    /**
     * Returns all the RenderableComponents.
     *
     * @return all the RenderableComponents
     */
    @NotNull
    public static RenderableComponents getRenderableComponents() {
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
    public static UniqueParameters getParameters() {
        return PARAMETERS;
    }

}
