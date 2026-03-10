package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreconditionsTest {
    @Test
    void preconditionsCheckArgumentThrowsOnFalseOnly() {
        assertDoesNotThrow(() -> {
            Preconditions.checkArgument(true);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Preconditions.checkArgument(false);
        });
    }
}