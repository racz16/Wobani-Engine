package wobani.resource.mesh;

import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import wobani.component.renderable.*;
import wobani.core.*;
import wobani.resource.*;
import wobani.resource.ResourceManager.*;
import wobani.resource.opengl.buffer.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.io.*;
import java.nio.*;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

/**
 Stores a mesh's data. You can load a mesh only once, if you try to load it twice, you get reference to the already
 loaded one. You can specify the StaticMesh's data store policy including when and where the data should be stored.

 @see #loadModel(File path) */
public class StaticMesh implements Mesh{

    /**
     Vertex count.
     */
    private final int vertexCount;
    /**
     Triangle count.
     */
    private final int faceCount;
    /**
     Axis alligned bounding box's min x, y and z values.
     */
    private final Vector3f aabbMin = new Vector3f();
    /**
     Axis alligned bounding box's max x, y and z values.
     */
    private final Vector3f aabbMax = new Vector3f();
    /**
     Stores meta data about this mesh.
     */
    private final DataStoreManager meta = new DataStoreManager();
    /**
     The resource's unique id.
     */
    private final ResourceId resourceId;
    /**
     The mesh's VAO.
     */
    private Vao vao;
    /**
     Furthest vertex distance.
     */
    private float furthestVertexDistance;
    /**
     Stores the mesh's position data.
     */
    private AIVector3D.Buffer position;
    /**
     Stores the mesh's texture coordinate data.
     */
    private AIVector3D.Buffer uv;
    /**
     Stores the mesh's normal vector data.
     */
    private AIVector3D.Buffer normal;
    /**
     Stores the mesh's tangent vector data.
     */
    private AIVector3D.Buffer tangent;
    /**
     Stores the mesh's index data.
     */
    private IntBuffer indices;

    /**
     Initializes a new StaticMesh to the given values.

     @param mesh       mesh
     @param path       model's relative path (with extension like "res/models/myModel.obj")
     @param resourceId the mesh's id
     */
    private StaticMesh(@NotNull AIMesh mesh, @NotNull File path, @NotNull ResourceId resourceId){
        faceCount = mesh.mNumFaces();
        vertexCount = faceCount * 3;
        computeFrustumCullingData(mesh);
        meta.setPaths(Utility.wrapObjectByList(path));
        meta.setLastActiveToNow();
        meta.setDataStorePolicy(ResourceState.ACTIVE);

        hddToRam(mesh);
        ramToVram();

        computeDataSize();
        this.resourceId = resourceId;
        ResourceManager.addResource(this);
    }

    //
    //loading-saving------------------------------------------------------------
    //

    /**
     Loads a model from the given path into meshes. You can load a mesh only once, if you try to load it twice, you get
     reference to the already loaded one.

     @param path model's relative path (with extension like "res/models/myModel.obj")

     @return list of model's meshes
     */
    @NotNull
    public static List<StaticMesh> loadModel(@NotNull File path){
        AIScene scene = getSceneAssimp(path);
        List<StaticMesh> meshes = new ArrayList<>();
        int meshCount = scene.mNumMeshes();
        PointerBuffer meshesBuffer = scene.mMeshes();

        List<ResourceId> ids = ResourceId.getResourceIds(path, meshCount);
        for(int i = 0; i < meshCount; ++i){
            StaticMesh me = ResourceManager.getResource(new ResourceId(path), StaticMesh.class);
            //StaticMesh me = (StaticMesh) ResourceManager.getMesh(new ResourceId(path));
            if(me == null){
                me = new StaticMesh(AIMesh.create(meshesBuffer.get(i)), path, ids.get(i));
            }
            meshes.add(me);
        }
        return meshes;
    }

    /**
     Loads a model from the given path into meshes, and adds each mesh as a OldMeshComponent to it's own GameObject.

     @param path model's relative path (with extension like "res/models/myModel.obj")

     @return list of GameObjects
     */
    @NotNull
    public static List<GameObject> loadModelToGameObjects(@NotNull File path){
        List<GameObject> list = new ArrayList<>();
        for(StaticMesh me : loadModel(path)){
            GameObject g = new GameObject();
            g.getComponents().add(new MeshComponent(me));
            list.add(g);
        }
        return list;
    }

