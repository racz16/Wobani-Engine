package resources.splines;

import java.util.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import toolbox.annotations.*;

/**
 * Represents a simple spline. It stores the control points and data you need to
 * render it.
 */
public class SimpleSpline implements Spline {

    /**
     * The spline's control points.
     */
    protected List<SplinePoint> controlPoints = new ArrayList<>();
    /**
     * The spline's VAO.
     */
    private transient Vao vao;
    /**
     * Determines whether the spline's data is valid.
     */
    protected boolean valid = true;
    /**
     * Determines whether the spline's length is valid.
     */
    protected boolean lengthValid = true;
    /**
     * The number of points stored in the VAO. This is not the number of the
     * control points.
     */
    private int numberOfPoints;
    /**
     * Determines whether the spline is a loop spline.
     */
    private boolean loopSpline;
    /**
     * Determines whether the spline's data is dynamic.
     */
    private boolean dynamic;
    /**
     * Axis alligned bounding box's min x, y and z values.
     */
    protected final Vector3f aabbMin = new Vector3f();
    /**
     * Axis alligned bounding box's max x, y and z values.
     */
    protected final Vector3f aabbMax = new Vector3f();
    /**
     * Furthest vertex distance.
     */
    protected float furthestVertexDistance;
    /**
     * The stored spline's size in bytes.
     */
    private int dataSize;
    /**
     * Approximated distances between the spline's control points.
     */
    private final List<Float> distances = new ArrayList<>();
    /**
     * The spline's approximated length.
     */
    protected float length;
    /**
     * The resource's unique id.
     */
    private final ResourceId resourceId;

    /**
     * Initializes a new SimpleSpline.
     */
    public SimpleSpline() {
        resourceId = new ResourceId();
        ResourceManager.addSpline(this);
    }

    /**
     * If the data stored in the VAO isn't valid, this method refreshes it.
     */
    protected void refresh() {
        if (!valid) {
            if (getRequiredControlPoints() <= getNumberOfControlPoints()) {
                if (vao == null) {
                    vao = new Vao();
                    vao.bindVao();
                    vao.createVbo("position");
                    vao.unbindVao();
                }
            } else {
                if (vao != null) {
                    release();
                }
                return;
            }

            float[] data = computeSplineData();
            numberOfPoints = data.length / 3;
            dataSize = numberOfPoints * 3 * 4;
            vao.bindVao();
            vao.bindAndAddData("position", 0, 3, data, isDynamic());
            vao.unbindVao();
            valid = true;
        }
    }

    /**
     * Returns the splie's data based on the control points and the type of the
     * spline. Also computes the AABB and the bounding sphere.
     *
     * @return the spline's data
     */
    @NotNull
    protected float[] computeSplineData() {
        Vector3f aabbMax = new Vector3f();
        Vector3f aabbMin = new Vector3f();
        float max = 0;

        float[] data = new float[getNumberOfControlPoints() * 3];
        int index = 0;
        for (int i = 0; i < getNumberOfControlPoints(); i++) {
            Vector3f pos = getControlPoint(i);

            //furthest vertex distance
            if (max < pos.length()) {
                max = pos.length();
            }
            //aabb
            for (int j = 0; j < 3; j++) {
                if (pos.get(j) < aabbMin.get(j)) {
                    aabbMin.setComponent(j, pos.get(j));
                }
                if (pos.get(j) > aabbMax.get(j)) {
                    aabbMax.setComponent(j, pos.get(j));
                }
            }

            data[index++] = pos.x;
            data[index++] = pos.y;
            data[index++] = pos.z;
        }

        this.aabbMin.set(aabbMin);
        this.aabbMax.set(aabbMax);
        furthestVertexDistance = max;

        return data;
    }

    @Nullable @ReadOnly
    @Override
    public Vector3f getForwardVector(float t) {
        if (getNumberOfControlPoints() < 2) {
            return null;
        } else {
            if (t >= 0.9999) {
                Vector3f p1 = getApproximatedPosition(t - 0.0001f);
                Vector3f p2 = getApproximatedPosition(t);
                return p2.sub(p1).normalize();
            } else {
                Vector3f p1 = getApproximatedPosition(t);
                Vector3f p2 = getApproximatedPosition(t + 0.0001f);
                return p2.sub(p1).normalize();
            }
        }
    }

