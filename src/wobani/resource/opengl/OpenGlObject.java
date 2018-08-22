package wobani.resource.opengl;

import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.exceptions.*;

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
     Invalid id signs that the OpenGL Object is not available.
     */
    private static final int INVALID_ID = -1;

    /**
     Initializes a new OpenGlObject to the given value.

     @param resourceId resource id

     @throws NullPointerException the resourceId parameter can't be null
     */
    public OpenGlObject(@NotNull ResourceId resourceId){
        if(resourceId == null){
            throw new NullPointerException("The resourceId parameter can't be null");
        }
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
    protected int getId(){
        return id;
    }

    /**
     Sets the OpenGL Object's id to the given value.

     @param id native OpenGL id

     @throws IllegalArgumentException if the id id isn't higher than 0
     */
    protected void setId(int id){
        if(id <= 0){
            throw new IllegalArgumentException("The id id isn't higher than 0");
        }
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

     @throws IllegalArgumentException if data size is lower than 0
     */
    protected void setActiveDataSize(int dataSize){
        if(dataSize < 0){
            throw new IllegalArgumentException("Data size is lower than 0");
        }
        this.dataSize = dataSize;
    }

    /**
     Determines whether the OpenGL Object is existing. It may returns different value than {@link #isUsable()} because an
     object can be usable even if it's OpenGL Objects not exists (for example it can reload it's data from file).

     @return true if the OpenGL Objects exists, false otherwise
     */
    protected boolean isIdValid(){
        return this.id != INVALID_ID;
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

     @throws IllegalArgumentException if the parameter is longer than the maximum length
     @see OpenGlConstants#MAX_LABEL_LENGTH
     */
    public void setLabel(@NotNull String label){
        checkRelease();
        if(OpenGlConstants.MAX_LABEL_LENGTH < label.length()){
            throw new IllegalArgumentException("The parameter is longer than the maximum length");
        }
        setLabelUnsafe(label);
    }

    /**
     Sets the OpenGL Object's label to the given value without inspections.

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

    /**
     If the OpenGL Object is released it throws a ReleasedException.

     @throws ReleasedException if the OpenGL Object is released
     */
    protected void checkRelease(){
        if(!isUsable()){
            throw new ReleasedException(this);
        }
    }

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
