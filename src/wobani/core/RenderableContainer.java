package wobani.core;

import java.util.*;
import wobani.component.renderable.*;
import wobani.material.*;
import wobani.rendering.geometry.*;
import wobani.resources.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

/**
 * Stores all the RenderableContainer.
 */
public class RenderableContainer {

    /**
     * Contains all the RenderableContainer.
     */
    private final Map<Class<? extends GeometryRenderer>, RenderableMap> RENDERABLES = new HashMap<>();

    /**
     * Adds the given RenderableComponent if it's attached to a GameObject.
     *
     * @param renderableComponent RenerableComponent you want to add
     */
    @Internal
    public void add(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	addRenderableComponent(renderableComponent);
    }

    /**
     * Adds the given RenderableComponent to the hierarchy.
     *
     * @param renderableComponent RenderableComponent you want to add
     */
    private void addRenderableComponent(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	RenderableMap map = getOrAdd(renderableComponent.getMaterial().getRenderer());
	map.add(renderableComponent);
    }

    /**
     * Removes the given RenderableComponent if it's not attached to a
     * GameObject.
     *
     * @param renderableComponent RenerableComponent you want to remove
     */
    @Internal
    public void remove(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	RenderableMap map = get(renderableComponent.getMaterial().getRenderer());
	map.remove(renderableComponent);
    }

    /**
     * Removes the given RenderableComponent from the hierarchy based on the
     * given Renderable and Material. You should call this method if the
     * RenderableComponent's Material or Renderable changed and it's not
     * detached from a GameObject.
     *
     * @param renderableComponent RenderableComponent
     * @param fromRenderable      Renderable
     * @param fromMaterial        Material
     */
    private void removeRenderableComponentWhenChanged(@NotNull RenderableComponent<? extends Renderable> renderableComponent, @NotNull Renderable fromRenderable, @NotNull Material fromMaterial) {
	RenderableMap map = get(fromMaterial.getRenderer());
	map.removeWhemChanged(renderableComponent, fromRenderable, fromMaterial);
    }

    /**
     * Refreshes the given RenderableComponent in the hierarchy after the
     * RenderableComponent's Renderable changed.
     *
     * @param renderableComponent RenderableComponent
     * @param from                RenderableComponent's old Renderable
     */
    @Internal
    public void refreshRenderableChange(@NotNull RenderableComponent<? extends Renderable> renderableComponent, @Nullable Renderable from) {
	if (from == null) {
	    return;
	}
	removeRenderableComponentWhenChanged(renderableComponent, from, renderableComponent.getMaterial());
	addRenderableComponent(renderableComponent);
    }

    /**
     * Refreshes the given RenderableComponent in the hierarchy after the
     * RenderableComponent's Material changed.
     *
     * @param renderableComponent RenderableComponent
     * @param from                RenderableComponent's old Material
     */
    @Internal
    public void refreshMaterialChange(@NotNull RenderableComponent<? extends Renderable> renderableComponent, @Nullable Material from) {
	if (from == null) {
	    return;
	}
	removeRenderableComponentWhenChanged(renderableComponent, renderableComponent.getRenderable(), from);
	addRenderableComponent(renderableComponent);
    }

    /**
     * Returns a RenderableMap based on the given Material's Renderer. If there
     * is no appropirate RenderableMap, this method creates one.
     *
     * @param renderer GeometryRenderer
     *
     * @return a RenderableMap based on the given Material's Renderer
     */
    @NotNull
    private RenderableMap getOrAdd(@NotNull Class<? extends GeometryRenderer> renderer) {
	RenderableMap map = get(renderer);
	if (map == null) {
	    map = add(renderer);
	}
	return map;
    }

    /**
     * Returns a RenderableMap based on the given Material's Renderer. If there
     * is no appropirate RenderableMap, this method returns null.
     *
     * @param renderer GeometryRenderer
     *
     * @return a RenderableMap based on the given Material's Renderer
     */
    @Nullable
    private RenderableMap get(@Nullable Class<? extends GeometryRenderer> renderer) {
	return RENDERABLES.get(renderer);
    }

