package wobani.component.light.blinnphong;

import org.joml.*;
import wobani.resource.opengl.buffer.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

import static wobani.component.light.blinnphong.BlinnPhongShaderHelper.*;

/**
 Represents a tile of the world and manages the positional lights placed in this tile of the world both in CPU and VGA
 side.
 */
public class BlinnPhongLightSourceTile{

    /**
     The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(BlinnPhongLightSourceTile.class.getName());
    /**
     The tile's positional light sources in the SSBO.
     */
    private final List<BlinnPhongPositionalLightComponent> lights = new ArrayList<>();
    /**
     The tile's center position.
     */
    private final Vector2i tileCenter = new Vector2i();
    /**
     The SSBO which contains the tile's light sources in the VGA side.
     */
    private Ssbo ssbo;
    /**
     Number of light slots used in the SSBO.
     */
    private int slotCount = 0;
    /**
     Signs that all light sources in the tile need to refresh for example because the SSBO's size changed.
     */
    private boolean refreshAllLights;
    /**
     The last time when lights filled more than half of the SSBO's capacity.
     */
    private long lastActive;

    /**
     Initializes a new BlinnPhongLightSourceTile to the given value.

     @param center tile's center position
     */
    public BlinnPhongLightSourceTile(@NotNull Vector2i center){
        tileCenter.set(center);
        createSsbo();
        extendSizeTo(1);
        setLastActiveToNow();
    }

    /**
     Refreshes in the VRAM the given light source.

     @param light positional light source
     */
    public void refresh(@NotNull BlinnPhongPositionalLightComponent light){
        removeIfNeeded(light);
        addIfNeeded(light);
        refreshIfNeeded(light);
        refreshAllLightsIfNeeded();
    }

    /**
     Refreshes all the tile's light sources if the SSBO's size changed.
     */
    private void refreshAllLightsIfNeeded(){
        if(refreshAllLights){
            refreshAllLights = false;
            refreshAllLightsUnsafe();
            setSlotCount();
        }
    }

    /**
     Refreshes all the tile's light sources.
     */
    private void refreshAllLightsUnsafe(){
        for(int i = 0; i < lights.size(); i++){
            if(lights.get(i) != null){
                refresh(lights.get(i));
            }else{
                removeLightFromSsbo(i);
            }
        }
    }

    //
    //remove
    //

    /**
     Removes the given light if it shouldn't be in this tile.

     @param light positional light source
     */
    private void removeIfNeeded(@NotNull BlinnPhongPositionalLightComponent light){
        if(shouldRemove(light)){
            lights.set(light.getShaderIndex(), null);
            removeLightFromSsbo(light.getShaderIndex());
            setLightMetadataToInactive(light);
        }
    }

    /**
     Returns true if the given light shouldn't be in this tile, false otherwise.

     @param light positional light source

     @return true if the given light shouldn't be in this tile, false otherwise
     */
    private boolean shouldRemove(@NotNull BlinnPhongPositionalLightComponent light){
        boolean attached = light.getGameObject() != null;
        boolean hasShaderIndex = light.getShaderIndex() != -1;
        boolean thisTileContains = thisTileContains(light);
        boolean shouldBeInAnotherTile = shouldBeInAnotherTile(light);
        return hasShaderIndex && thisTileContains && (!attached || shouldBeInAnotherTile);
    }

    /**
     Sets the given light's shader index to -1 and tile position to null.

     @param light positional light source
     */
    private void setLightMetadataToInactive(@NotNull BlinnPhongPositionalLightComponent light){
        light.setShaderIndex(-1);
        light.setTilePosition(null);
    }

    //
    //add
    //

    /**
     Adds the given light if it should be in this tile.

     @param light positional light source
     */
    private void addIfNeeded(@NotNull BlinnPhongPositionalLightComponent light){
        if(shouldAdd(light)){
            int shaderIndex = computeNewShaderIndex();
            extendStorageIfNeeded(shaderIndex);
            setLightMetadata(light, shaderIndex);
            lights.set(shaderIndex, light);
        }
    }

