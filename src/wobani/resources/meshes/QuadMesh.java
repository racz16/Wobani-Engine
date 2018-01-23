package wobani.resources.meshes;

import org.joml.Math;
import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resources.*;
import wobani.toolbox.annotations.*;

/**
 * A simple quad mesh. It can be useful for particles or bilboards. If you
 * render it in screen space, it's a fullscreen quad.
 */
public class QuadMesh implements Mesh {

    /**
     * The mesh's VAO.
     */
    private Vao vao;
    /**
     * The only QuadMesh instance.
     */
    private static QuadMesh instace;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new QuadMesh.
     */
    private QuadMesh() {
        loadData();
        resourceId = new ResourceId();
        ResourceManager.addMesh(this);
    }

    /**
     * Returns the QuadMesh instance.
     *
     * @return the QuadMesh instance
     */
    @NotNull
    public static QuadMesh getInstance() {
        if (instace == null) {
            instace = new QuadMesh();
        }
        return instace;
    }

    /**
     * Loads the quad's data to the VAO (if the VAO doesn't store it already).
     */
    private void loadData() {
        if (vao == null || !vao.isUsable()) {
            float[] positions = new float[12];
            //top left
            positions[0] = -1;
            positions[1] = 1;
            positions[2] = 0;
            //top right
            positions[3] = 1;
            positions[4] = 1;
            positions[5] = 0;
            //bottom left
            positions[6] = -1;
            positions[7] = -1;
            positions[8] = 0;
            //bottom right
            positions[9] = 1;
            positions[10] = -1;
            positions[11] = 0;

            vao = new Vao();
            vao.bindVao();
            //inicies
            int[] indices = {0, 2, 3, 1, 0, 3};
            vao.createEbo();
            vao.bindEbo();
            vao.addIndices(indices, false);
            //position
            vao.createVbo("position");
            vao.bindAndAddData("position", 0, 3, positions, false);
            //uv
            float[] uv = {0, 1,
                1, 1,
                0, 0,
                1, 0};
            vao.createVbo("uv");
            vao.bindAndAddData("uv", 1, 2, uv, false);
            vao.unbindVao();
        }
    }

    @Override
    public int getVertexCount() {
        return 6;
    }

    @Override
    public int getFaceCount() {
        return 2;
    }

    @Override
    public float getRadius() {
        return (float) Math.sqrt(2);
    }

    @NotNull @ReadOnly
    @Override
    public Vector3f getAabbMin() {
        return new Vector3f(-1, -1, 0);
    }

    @NotNull @ReadOnly
    @Override
    public Vector3f getAabbMax() {
        return new Vector3f(1, 1, 0);
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
        GL11.glDrawElements(GL11.GL_TRIANGLES, getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void afterDraw() {
        if (vao != null && vao.isUsable()) {
            vao.unbindVao();
        }
    }

    @Override
    public int getDataSizeInRam() {
        return 0;
    }

    @Override
    public int getDataSizeInAction() {
        return vao == null || !vao.isUsable() ? 0 : 104;
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
        return "QuadMesh{" + "vao=" + vao + ", resourceId=" + resourceId + '}';
    }

}