    @Nullable @ReadOnly
    @Override
    public Vector3f getApproximatedPosition(float t) {
        t = t < 0 ? t % -1f : t % 1f;

        if (getNumberOfControlPoints() < 1) {
            return null;
        } else if (getVertexCount() == 1) {
            return getControlPoint(0);
        } else {
            refreshLength();

            if (t == 0) {
                return getValue(0, 0);
            }

            int index = 0;
            float dist = distances.get(0);
            float wantedDistance = length * t;

            while (dist < wantedDistance) {
                index++;
                dist += distances.get(index);
            }

            float T = (wantedDistance - (dist - distances.get(index))) / distances.get(index);

            return getValue(index, T);
        }
    }

    /**
     * Returns the point on the spline between the startIndexth and
     * startIndex+1th controlpoints, specified by the argument t. This method
     * returns to null if the spline doesn't contain any points. The startIndex
     * can't be lower than 0 and can't be higher than the control point's number
     * -1 (except if it's a loop spline).
     *
     * @param startIndex start control point's index
     * @param t "time"
     * @return the specified point of the spline
     *
     * @see #getNumberOfControlPoints()
     */
    @Nullable
    protected Vector3f getValue(int startIndex, float t) {
        if (getNumberOfControlPoints() < 1) {
            return null;
        } else if (getVertexCount() == 1) {
            return getControlPoint(0);
        } else {
            Vector3f first = new Vector3f(getControlPoint(startIndex)).mul(1 - t);
            Vector3f second = new Vector3f(getControlPoint(startIndex == getVertexCount() - 1 ? 0 : startIndex + 1));
            return first.add(second.mul(t));
        }
    }

    /**
     * If the stored length isn't valid, this method refreshes it.
     */
    protected void refreshLength() {
        if (!lengthValid) {
            distances.clear();
            float sum = 0;
            for (int i = 0; i < controlPoints.size() - 1; i++) {
                float dist = getControlPoint(i).distance(getControlPoint(i + 1));
                sum += dist;
                distances.add(dist);
            }
            if (isLoopSpline()) {
                float dist = getControlPoint(controlPoints.size() - 1).distance(getControlPoint(0));
                sum += dist;
                distances.add(dist);
            }

            length = sum;
            lengthValid = true;
        }
    }

    @Override
    public float getApproximatedLength() {
        refreshLength();
        return length;
    }

    /**
     * Determines whether the spline's data is dynamic.
     *
     * @return true if the spline's data is dynamic, false otherwise
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * Sets whether or not the spline is dynamic. It is advised to set the
     * spline to dynamic if you want to change the spline's data frequently.
     *
     * @param dynamic true if the spline's data should be dynamic, false
     * otherwise
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        valid = false;
    }

    /**
     * Determines whether the spline is a loop spline.
     *
     * @return true if the spline is loop spline, false otherwise
     */
    @Override
    public boolean isLoopSpline() {
        return loopSpline;
    }

    /**
     * Sets whether or not the spline is a loop spline.
     *
     * @param loop true if the spline should be a loop spline, false otherwise
     */
    public void setLoopSpline(boolean loop) {
        this.loopSpline = loop;
        valid = false;
        lengthValid = false;
    }

    /**
     * Returns the number of points stored in the VAO. This is not the number of
     * the control points.
     *
     * @return the number of points stored in the VAO
     * @see #getNumberOfControlPoints()
     */
    @Override
    public int getVertexCount() {
        return numberOfPoints;
    }

    //
    //control points------------------------------------------------------------
    //
    /**
     * Returns the number of the spline's control points. This is not the same
     * as the number of points stored in the VAO.
     *
     * @return number of the spline's control points
     *
     * @see #getVertexCount()
     */
    public int getNumberOfControlPoints() {
        return controlPoints.size();
    }

    /**
     * Adds the given control point to the control points' list.
     *
     * @param point control point
     */
    public void addControlPointToTheEnd(@NotNull Vector3f point) {
        controlPoints.add(new SplinePoint(new Vector3f(point)));
        valid = false;
        lengthValid = false;
    }