    /**
     Returns true if the given light should be in this tile, false otherwise.

     @param light positional light source

     @return true if the given light should be in this tile, false otherwise
     */
    private boolean shouldAdd(@NotNull BlinnPhongPositionalLightComponent light){
        boolean attached = light.getGameObject() != null;
        boolean hasShaderIndex = light.getShaderIndex() != -1;
        boolean thisTileContains = thisTileContains(light);
        boolean shouldBeInThisTile = !shouldBeInAnotherTile(light);
        return attached && !hasShaderIndex && !thisTileContains && shouldBeInThisTile;
    }

    /**
     Computes the new light source's index.

     @return the new light source's index
     */
    private int computeNewShaderIndex(){
        for(int i = 0; i < lights.size(); i++){
            if(lights.get(i) == null){
                return i;
            }
        }
        return lights.size();
    }

    /**
     Extends the SSBO's size if the given light shader index equals to the SSBO's size.

     @param shaderIndex shader index
     */
    private void extendStorageIfNeeded(int shaderIndex){
        if(shaderIndex == lights.size()){
            int size = lights.size() * 2;
            extendSizeTo(size);
        }
    }

    /**
     Sets the given light's shader index to the given value and tile position to this tile's center position.

     @param light       positional light source
     @param shaderIndex shader index
     */
    private void setLightMetadata(@NotNull BlinnPhongPositionalLightComponent light, int shaderIndex){
        light.setShaderIndex(shaderIndex);
        light.setTilePosition(new Vector2i(tileCenter));
    }

    //
    //refresh
    //

    /**
     Refreshes the given light in the SSBO.

     @param light positional light source
     */
    private void refreshIfNeeded(@NotNull BlinnPhongPositionalLightComponent light){
        if(shouldRefresh(light)){
            refreshInSsbo(light);
        }
    }

    /**
     Returns true if the given light should be refreshed in this tile, false otherwise.

     @param light positional light source

     @return rue if the given light should be refreshed in this tile, false otherwise
     */
    private boolean shouldRefresh(@NotNull BlinnPhongPositionalLightComponent light){
        boolean attached = light.getGameObject() != null;
        boolean hasShaderIndex = light.getShaderIndex() != -1;
        boolean thisTileContains = tileCenter.equals(light.getTilePosition());

        return attached && hasShaderIndex && thisTileContains;
    }

    /**
     Refreshes the given light source's data in the SSBO.

     @param light positional light source
     */
    private void refreshInSsbo(@NotNull BlinnPhongPositionalLightComponent light){
        FloatBuffer parameters = light.computeLightParameters();
        IntBuffer metadata = light.computeLightMetadata();
        ssbo.bind();
        ssbo.store(parameters, light.getShaderIndex() * LIGHT_SIZE + LIGHT_SOURCES_OFFSET);
        ssbo.store(metadata, light.getShaderIndex() * LIGHT_SIZE + TYPE_ADDRESS + LIGHT_SOURCES_OFFSET);
        ssbo.unbind();
        LOG.fine("Positional light refreshed in the SSBO");
    }

    //
    //other
    //

    /**
     Returns the tile's center position.

     @return the tile's center position
     */
    public Vector2i getCenter(){
        return tileCenter;
    }

    /**
     Computes the tile's slot count and refreshes it in the SSBO.
     */
    private void setSlotCount(){
        slotCount = computeSlotCount();
        refreshSlotCountInSsbo();
    }

    /**
     Computes the tile's slot count.

     @return the tile's slot count
     */
    private int computeSlotCount(){
        for(int i = lights.size() - 1; i >= 0; i--){
            if(lights.get(i) != null){
                return i + 1;
            }
        }
        return 0;
    }

    /**
     Refreshes the tile's slot count in the SSBO.
     */
    private void refreshSlotCountInSsbo(){
        ssbo.bind();
        ssbo.store(new int[]{slotCount}, 0);
        ssbo.unbind();
    }

