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
    private int id = -1;
    /**
     The OpenGL Object's label.
     */
    private String label = "";

    /**
     Initializes a new OpenGlObject.
     */
    public OpenGlObject(){
        id = createId();
        resourceId = createResourceId();
        ResourceManager.addResource(this);
    }

    /**
     Returns a new, native OpenGL id.

     @return a new, native OpenGL id
     */
    protected abstract int createId();

    /**
     Returns a new, unique resource id.

     @return a new, unique resource id
     */
    @NotNull
    protected abstract ResourceId createResourceId();

    /**
     Returns the OpenGL Object's native id.

     @return the OpenGL Object's native id

     @see #getResourceId()
     */
    protected int getId(){
        return id;
    }

    /**
     Sets the OpenGL Object's id to the given value.

     @param id OpenGL Object's id
     */
    protected void setId(int id){
        this.id = id;
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
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
        return OpenGlObject.class
                .getSimpleName() + "(" + "resourceId: " + resourceId + ", " + "id: " + id + ", " + "label: " + label + ")";
    }

}
