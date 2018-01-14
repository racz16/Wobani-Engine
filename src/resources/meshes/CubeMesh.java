package resources.meshes;

import java.util.*;
import org.joml.Math;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import toolbox.annotations.*;

/**
 * A simple cube mesh. It can be useful for the skybox.
 */
public class CubeMesh implements Mesh {

    /**
     * The mesh's VAO.
     */
    private Vao vao;
    /**
     * The only CubeMesh instance.
     */
    private static CubeMesh instace;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     * Cube's vertex positions.
     */
    float[] positions = {
        -1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,
        -1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f
    };

    /**
     * Initializes a new CubeMesh.
     */
    private CubeMesh() {
        loadData();
        resourceId = new ResourceId();
        ResourceManager.addMesh(this);
    }

    /**
     * Returns the CubeMesh instance.
     *
     * @return the CubeMesh instance
     */
    @NotNull
    public static CubeMesh getInstance() {
        if (instace == null) {
            instace = new CubeMesh();
        }
        return instace;
    }

    /**
     * Loads the cube's data to the VAO (if the VAO doesn't store it already).
     */
    private void loadData() {
        if (vao == null || !vao.isUsable()) {
            vao = new Vao();
            vao.bindVao();
            //position
            vao.createVbo("position");
            vao.bindAndAddData("position", 0, 3, positions, false);
            vao.unbindVao();
        }
    }

    @Override
    public int getVertexCount() {
        return 36;
    }

    @Override
    public int getFaceCount() {
        return 12;
    }

    @Override
    public float getRadius() {
        return (float) Math.sqrt(3);
    }

    @NotNull @ReadOnly
    @Override
    public Vector3f getAabbMin() {
        return new Vector3f(-1, -1, -1);
    }

    @NotNull @ReadOnly
    @Override
    public Vector3f getAabbMax() {
        return new Vector3f(1, 1, 1);
    }

    @Override
    public void beforeDraw() {
        if (vao != null && vao.isUsable()) {
            vao.bindVao();
        }
    }

    @Override
    public void draw() {
        if (vao == null || !vao.isUsable()) {
            loadData();
            vao.bindVao();
        }
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, getVertexCount());
    }

    @Override
    public void afterDraw() {
        if (vao != null && vao.isUsable()) {
            vao.unbindVao();
        }
    }

    @Override
    public int getDataSizeInRam() {
        return positions.length * 4;
    }

    @Override
    public int getDataSizeInAction() {
        return vao == null || !vao.isUsable() ? 0 : positions.length * 4;
    }

    @Override
    public void update() {

    }

    @Override
    public void release() {
        vao.release();
        vao = null;
    }

    @NotNull
    @Override
    public ResourceId getResourceId() {
        return resourceId;
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public String toString() {
        return "CubeMesh{" + "vao=" + vao + ", resourceId=" + resourceId
                + ", positions=" + Arrays.toString(positions) + '}';
    }

}
