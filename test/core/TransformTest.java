package core;

import org.joml.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;

public class TransformTest {

    private final float DELTA_FLOAT = 0.0001f;

    public TransformTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void beforeEachTest() {

    }

    @After
    public void afterEachTest() {
    }

    //
    //position------------------------------------------------------------------
    //
    @Test
    public void testRelativePosition() {
        Transform t = new Transform();

        //change relative position
        t.setRelativePosition(new Vector3f(0, -3, 353.5f));
        assertEquals(0, t.getRelativePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getRelativePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getRelativePosition().z(), DELTA_FLOAT);

        //change relative position
        t.setRelativePosition(new Vector3f(3472.111f, -3, 5));
        assertEquals(3472.111f, t.getRelativePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getRelativePosition().y(), DELTA_FLOAT);
        assertEquals(5, t.getRelativePosition().z(), DELTA_FLOAT);

        //realtive position = absolute position
        assertEquals(3472.111f, t.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(5, t.getAbsolutePosition().z(), DELTA_FLOAT);
    }

    @Test
    public void testRelativePositionDefensiveCopy() {
        Transform t = new Transform();
        Vector3f position = new Vector3f(0, -3, 353.5f);
        t.setRelativePosition(position);

        //change parameter
        position.set(3472.111f, -3, 5);
        assertEquals(0, t.getRelativePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getRelativePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getRelativePosition().z(), DELTA_FLOAT);

        //change return value
        position = t.getRelativePosition();
        position.set(3472.111f, -3, 5);
        assertEquals(0, t.getRelativePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getRelativePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getRelativePosition().z(), DELTA_FLOAT);
    }

    @Test(expected = NullPointerException.class)
    public void testRelativePositionNull() {
        Transform t = new Transform();

        t.setRelativePosition(null);
    }

    @Test
    public void testAbsolutePosition() {
        GameObject g1 = new GameObject();
        GameObject g2 = new GameObject();
        g2.setParent(g1);
        Transform t1 = g1.getTransform();
        Transform t2 = g2.getTransform();

        //change child absolute position
        t2.setAbsolutePosition(new Vector3f(0, -3, 353.5f));
        assertEquals(0, t1.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(0, t1.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(0, t1.getAbsolutePosition().z(), DELTA_FLOAT);
        assertEquals(0, t2.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t2.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t2.getAbsolutePosition().z(), DELTA_FLOAT);

        //change parent relative position
        t1.setRelativePosition(new Vector3f(3472.111f, -3, 5));
        assertEquals(3472.111f, t1.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t1.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(5, t1.getAbsolutePosition().z(), DELTA_FLOAT);
        assertEquals(3472.111f, t2.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-6, t2.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(358.5f, t2.getAbsolutePosition().z(), DELTA_FLOAT);

        //change parent absolute position
        t1.setAbsolutePosition(new Vector3f(3472.111f, -3, 5));
        assertEquals(3472.111f, t1.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t1.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(5, t1.getAbsolutePosition().z(), DELTA_FLOAT);
        assertEquals(3472.111f, t2.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-6, t2.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(358.5f, t2.getAbsolutePosition().z(), DELTA_FLOAT);
    }

    @Test
    public void testAbsolutePositionDefensiveCopy() {
        Transform t = new Transform();
        Vector3f position = new Vector3f(0, -3, 353.5f);
        t.setAbsolutePosition(position);

        //change parameter
        position.set(3472.111f, -3, 5);
        assertEquals(0, t.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getAbsolutePosition().z(), DELTA_FLOAT);

        //change return value
        position = t.getAbsolutePosition();
        position.set(3472.111f, -3, 5);
        assertEquals(0, t.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getAbsolutePosition().z(), DELTA_FLOAT);
    }

    @Test(expected = NullPointerException.class)
    public void testAbsolutePositionNull() {
        Transform t = new Transform();

        t.setAbsolutePosition(null);
    }

    @Test
    public void testMove() {
        Transform t = new Transform();

        //move
        t.move(new Vector3f(0, -3, 353.5f));
        assertEquals(0, t.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(-3, t.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getAbsolutePosition().z(), DELTA_FLOAT);

        //move
        t.move(new Vector3f(0, 7.5f, 0));
        assertEquals(0, t.getAbsolutePosition().x(), DELTA_FLOAT);
        assertEquals(4.5f, t.getAbsolutePosition().y(), DELTA_FLOAT);
        assertEquals(353.5f, t.getAbsolutePosition().z(), DELTA_FLOAT);
    }

    @Test(expected = NullPointerException.class)
    public void testMoveNull() {
        Transform t = new Transform();

        t.move(null);
    }

}