    /**
     Loads a model from the given path into meshes, and adds all the meshes as MeshComponents to a single GameObject.

     @param path model's relative path (with extension like "res/models/myModel.obj")

     @return GameObject
     */
    @NotNull
    public static GameObject loadModelToGameObject(@NotNull File path){
        GameObject g = new GameObject();
        for(StaticMesh me : loadModel(path)){
            g.getComponents().add(new MeshComponent(me));
        }
        return g;
    }

    /**
     Returns the model's scene stored in the given path.

     @param path model's relative path (with extension like "res/models/myModel.obj")

     @return model's scene

     @throws IllegalStateException if assimp can't load the data from the file
     */
    @NotNull
    private static AIScene getSceneAssimp(@NotNull File path){
        AIScene scene = aiImportFile(path
                .getPath(), aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_CalcTangentSpace);
        if(scene == null){
            throw new IllegalStateException(aiGetErrorString());
        }
        return scene;
    }

    /**
     Computes the mesh's size.
     */
    private void computeDataSize(){
        int FLOAT_SIZE = 4;
        int INT_SIZE = 4;

        int size = 0;
        size += position.capacity() * FLOAT_SIZE;
        size += uv.capacity() * FLOAT_SIZE;
        size += normal.capacity() * FLOAT_SIZE;
        size += tangent.capacity() * FLOAT_SIZE;
        size += indices.capacity() * INT_SIZE;

        meta.setDataSize(size);
    }

    /**
     Computes the mesh's indices buffer.

     @param mesh mesh

     @return indices buffer

     @throws IllegalStateException a face is not a triangle
     */
    @NotNull
    private IntBuffer computeIndicesBuffer(@NotNull AIMesh mesh){
        AIFace.Buffer facesBuffer = mesh.mFaces();
        IntBuffer elementArrayBufferData = MemoryUtil.memAllocInt(vertexCount);
        for(int j = 0; j < faceCount; ++j){
            AIFace face = facesBuffer.get(j);
            if(face.mNumIndices() != 3){
                throw new IllegalStateException("AIFace.mNumIndices() != 3");
            }
            elementArrayBufferData.put(face.mIndices());
        }
        elementArrayBufferData.flip();
        return elementArrayBufferData;
    }

    /**
     Computes the mesh's axis alligned bounding box and it's furthest vertex distance.

     @param mesh mesh
     */
    private void computeFrustumCullingData(@NotNull AIMesh mesh){
        float max = 0;
        Vector3f aabbMax = new Vector3f();
        Vector3f aabbMin = new Vector3f();
        Vector3f currentVec = new Vector3f();

        for(int i = 0; i < mesh.mVertices().limit(); i++){
            currentVec.set(mesh.mVertices().get(i).x(), mesh.mVertices().get(i).y(), mesh.mVertices().get(i).z());
            //furthest vertex distance
            if(max < currentVec.length()){
                max = currentVec.length();
            }
            //aabb
            for(int j = 0; j < 3; j++){
                if(currentVec.get(j) < aabbMin.get(j)){
                    aabbMin.setComponent(j, currentVec.get(j));
                }
                if(currentVec.get(j) > aabbMax.get(j)){
                    aabbMax.setComponent(j, currentVec.get(j));
                }
            }
        }

        this.aabbMin.set(aabbMin);
        this.aabbMax.set(aabbMax);
        furthestVertexDistance = max;
    }

    /**
     Loads the mesh's data from file to the CACHE. It doesn't compute AABB and furthest vertex distance again.
     */
    private void hddToRam(){
        AIMesh mesh = AIMesh.create(getSceneAssimp(getPath()).mMeshes().get(resourceId.getIndex()));
        hddToRam(mesh);
    }

    /**
     Loads the mesh's data from the given parameter to the CACHE. It doesn't compute AABB and furthest vertex distance
     again.

     @param mesh mesh
     */
    private void hddToRam(@NotNull AIMesh mesh){
        indices = computeIndicesBuffer(mesh);
        position = mesh.mVertices();
        uv = mesh.mTextureCoords(0);
        normal = mesh.mNormals();
        tangent = mesh.mTangents();

        meta.setState(ResourceState.CACHE);
    }