    /**
     Removes the light source from the SSBO based on the given shader index.

     @param shaderIndex light shader index
     */
    private void removeLightFromSsbo(int shaderIndex){
        ssbo.bind();
        ssbo.store(new int[]{0}, shaderIndex * LIGHT_SIZE + ACTIVE_ADDRESS + LIGHT_SOURCES_OFFSET);
        ssbo.unbind();
        LOG.fine("Positional light removed from the SSBO");
    }

    /**
     Changes the SSBO's size based on how many light sources you would like to store in it.

     @param lightCount number of light to store in the SSBO
     */
    private void changeSsboSizeTo(int lightCount){
        refreshAllLights = true;
        ssbo.bind();
        ssbo.allocate(lightCount * LIGHT_SIZE + LIGHT_SOURCES_OFFSET, BufferObject.BufferObjectUsage.STATIC_DRAW);
        ssbo.unbind();
        LOG.fine("Positional light SSBO size changed");
    }

    /**
     Extends the list which contains this tile's lights and the SSBO's size to the given size.

     @param size size
     */
    private void extendSizeTo(int size){
        changeSsboSizeTo(size);
        while(lights.size() < size){
            lights.add(null);
        }
    }

    /**
     Shrinks the list which contains this tile's lights and the SSBO's size to the given size.

     @param size size
     */
    private void shrinkSizeTo(int size){
        changeSsboSizeTo(size);
        while(lights.size() > size){
            lights.remove(size);
        }
    }

    /**
     Returns true if the given positional light source should be in this tile, false otherwise.

     @param light positional light source

     @return true if the given positional light source should be in this tile, false otherwise
     */
    private boolean shouldBeInAnotherTile(@NotNull BlinnPhongPositionalLightComponent light){
        if(light.getGameObject() != null){
            Vector3f absolutePosition = light.getGameObject().getTransform().getAbsolutePosition();
            Vector2i validTilePosition = BlinnPhongLightSources.computeTilePosition(absolutePosition);
            return !tileCenter.equals(validTilePosition);
        }
        return false;
    }

    /**
     Returns true if this tile contains the given positional light source, false otherwise.

     @param light positional light source

     @return true if this tile contains the given positional light source, false otherwise
     */
    private boolean thisTileContains(@NotNull BlinnPhongPositionalLightComponent light){
        for(BlinnPhongPositionalLightComponent bpplc : lights){
            if(light == bpplc){
                return true;
            }
        }
        return false;
    }

    //
    //resource
    //

    /**
     Signs that the lights fill more than half of the SSBO's capacity and don't have to shrink the SSBO in the near
     future.
     */
    private void setLastActiveToNow(){
        lastActive = System.currentTimeMillis();
    }

    /**
     Tries to optimize the tile's SSBO. If more than half of the SSBO is empty or even the whole SSBO is empty since a
     time, it'll shrink or even release the SSBO.

     @see BlinnPhongLightSources#setActionTimeLimit(long)
     */
    public void refreshState(){
        int elementCount = computeNumberOfLightsInTheTile();
        if(elementCount > lights.size() / 2){
            setLastActiveToNow();
        }
        long elapsedTime = System.currentTimeMillis() - lastActive;
        shrinkOrReleaseSsboIfNeeded(elapsedTime);
    }

    /**
     Tries to optimize the tile's SSBO. If more than half of the SSBO is empty or even the whole SSBO is empty at a time,
     it'll shrink or even release the SSBO.

     @param elapsedTimeSinceMoreThanHalfCapacity elapsed time since the SSBO was filled more than half of it's capacity
     */
    private void shrinkOrReleaseSsboIfNeeded(long elapsedTimeSinceMoreThanHalfCapacity){
        if(elapsedTimeSinceMoreThanHalfCapacity > lastActive){
            if(lights.size() == 1){
                release();
            }else{
                shrinkSsbo();
            }
        }
    }

