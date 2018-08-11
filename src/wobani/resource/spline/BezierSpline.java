package wobani.resource.spline;

import org.joml.*;
import wobani.toolbox.annotation.*;

/**
 Implementation of the Bezier spline.
 */
public class BezierSpline extends CubicSpline{

    /**
     Initializes a new BezierSpline.
     <p>
     */
    public BezierSpline(){
        super();
        computeBasisMatrix();
    }

    @Nullable
    @Override
    protected Vector3f getValue(int startIndex, float t){
        if(getNumberOfControlPoints() < getRequiredControlPoints()){
            return super.getValue(startIndex, t);
        }else{
            Vector4f vec = new Vector4f(t * t * t, t * t, t, 1).mul(basisMatrix);
            Vector3f[] cps = new Vector3f[4];
            cps[0] = controlPoints.get(startIndex).getPoint();
            cps[1] = controlPoints.get(startIndex).getRight();
            if(startIndex + 1 == getNumberOfControlPoints() && isLoopSpline()){
                cps[2] = controlPoints.get(0).getLeft();
                cps[3] = controlPoints.get(0).getPoint();
            }else{
                cps[2] = controlPoints.get(startIndex + 1).getLeft();
                cps[3] = controlPoints.get(startIndex + 1).getPoint();
            }

            Vector4f v1 = new Vector4f(cps[0].x, cps[1].x, cps[2].x, cps[3].x);
            Vector4f v2 = new Vector4f(cps[0].y, cps[1].y, cps[2].y, cps[3].y);
            Vector4f v3 = new Vector4f(cps[0].z, cps[1].z, cps[2].z, cps[3].z);

            return new Vector3f(vec.dot(v1), vec.dot(v2), vec.dot(v3));
        }
    }

    /**
     Returns the specified control point's left helper point.

     @param index specifies the control point

     @return the specified control point's left helper point
     */
    @NotNull
    @ReadOnly
    public Vector3f getLeftHelperPoint(int index){
        return new Vector3f(controlPoints.get(index).getLeft());
    }

    /**
     Returns the specified control point's right helper point.

     @param index specifies the control point

     @return the specified control point's right helper point
     */
    @NotNull
    @ReadOnly
    public Vector3f getRighHelperPoint(int index){
        return new Vector3f(controlPoints.get(index).getRight());
    }

    /**
     Sets the specified control point's left helper point to the given value.

     @param index       specifies the control point
     @param helperPoint left helper point
     */
    public void setLeftHelperPoint(int index, @NotNull Vector3f helperPoint){
        controlPoints.get(index).setLeft(new Vector3f(helperPoint));
        valid = false;
    }

    /**
     Sets the specified control point's right helper point to the given value.

     @param index       specifies the control point
     @param helperPoint right helper point
     */
    public void setRightHelperPoint(int index, @NotNull Vector3f helperPoint){
        controlPoints.get(index).setRight(new Vector3f(helperPoint));
        valid = false;
    }

    /**
     This method helps you if you don't want to give each control point's helper points by hand. It replaces all the
     helper points accoarding to the line indicated by the previous and the next control point.

     @param distance distance between the control point and it's helper points
     */
    public void normalizeHelperPoints(float distance){
        if(getNumberOfControlPoints() < getRequiredControlPoints()){
            return;
        }
        //first
        Vector3f direction;
        if(isLoopSpline()){
            direction = getControlPoint(controlPoints.size() - 1).sub(getControlPoint(1)).normalize();
        }else{
            direction = getControlPoint(0).sub(getControlPoint(1)).normalize();
        }
        controlPoints.get(0).setLeft(getControlPoint(0).add(direction.mul(distance)));
        //normal control points
        for(int i = 1; i < controlPoints.size() - 1; i++){
            direction = getControlPoint(i - 1).sub(getControlPoint(i + 1)).normalize();
            controlPoints.get(i).setLeft(getControlPoint(i).add(direction.mul(distance)));
        }
        //last
        if(isLoopSpline()){
            direction = getControlPoint(controlPoints.size() - 2).sub(getControlPoint(0)).normalize();
        }else{
            direction = getControlPoint(controlPoints.size() - 2).sub(getControlPoint(controlPoints.size() - 1))
                    .normalize();
        }
        controlPoints.get(controlPoints.size() - 1)
                .setLeft(getControlPoint(controlPoints.size() - 1).add(direction.mul(distance)));
        valid = false;
    }

    @Override
    protected void computeBasisMatrix(){
        basisMatrix.set(-1, 3, -3, 1, 3, -6, 3, 0, -3, 3, 0, 0, 1, 0, 0, 0);
        valid = false;
    }

}