    /**
     Loads the mesh's data from the CACHE to the ACTIVE. It may cause errors if the data isn't in the CACHE.
     */
    private void ramToVram(){
        vao = new Vao(getClass().getSimpleName() + " " + getPath());

        /*List<Float> pos = new ArrayList<>();
        while (position.remaining() > 0) {
            AIVector3D vec = position.get();
            pos.add(vec.x());
            pos.add(vec.y());
            pos.add(vec.z());
        }*/
        Vbo positionVbo = new Vbo(getClass().getSimpleName() + " " + getPath() + " position");
        positionVbo.allocateAndStoreImmutable(position, false);
        vao.connectVbo(positionVbo, new VertexAttribPointer(0, 3));

        /*List<Float> uvs = new ArrayList<>();
        while (uv.remaining() > 0) {
            AIVector3D vec = uv.get();
            uvs.add(vec.x());
            uvs.add(vec.y());
            uvs.add(vec.z());
        }*/
        Vbo uvVbo = new Vbo(getClass().getSimpleName() + " " + getPath() + " uv");
        uvVbo.allocateAndStoreImmutable(uv, false);
        vao.connectVbo(uvVbo, new VertexAttribPointer(1, 3));

        Vbo normalVbo = new Vbo(getClass().getSimpleName() + " " + getPath() + " normal");
        normalVbo.allocateAndStoreImmutable(normal, false);
        vao.connectVbo(normalVbo, new VertexAttribPointer(2, 3));

        Vbo tangentVbo = new Vbo(getClass().getSimpleName() + " " + getPath() + " tangent");
        tangentVbo.allocateAndStoreImmutable(tangent, false);
        vao.connectVbo(tangentVbo, new VertexAttribPointer(3, 3));

        Ebo ebo = new Ebo();
        ebo.allocateAndStoreImmutable(indices, false);
        vao.connectEbo(ebo);

        meta.setState(ResourceState.ACTIVE);
    }

    /**
     Removes the mesh's data from the ACTIVE. It may cause errors if the data isn't in the ACTIVE.
     */
    private void vramToRam(){
        vao.release();
        vao = null;

        meta.setState(ResourceState.CACHE);
    }

    /**
     Removes the mesh's data from the CACHE. It may cause errors if the data isn't in the CACHE.
     */
    private void ramToHdd(){
        position = null;
        uv = null;
        normal = null;
        tangent = null;
        MemoryUtil.memFree(indices);
        indices = null;

        meta.setState(ResourceState.STORAGE);
    }

    //
    //opengl related------------------------------------------------------------
    //
    @Override
    public void beforeDraw(){
        if(getState() == ResourceState.ACTIVE){
            vao.bind();
        }
    }

