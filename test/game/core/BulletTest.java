package game.core;

import game.core.Bullet;
import org.junit.Test;
import static org.junit.Assert.*;

public class BulletTest {

    @Test
    public void testTick_MovesUpward() {
        Bullet bullet = new Bullet(5, 10); // Starting at (5, 10)
        bullet.tick(0); // Tick is ignored
        assertEquals(5, bullet.getX());
        assertEquals(9, bullet.getY()); // Should move up (y--)
    }

    @Test
    public void testTick_MultipleTicks() {
        Bullet bullet = new Bullet(3, 5);
        bullet.tick(1);
        bullet.tick(2);
        bullet.tick(3);
        assertEquals(3, bullet.getX());
        assertEquals(2, bullet.getY()); // y should go from 5 → 4 → 3 → 2
    }

    @Test
    public void testTick_WithNegativeY() {
        Bullet bullet = new Bullet(0, 0);
        bullet.tick(5);
        assertEquals(0, bullet.getX());
        assertEquals(-1, bullet.getY()); // Bullet can move into negative y
    }

    @Test
    public void testTick_IgnoresTickValue() {
        Bullet bullet = new Bullet(2, 7);
        bullet.tick(999); // Should still just decrement y by 1
        assertEquals(6, bullet.getY());
    }
}