    /**
     * Adds the given control point to the given index.
     *
     * @param index control point's index
     * @param point control point
     */
    public void addControlPointToIndex(int index, @NotNull Vector3f point) {
        controlPoints.add(index, new SplinePoint(new Vector3f(point)));
        valid = false;
        lengthValid = false;
    }

    /**
     * Returns the specified control point.
     *
     * @param index control point's index
     * @return control point
     */
    @NotNull @ReadOnly
    public Vector3f getControlPoint(int index) {
        return new Vector3f(controlPoints.get(index).getPoint());
    }

    /**
     * Sets the specified control point to the given value.
     *
     * @param index contol point's index
     * @param point control point
     */
    public void setControlPoint(int index, @NotNull Vector3f point) {
        controlPoints.set(index, new SplinePoint(new Vector3f(point)));
        valid = false;
        lengthValid = false;
    }

    /**
     * Removes the last control point.
     */
    public void removeControlPointFromTheEnd() {
        controlPoints.remove(controlPoints.size() - 1);
        valid = false;
        lengthValid = false;
    }

    /**
     * Removes the specified control point.
     *
     * @param index control point's index
     */
    public void removeControlPoint(int index) {
        controlPoints.remove(index);
        valid = false;
        lengthValid = false;
    }

    /**
     * Removes all the spline's control points.
     */
    public void removeAllControlPoints() {
        controlPoints.clear();
        valid = false;
        lengthValid = false;
    }

    /**
     * Returns the required number of control points. If the spline contains
     * less control points, you can't render the spline.
     *
     * @return the required number of control points to render the spline
     */
    public int getRequiredControlPoints() {
        return 2;
    }

    //
    //frustum culling-----------------------------------------------------------
    //
    @Override
    public float getFurthestVertexDistance() {
        refresh();
        if (vao != null) {
            return furthestVertexDistance;
        } else {
            return 0;
        }
    }

    @Nullable @ReadOnly
    @Override
    public Vector3f getAabbMin() {
        refresh();
        if (vao != null) {
            return new Vector3f(aabbMin);
        } else {
            return null;
        }
    }

    @Nullable @ReadOnly
    @Override
    public Vector3f getAabbMax() {
        refresh();
        if (vao != null) {
            return new Vector3f(aabbMax);
        } else {
            return null;
        }
    }

    //
    //rendering
    //
    @Override
    public void beforeDraw() {
        if (vao != null && vao.isUsable()) {
            vao.bindVao();
        }
    }

    @Override
    public void draw() {
        boolean shouldBind = vao == null || !vao.isUsable();
        refresh();
        if (shouldBind) {
            beforeDraw();
        }
        GL11.glDrawArrays(isLoopSpline() ? GL11.GL_LINE_LOOP : GL11.GL_LINE_STRIP, 0, getVertexCount());
    }

    @Override
    public void afterDraw() {
        if (vao != null && vao.isUsable()) {
            vao.unbindVao();
        }
    }

    //
    //misc----------------------------------------------------------------------
    //
    @Override
    public int getDataSizeInRam() {
        return dataSize;
    }

    @Override
    public int getDataSizeInAction() {
        return vao == null || !vao.isUsable() ? 0 : dataSize;
    }

    @Override
    public void update() {

    }

    @Override
    public void release() {
        if (vao != null) {
            vao.release();
            vao = null;
        }
        valid = false;
        lengthValid = false;
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
        return "SimpleSpline{" + "controlPoints=" + controlPoints + ", vao=" + vao
                + ", valid=" + valid + ", lengthValid=" + lengthValid
                + ", numberOfPoints=" + numberOfPoints + ", loopSpline=" + loopSpline
                + ", dynamic=" + dynamic + ", aabbMin=" + aabbMin + ", aabbMax=" + aabbMax
                + ", furthestVertexDistance=" + furthestVertexDistance + ", dataSize=" + dataSize
                + ", distances=" + distances + ", length=" + length + ", resourceId=" + resourceId + '}';
    }

}