    @Override
    public void draw(){
        if(getState() != ResourceState.ACTIVE){
            if(getState() == ResourceState.STORAGE){
                hddToRam();
            }
            ramToVram();
            vao.bind();
        }
        GL11.glDrawElements(GL11.GL_TRIANGLES, getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        meta.setLastActiveToNow();
    }

    @Override
    public void afterDraw(){
        if(getState() == ResourceState.ACTIVE){
            vao.unbind();
        }
    }

    //
    //data store----------------------------------------------------------------
    //

    /**
     Returns the ACTIVE time limit. If the elapsed time since this mesh's last use is higher than this value and the
     mesh's data store policy is CACHE or STORAGE, the mesh's data may be removed from ACTIVE. Later if you want to render this
     mesh, it'll automatically load the data from file again.

     @return ACTIVE time limit (in miliseconds)
     */
    public long getVramTimeLimit(){
        return meta.getActiveTimeLimit();
    }

    /**
     Sets the ACTIVE time limit to the given value. If the elapsed time since this mesh's last use is higher than this
     value and the mesh's data store policy is CACHE or STORAGE, the mesh's data may be removed from ACTIVE. Later if you want
     to render this mesh, it'll automatically load the data from file again.

     @param vramTimeLimit ACTIVE time limit (in miliseconds)
     */
    public void setVramTimeLimit(long vramTimeLimit){
        meta.setActionTimeLimit(vramTimeLimit);
    }

    /**
     Returns the CACHE time limit. If the elapsed time since this mesh's last use is higher than this value and the mesh's
     data store policy is STORAGE, the mesh's data may be removed from ACTIVE or even from CACHE. Later if you want to render
     this mesh, it'll automatically load the data from file again.

     @return CACHE time limit (in miliseconds)
     */
    public long getRamTimeLimit(){
        return meta.getCacheTimeLimit();
    }

    /**
     Sets the CACHE time limit to the given value. If the elapsed time since this mesh's last use is higher than this value
     and the mesh's data store policy is STORAGE, the mesh's data may be removed from ACTIVE or even from CACHE. Later if you
     want to render this mesh, it'll automatically load the data from file again.

     @param ramTimeLimit CACHE time limit (in miliseconds)
     */
    public void setRamTimeLimit(long ramTimeLimit){
        meta.setCacheTimeLimit(ramTimeLimit);
    }

    /**
     Returns the mesh's state. It determines where the mesh is currently stored.

     @return the mesh's state
     */
    @NotNull
    public ResourceState getState(){
        return meta.getState();
    }

    /**
     Returns the mesh's data store policy. ACTIVE means that the mesh's data will be stored in ACTIVE. CACHE means that the
     mesh's data may be removed from ACTIVE to CACHE if it's rarely used. STORAGE means that the mesh's data may be removed
     from ACTIVE or even from CACHE if it's rarely used. Later if you want to render this mesh, it'll automatically load
     the data from file again.

     @return the mesh's data store policy
     */
    @NotNull
    public ResourceState getDataStorePolicy(){
        return meta.getDataStorePolicy();
    }

    /**
     Sets the mesh's data store policy to the given value. ACTIVE means that the mesh's data will be stored in ACTIVE.
     CACHE means that the mesh's data may be removed from ACTIVE to CACHE if it's rarely rendered. STORAGE means that the mesh's
     data may be removed from ACTIVE or even from CACHE if it's rarely rendered. Later if you want to render this mesh,
     it'll automatically load the data from file again.

     @param minState data store policy
     */
    public void setDataStorePolicy(@NotNull ResourceState minState){
        meta.setDataStorePolicy(minState);

        if(minState != ResourceState.STORAGE && getState() == ResourceState.STORAGE){
            hddToRam();
        }
        if(minState == ResourceState.ACTIVE && getState() != ResourceState.ACTIVE){
            ramToVram();
        }
    }

    /**
     Returns the time when the mesh last time used.

     @return the time when the mesh last time used (in miliseconds)
     */
    public long getLastActive(){
        return meta.getLastActive();
    }

    @Override
    public void update(){
        long elapsedTime = System.currentTimeMillis() - getLastActive();
        if(elapsedTime > getVramTimeLimit() && getDataStorePolicy() != ResourceState.ACTIVE && getState() != ResourceState.STORAGE){
            if(getState() == ResourceState.ACTIVE){
                vramToRam();
            }
            if(elapsedTime > getRamTimeLimit() && getDataStorePolicy() == ResourceState.STORAGE){
                ramToHdd();
            }
        }
    }

    @Override
    public void release(){
        if(getState() == ResourceState.ACTIVE){
            vramToRam();
        }
        if(getState() == ResourceState.CACHE){
            ramToHdd();
        }
    }

    @NotNull
    @Override
    public ResourceId getResourceId(){
        return resourceId;
    }

    //
    //misc----------------------------------------------------------------------
    //

    /**
     Returns the loaded model's path.

     @return the loaded model's path
     */
    @NotNull
    public File getPath(){
        return meta.getPaths().get(0);
    }

    /**
     Returns the mesh's index in the loaded model.

     @return the mesh's index in the loaded model
     */
    public int getIndex(){
        return resourceId.getIndex();
    }

    @Override
    public int getCacheDataSize(){
        return getState() == ResourceState.STORAGE ? 0 : meta.getDataSize();
    }

    @Override
    public int getActiveDataSize(){
        return getState() == ResourceState.ACTIVE ? meta.getDataSize() : 0;
    }

    @Override
    public int getVertexCount(){
        return vertexCount;
    }

    @Override
    public int getFaceCount(){
        return faceCount;
    }

    @Override
    public boolean isUsable(){
        return true;
    }

    @Override
    public float getRadius(){
        return furthestVertexDistance;
    }

    @NotNull
    @ReadOnly
    @Override
    public Vector3f getAabbMax(){
        return new Vector3f(aabbMax);
    }

    @NotNull
    @ReadOnly
    @Override
    public Vector3f getAabbMin(){
        return new Vector3f(aabbMin);
    }

    @Override
    public String toString(){
        return "StaticMesh{" + "vao=" + vao + ", vertexCount=" + vertexCount + ", faceCount=" + faceCount + ", furthestVertexDistance=" + furthestVertexDistance + ", aabbMin=" + aabbMin + ", aabbMax=" + aabbMax + ", position=" + position + ", uv=" + uv + ", normal=" + normal + ", tangent=" + tangent + ", indices=" + indices + ", meta=" + meta + ", resourceId=" + resourceId + '}';
    }

}
