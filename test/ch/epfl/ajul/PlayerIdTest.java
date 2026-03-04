package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerIdTest {
    @Test
    void playerIsValuesAreCorrectlyDefined() {
        var expected = new PlayerId[]{
                PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4
        };
        assertArrayEquals(expected, PlayerId.values());
    }

    @Test
    void playerIdAllIsCorrectlyDefined() {
        assertEquals(
                List.of(PlayerId.P1, PlayerId.P2, PlayerId.P3, PlayerId.P4),
                PlayerId.ALL);
    }
}