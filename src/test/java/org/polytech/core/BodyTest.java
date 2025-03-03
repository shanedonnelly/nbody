package org.polytech.core;

import org.acme.core.Body;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BodyTest {

    @Test
    void testBodyConstructor() {
        Body body = new Body(1.0, 2.0, 3.0, 4.0, 5.0);
        assertEquals(1.0, body.getX());
        assertEquals(2.0, body.getY());
        assertEquals(3.0, body.getVx());
        assertEquals(4.0, body.getVy());
        assertEquals(5.0, body.getMass());
    }

    private static void createBodyWithNegativeMass() {
        new Body(0, 0, 0, 0, -1.0);
    }

    @Test
    void testSetX() {
        Body body = new Body(0, 0, 0, 0, 0);
        body.setX(1.0);
        assertEquals(1.0, body.getX());
    }

    @Test
    void testSetY() {
        Body body = new Body(0, 0, 0, 0, 0);
        body.setY(2.0);
        assertEquals(2.0, body.getY());
    }

    @Test
    void testSetVx() {
        Body body = new Body(0, 0, 0, 0, 0);
        body.setVx(3.0);
        assertEquals(3.0, body.getVx());
    }

    @Test
    void testSetVy() {
        Body body = new Body(0, 0, 0, 0, 0);
        body.setVy(4.0);
        assertEquals(4.0, body.getVy());
    }

    @Test
    void testSetMass() {
        Body body = new Body(0, 0, 0, 0, 0);
        body.setMass(5.0);
        assertEquals(5.0, body.getMass());
    }

    // Tests supplémentaires

    @Test
    void testNegativeMass() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, BodyTest::createBodyWithNegativeMass);
        assertEquals("Mass cannot be negative", exception.getMessage());
    }

    @Test
    void testUpdatePosition() {
        Body body = new Body(0, 0, 1.0, 1.0, 1.0);
        body.setX(body.getX() + body.getVx());
        body.setY(body.getY() + body.getVy());
        assertEquals(1.0, body.getX());
        assertEquals(1.0, body.getY());
    }

    @Test
    void testZeroVelocity() {
        Body body = new Body(0, 0, 0, 0, 1.0);
        body.setX(body.getX() + body.getVx());
        body.setY(body.getY() + body.getVy());
        assertEquals(0, body.getX());
        assertEquals(0, body.getY());
    }
}