    /**
     * Adds a RenderableMap based on the given Material's Renderer.
     *
     * @param renderer GeometryRenderer
     *
     * @return a RenderableMap based on the given Material's Renderer
     *
     * @throws NullPointerException parameter can't be null
     */
    @NotNull
    private RenderableMap add(@NotNull Class<? extends GeometryRenderer> renderer) {
	if (renderer == null) {
	    throw new NullPointerException();
	}
	RenderableMap map = new RenderableMap();
	RENDERABLES.put(renderer, map);
	return map;
    }

    /**
     * Returns the array of Renderables using the given GeometryRenderer.
     *
     * @param renderer GeometryRenderer
     *
     * @return the array of Renderables using the given GeometryRenderer
     */
    @NotNull @ReadOnly
    public Renderable[] getRenderables(@NotNull Class<? extends GeometryRenderer> renderer) {
	RenderableMap map = get(renderer);
	if (map == null) {
	    return new Renderable[0];
	} else {
	    return getRenderablesArray(map);
	}
    }

    /**
     * Returns the array of Renderables using the given GeometryRenderer.
     *
     * @param map RenderableMap
     *
     * @return the array of Renderables using the given GeometryRenderer
     */
    @NotNull @ReadOnly
    private Renderable[] getRenderablesArray(@NotNull RenderableMap map) {
	Renderable[] ret = new Renderable[map.getRenderables().size()];
	map.getRenderables().toArray(ret);
	return ret;
    }

    /**
     * Returns the number of RenderableContainer using the given
     * GeometryRenderer and Renderable.
     *
     * @param renderer   GeometryRenderer
     * @param renderable Renderable
     *
     * @return number of RenderableComponent using the given GeometryRenderer
     *         and Renderable
     */
    public int getRenderableComponentCount(@Nullable Class<? extends GeometryRenderer> renderer, @Nullable Renderable renderable) {
	RenderableMap map = get(renderer);
	if (map == null) {
	    return 0;
	} else {
	    return map.getRenderableComponentCount(renderable);
	}
    }

    /**
     * Returns the indexth RenderableComponent using the given GeometryRenderer
     * and Renderable.If there is no such RenderableComponent, it returns null.
     *
     * @param renderer   GeometryRenderer
     * @param renderable Renderable
     * @param index      index
     *
     * @return the indexth RenderableComponent using the given GeometryRenderer
     *         and Renderable
     */
    @Nullable
    public RenderableComponent<? extends Renderable> getRenderableComponent(@Nullable Class<? extends GeometryRenderer> renderer, @Nullable Renderable renderable, int index) {
	RenderableMap map = get(renderer);
	if (map == null) {
	    return null;
	} else {
	    return map.getRenderableComponent(renderable, index);
	}
    }

    /**
     * The RenderableMap contains RenderableContainer grouped by Renderables.
     */
    private class RenderableMap {

	/**
	 * Contains RenderableContainer grouped by Renderables.
	 */
	private final Map<Renderable, RenderableComponentList> map = new HashMap<>();

	/**
	 * Adds the given RenderableComponent if it's attached to a GameObject.
	 *
	 * @param renderableComponent RenderableComponent you want to add
	 *
	 * @throws ComponentAttachmentException RenderableComponent is not yet
	 *                                      attached to a GameObject
	 */
	public void add(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	    if (renderableComponent.getGameObject() == null) {
		throw new ComponentAttachmentException(renderableComponent);
	    }
	    RenderableComponentList list = getOrAddList(renderableComponent);
	    list.add(renderableComponent);
	}

	/**
	 * Removes the given RenderableComponent if it's not attached to a
	 * GameObject.
	 *
	 * @param renderableComponent RenderableComponent you want to remove
	 *
	 * @throws ComponentAttachmentException RenderableComponent already
	 *                                      attached to a GameObject
	 */
	public void remove(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	    if (renderableComponent.getGameObject() != null) {
		throw new ComponentAttachmentException(renderableComponent);
	    }
	    removeRenderableComponent(renderableComponent, renderableComponent.getRenderable(), renderableComponent.getMaterial());
	}

