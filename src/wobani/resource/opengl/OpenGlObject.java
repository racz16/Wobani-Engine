package wobani.resource.opengl;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import static wobani.resource.ExceptionHelper.*;

/**
 Super class of all OpenGL Objects.
 */
public abstract class OpenGlObject implements Resource{
    /**
     The OpenGL Object's unique resource id.
     */
    private final ResourceId resourceId;
    /**
     The OpenGL Object's native id.
     */
    private int id;
    /**
     The OpenGL Object's data size (in bytes).
     */
    private int dataSize;
    /**
     The OpenGL Object's label.
     */
    private String label = "";

    /**
     Initializes a new OpenGlObject to the given value.

     @param resourceId resource id

     @throws NullPointerException the resourceId parameter can't be null
     */
    public OpenGlObject(@NotNull ResourceId resourceId){
        exceptionIfNull(resourceId);
        id = INVALID_ID;
        this.resourceId = resourceId;
        ResourceManager.addResource(this);
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    /**
     Returns the native OpenGL id.

     @return the native OpenGL id
     */
    @Internal
    public int getId(){
        return id;
    }

    /**
     Sets the OpenGL Object's id to the given value.

     @param id native OpenGL id
     */
    protected void setId(int id){
        exceptionIfLowerOrEquals(0, id);
        this.id = id;
    }

    /**
     Sets the OpenGL Object's id to -1 signing that the it is not available.
     */
    protected void setIdToInvalid(){
        this.id = INVALID_ID;
    }

    @Override
    public int getActiveDataSize(){
        return dataSize;
    }

    /**
     Sets the OpenGL Object data size to the given value.

     @param dataSize data size (in bytes)
     */
    protected void setActiveDataSize(int dataSize){
        exceptionIfLower(0, dataSize);
        this.dataSize = dataSize;
    }

    /**
     Determines whether the OpenGL Object is existing. It may returns different value than {@link #isUsable()} because an
     object can be usable even if it's OpenGL Objects not exists (for example it can reload it's data from file).

     @return true if the OpenGL Objects exists, false otherwise
     */
    public boolean isAvailable(){
        return this.id != INVALID_ID;
    }

    /**
     Returns the maximal allowed length of a label in the current GPU.

     @return the maximal allowed length of a label
     */
    public static int getMaxLabelLength(){
        return OpenGlConstants.MAX_LABEL_LENGTH;
    }

    /**
     Returns the OpenGL Object's label.

     @return the OpenGL Object's label
     */
    @NotNull
    public String getLabel(){
        return label;
    }

    /**
     Sets the OpenGL Object's label to the given value.

     @param label label

     @see #getMaxLabelLength()
     */
    public void setLabel(@NotNull String label){
        exceptionIfNotAvailable(this);
        exceptionIfNull(label);
        exceptionIfLower(label.length(), getMaxLabelLength());
        setLabelUnsafe(label);
    }

    /**
     Sets the OpenGL Object's label to the given value.

     @param label label
     */
    private void setLabelUnsafe(@NotNull String label){
        //TODO: label all opengl objects
        this.label = label;
        GL43.glObjectLabel(getType(), getId(), getTypeName() + " " + label);
    }

    /**
     Returns the OpenGL Object's type.

     @return the OpenGL Object's type
     */
    protected abstract int getType();

    /**
     Returns the OpenGL Object's readable String name.

     @return the OpenGL Object's readable String name
     */
    @NotNull
    protected abstract String getTypeName();

    @Override
    public void update(){

    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        OpenGlObject that = (OpenGlObject) o;

        return resourceId.equals(that.resourceId);
    }

    @Override
    public int hashCode(){
        return resourceId.hashCode();
    }

    @Override
    public String toString(){
        return OpenGlObject.class.getSimpleName() + "(" +
                "resourceId: " + resourceId + ", " +
                "id: " + id + ", " +
                "dataSize: " + dataSize + ", " +
                "label: " + label + ")";
    }

}
