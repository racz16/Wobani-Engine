/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import org.joml.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Zalan
 */
public class TransformTest {

    private Transform transform;
    private GameObject gameObject;

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
        transform = new Transform();
    }

    @After
    public void afterEachTest() {
    }

    @Test
    public void testGetRelativePosition() {
        Vector3f testRelativePosition = new Vector3f(0, -3, 353.5f);
        transform.setRelativePosition(testRelativePosition);
        assertEquals(0, transform.getRelativePosition().x(), 0);
        assertEquals(-3, transform.getRelativePosition().y(), 0.001f);
        assertEquals(353.5f, transform.getRelativePosition().z(), 0.001f);
    }

}
