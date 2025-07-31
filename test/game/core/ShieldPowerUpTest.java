package game.core;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit test for {@link ShieldPowerUp}.
 */
public class ShieldPowerUpTest {

    @Test
    public void testApplyEffect_NoConsoleOutput() {
        // Save the original System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // Setup
            Ship ship = new Ship();
            ShieldPowerUp shield = new ShieldPowerUp(5, 5);

            // Act
            shield.applyEffect(ship);

            // Assert
            assertEquals("Console output should be empty", "", outContent.toString().trim());
        } finally {
            // Restore original System.out
            System.setOut(originalOut);
        }
    }

    @Test
    public void testApplyEffect() {
        Ship ship = new Ship();
        ShieldPowerUp shield = new ShieldPowerUp(5, 5);

        shield.applyEffect(ship);
        assertEquals(50, ship.getScore());
    }

    @Test
    public void testApplyEffect_IncreasesScore() {
        Ship ship = new Ship(); // assume initial score is 0
        ShieldPowerUp shield = new ShieldPowerUp(1, 1);

        int initialScore = ship.getScore();
        shield.applyEffect(ship);

        assertEquals(initialScore + 50, ship.getScore());
    }

    @Test
    public void testApplyEffect_Twice() {
        Ship ship = new Ship();
        ShieldPowerUp shield = new ShieldPowerUp(2, 2);

        shield.applyEffect(ship);
        shield.applyEffect(ship);

        assertEquals(100, ship.getScore()); // +50 twice
    }
}