    /**
     Computes the number of the light sources stored in this tile.

     @return the number of light sources stored in this tile
     */
    private int computeNumberOfLightsInTheTile(){
        int elementCount = 0;
        for(BlinnPhongPositionalLightComponent light : lights){
            if(light != null){
                elementCount++;
            }
        }
        return elementCount;
    }

    /**
     Replaces the lights into the first half of the SSBO's memory and than shrinks the SSBO to half memory size.
     */
    private void shrinkSsbo(){
        for(BlinnPhongPositionalLightComponent light : lights){
            if(light != null){
                placeLightToLowerIndex(light);
            }
        }
        shrinkSizeTo(lights.size() / 2);
        refreshAllLightsIfNeeded();
    }

    /**
     Tries to replace the given light to a lower index in the SSBO.

     @param light positional light source
     */
    private void placeLightToLowerIndex(@NotNull BlinnPhongPositionalLightComponent light){
        int placeIndex = computeFirstEmptySlot();
        int lightIndex = light.getShaderIndex();
        if(placeIndex >= 0){
            lights.set(placeIndex, lights.get(lightIndex));
            lights.set(lightIndex, null);
            lights.get(placeIndex).setShaderIndex(placeIndex);
        }
    }

    /**
     Computes the SSBO's first empty light slot in the first half of the SSBO's memory. It returns -1 if all the SSBO's
     first half memory is filled with light sources.

     @return the SSBO's first empty light slot in the first half of the SSBO's memory
     */
    private int computeFirstEmptySlot(){
        for(int i = 0; i < lights.size() / 2; i++){
            if(lights.get(i) == null){
                return i;
            }
        }
        return -1;
    }

    /**
     Binds the SSBO to the given binding point.

     @param bindingPoint binding point
     */
    public void bindTo(int bindingPoint){
        ssbo.bindToBindingPoint(bindingPoint);
    }

    /**
     Creates a new SSBO for the lights if the last SSBO isn't usable.
     */
    private void createSsbo(){
        if(!isUsable()){
            createSsboUnsafe();
            LOG.fine("Light SSBO created");
        }
    }

    /**
     Creates an SSBO for the lights.
     */
    private void createSsboUnsafe(){
        ssbo = new Ssbo(getClass().getSimpleName() + " " + getCenter().x() + " " + getCenter().y());
        ssbo.bind();
        ssbo.allocate(LIGHT_SIZE + LIGHT_SOURCES_OFFSET, BufferObject.BufferObjectUsage.STATIC_DRAW);
        ssbo.unbind();
    }

    /**
     Releases the tile including the SSBO.
     */
    public void release(){
        releaseSsbo();
        releaseLights();
        BlinnPhongLightSources.removeTile(tileCenter);
    }

    /**
     Releases the SSBO if it's usable.
     */
    private void releaseSsbo(){
        if(isUsable()){
            ssbo.release();
            ssbo = null;
            LOG.fine("Light SSBO released");
        }
    }

    /**
     Releases the light sources from the tile.
     */
    private void releaseLights(){
        for(BlinnPhongPositionalLightComponent light : lights){
            if(light != null){
                light.setShaderIndex(-1);
            }
        }
        lights.clear();
    }

    /**
     Returns true if the tile and the SSBO are usable, and false if they're released.

     @return true if the tile and the SSBO are usable, false otherwise
     */
    public boolean isUsable(){
        return Utility.isUsable(ssbo);
    }

    @Override
    public int hashCode(){
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.tileCenter);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final BlinnPhongLightSourceTile other = (BlinnPhongLightSourceTile) obj;
        if(!Objects.equals(this.tileCenter, other.tileCenter)){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(BlinnPhongLightSourceTile.class.getSimpleName()).append("(")
                .append(", tile center: ").append(tileCenter).append(", count: ").append(slotCount).append(")");
        return res.toString();
    }

}