	/**
	 * Removes the given RenderableComponent if it's Material or Renderable
	 * changed.
	 *
	 * @param renderableComponent RenderableComponent you want to remove
	 * @param fromRenderable      Renderable
	 * @param fromMaterial        Material
	 *
	 * @throws ComponentAttachmentException RenderableComponent is not yet
	 *                                      attached to a GameObject
	 */
	public void removeWhemChanged(@NotNull RenderableComponent<? extends Renderable> renderableComponent, @NotNull Renderable fromRenderable, @NotNull Material fromMaterial) {
	    if (renderableComponent.getGameObject() == null) {
		throw new ComponentAttachmentException(renderableComponent);
	    }
	    removeRenderableComponent(renderableComponent, fromRenderable, fromMaterial);
	}

	/**
	 * Return the number of RendereableComponents using the given
	 * Renderable.
	 *
	 * @param renderable Renderable
	 *
	 * @return the number of RendereableComponents using the given
	 *         Renderable
	 */
	public int getRenderableComponentCount(@Nullable Renderable renderable) {
	    RenderableComponentList list = map.get(renderable);
	    return list == null ? 0 : list.size();
	}

	/**
	 * Returns the indexth RenderableComponent using the given Renderable.
	 *
	 * @param index      RenderableComponent's index
	 * @param renderable Renderable
	 *
	 * @return the indexth RenderableComponent using the given Renderable
	 */
	@Nullable
	public RenderableComponent<? extends Renderable> getRenderableComponent(@Nullable Renderable renderable, int index) {
	    RenderableComponentList list = map.get(renderable);
	    return list == null ? null : list.get(index);
	}

	/**
	 * Returns the Renderables using this GeometryRenderer.
	 *
	 * @return the Renderables using this GeometryRenderer
	 */
	@NotNull
	public Set<Renderable> getRenderables() {
	    return map.keySet();
	}

	/**
	 * Returns a RenderableComponentList based on the given
	 * RenderableComponent. If there is no appropirate
	 * RenderableComponentList, this method creates one.
	 *
	 * @param renderableComponent RenderableComponentList
	 *
	 * @return a RenderableComponentList based on the given
	 *         RenderableComponent
	 */
	@NotNull
	private RenderableComponentList getOrAddList(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	    RenderableComponentList list = getList(renderableComponent.getRenderable());
	    if (list == null) {
		list = addList(renderableComponent);
	    }
	    return list;
	}

	/**
	 * Returns a RenderableComponentList based on the given Renderable. If
	 * there is no appropirate RenderableComponentList, this method returns
	 * null.
	 *
	 * @param renderable Renderable
	 *
	 * @return a RenderableComponentList based on the given Renderable
	 */
	@Nullable
	private RenderableComponentList getList(@Nullable Renderable renderable) {
	    return map.get(renderable);
	}

	/**
	 * Adds a RenderableComponentList based on the given
	 * RenderableComponent.
	 *
	 * @param renderableComponent RenderableComponent
	 *
	 * @return a RenderableComponentList based on the given
	 *         RenderableComponent
	 */
	@NotNull
	private RenderableComponentList addList(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	    RenderableComponentList list = new RenderableComponentList();
	    map.put(renderableComponent.getRenderable(), list);
	    return list;
	}

	/**
	 * Removes the given RenderableComponent based on the given parameters.
	 *
	 * @param renderableComponent RenderableComponent you want to remove
	 * @param fromRenderable      Renderable
	 * @param fromMaterial        Material
	 */
	private void removeRenderableComponent(@NotNull RenderableComponent<? extends Renderable> renderableComponent, @NotNull Renderable fromRenderable, @NotNull Material fromMaterial) {
	    RenderableComponentList list = getList(fromRenderable);
	    if (list != null) {
		list.remove(renderableComponent);
		removeUnnecessaryLeaf(list, fromRenderable, fromMaterial);
	    }
	}

