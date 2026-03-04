package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PkMoveTest {
    @Test
    void pkMovePackWorks() {
        var remainingCases = 10 * 5 * 6;
        for (var destination : TileDestination.ALL) {
            var pkDestination = destination.index();
            for (var color : TileKind.Colored.values()) {
                var pkDestinationColor = (pkDestination << 3) | color.index();
                for (var source : TileSource.ALL) {
                    var pkMove = (pkDestinationColor << 4) | source.index();
                    assertEquals(pkMove, PkMove.pack(source, color, destination));
                    remainingCases -= 1;
                }
            }
        }
        assertEquals(0, remainingCases);
    }

    @Test
    void pkMoveSourceWorks() {
        var remainingCases = 10;
        for (var source : TileSource.ALL) {
            var pkMove = PkMove.pack(source, TileKind.Colored.A, TileDestination.FLOOR);
            assertSame(source, PkMove.source(pkMove));
            remainingCases -= 1;
        }
        assertEquals(0, remainingCases);
    }

    @Test
    void pkMoveColorWorks() {
        var remainingCases = 5;
        for (var color : TileKind.Colored.ALL) {
            var pkMove = PkMove.pack(TileSource.FACTORY_1, color, TileDestination.FLOOR);
            assertSame(color, PkMove.color(pkMove));
            remainingCases -= 1;
        }
        assertEquals(0, remainingCases);
    }

    @Test
    void pkMoveDestinationWorks() {
        var remainingCases = 6;
        for (var destination : TileDestination.ALL) {
            var pkMove = PkMove.pack(TileSource.FACTORY_1, TileKind.Colored.A, destination);
            assertSame(destination, PkMove.destination(pkMove));
            remainingCases -= 1;
        }
        assertEquals(0, remainingCases);
    }
}