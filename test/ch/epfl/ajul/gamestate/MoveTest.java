package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveTest {
    @Test
    void moveMaxMovesIsCorrectlyDefined() {
        assertEquals(216, Move.MAX_MOVES);
    }

    @Test
    void moveConstructorsThrowsOnNullArgument() {
        assertThrows(NullPointerException.class, () -> {
            new Move(null, TileKind.Colored.A, TileDestination.FLOOR);
        });
        assertThrows(NullPointerException.class, () -> {
            new Move(TileSource.FACTORY_1, null, TileDestination.FLOOR);
        });
        assertThrows(NullPointerException.class, () -> {
            new Move(TileSource.FACTORY_1, TileKind.Colored.A, null);
        });
    }

    @Test
    void moveOfPackedWorks() {
        for (var destination : TileDestination.ALL) {
            var pkDestination = destination.index();
            for (var color : TileKind.Colored.values()) {
                var pkDestinationColor = (pkDestination << 3) | color.index();
                for (var source : TileSource.ALL) {
                    var pkMove = (pkDestinationColor << 4) | source.index();
                    assertEquals(
                            new Move(source, color, destination),
                            Move.ofPacked((short) pkMove));
                }
            }
        }
    }

    @Test
    void movePackedWorks() {
        for (var destination : TileDestination.ALL) {
            var pkDestination = destination.index();
            for (var color : TileKind.Colored.values()) {
                var pkDestinationColor = (pkDestination << 3) | color.index();
                for (var source : TileSource.ALL) {
                    var pkMove = (pkDestinationColor << 4) | source.index();
                    assertEquals(pkMove, new Move(source, color, destination).packed());
                }
            }
        }
    }
}