	/**
	 * Removes the unnecessary leaf based on the given parameters.
	 *
	 * @param list           RenderableComponentList
	 * @param fromRenderable Renderable
	 * @param fromMaterial   Material
	 */
	private void removeUnnecessaryLeaf(@NotNull RenderableComponentList list, @NotNull Renderable fromRenderable, @NotNull Material fromMaterial) {
	    if (list.isEmpty()) {
		map.remove(fromRenderable);
		if (map.isEmpty()) {
		    RENDERABLES.remove(fromMaterial.getRenderer());
		}
	    }
	}

	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 71 * hash + Objects.hashCode(this.map);
	    return hash;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final RenderableMap other = (RenderableMap) obj;
	    if (!Objects.equals(this.map, other.map)) {
		return false;
	    }
	    return true;
	}

	@Override
	public String toString() {
	    StringBuilder res = new StringBuilder(RenderableMap.class.getSimpleName())
		    .append("(")
		    .append("map: ").append(map)
		    .append(")");
	    return res.toString();
	}

    }

    /**
     * Represents a list which contains RenderableContainer using the same
     * Renderable and GeometryRenderer.
     */
    private class RenderableComponentList {

	/**
	 * List of RenderableContainer.
	 */
	private final List<RenderableComponent<? extends Renderable>> list = new ArrayList<>();

	/**
	 * Adds the given RenderableComponent it it's not already stored.
	 *
	 * @param renderableComponent RenderableComponent you want to add
	 */
	public void add(@NotNull RenderableComponent<? extends Renderable> renderableComponent) {
	    if (!contains(renderableComponent)) {
		list.add(renderableComponent);
	    }
	}

	/**
	 * Returns the specified RenderableComponent.
	 *
	 * @param index RenderableComponent's index
	 *
	 * @return the specified RenderableComponent
	 */
	@NotNull
	public RenderableComponent<? extends Renderable> get(int index) {
	    return list.get(index);
	}

	/**
	 * Determines whether the given RenderableComponent is contained by the
	 * list.
	 *
	 * @param renderableComponent RenderableComponent
	 *
	 * @return true if the list contains the given RenderableComponent,
	 *         false otherwise
	 */
	public boolean contains(@Nullable RenderableComponent<? extends Renderable> renderableComponent) {
	    return Utility.containsReference(list, renderableComponent);
	}

	/**
	 * Returns the number of RenderableContainer in the list.
	 *
	 * @return the number of RenderableContainer in the list
	 */
	public int size() {
	    return list.size();
	}

	/**
	 * Returns true if this list contains no elements.
	 *
	 * @return true if this list contains no elements, false otherwise
	 */
	public boolean isEmpty() {
	    return list.isEmpty();
	}

	/**
	 * Removes the given RenderableComponent.
	 *
	 * @param renderableComponent RenderableComponent
	 */
	public void remove(@Nullable RenderableComponent<? extends Renderable> renderableComponent) {
	    Utility.removeReference(list, renderableComponent);
	}

	@Override
	public int hashCode() {
	    int hash = 7;
	    return hash;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final RenderableComponentList other = (RenderableComponentList) obj;
	    if (!Objects.equals(this.list, other.list)) {
		return false;
	    }
	    return true;
	}

	@Override
	public String toString() {
	    StringBuilder res = new StringBuilder(RenderableComponentList.class.getSimpleName())
		    .append("(")
		    .append("list: ").append(list)
		    .append(")");
	    return res.toString();
	}

    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 97 * hash + Objects.hashCode(this.RENDERABLES);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final RenderableContainer other = (RenderableContainer) obj;
	if (!Objects.equals(this.RENDERABLES, other.RENDERABLES)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder("RenderableComponents(");
	for (Class<?> renderer : RENDERABLES.keySet()) {
	    res.append(" ").append(renderer.getSimpleName()).append("(");
	    RenderableMap rm = RENDERABLES.get(renderer);
	    for (Renderable renderable : rm.getRenderables()) {
		res.append(" ").append(renderable.hashCode()).append("(")
			.append(rm.getList(renderable).size())
			.append("), ");
	    }
	    res.append("), ");
	}
	res.append(")");
	return res.toString();
    }

}
