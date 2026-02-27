package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.packed.PkMove;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyMoveTest {

    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    // ===================== Constructeur compact =====================

    @Test
    void constructorThrowsIfSourceIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Move(null, TileKind.Colored.ALL.get(0), TileDestination.ALL.get(0))
        );
    }

    @Test
    void constructorThrowsIfColorIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Move(TileSource.ALL.get(0), null, TileDestination.ALL.get(0))
        );
    }

    @Test
    void constructorThrowsIfDestinationIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Move(TileSource.ALL.get(0), TileKind.Colored.ALL.get(0), null)
        );
    }

    @Test
    void constructorAcceptsAllValidCombinations() {
        for (var source : TileSource.ALL) {
            for (var color : TileKind.Colored.ALL) {
                for (var destination : TileDestination.ALL) {
                    assertDoesNotThrow(() -> new Move(source, color, destination));
                }
            }
        }
    }

    // ===================== Accesseurs =====================

    @Test
    void accessorsReturnCorrectValues() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var source = TileSource.ALL.get(rng.nextInt(TileSource.ALL.size()));
            var color = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var destination = TileDestination.ALL.get(rng.nextInt(TileDestination.ALL.size()));
            var move = new Move(source, color, destination);
            assertEquals(source, move.source());
            assertEquals(color, move.tileColor());
            assertEquals(destination, move.destination());
        }
    }

    // ===================== ofPacked / packed =====================

    @Test
    void ofPackedAndPackedAreInverseForAllCombinations() {
        for (var source : TileSource.ALL) {
            for (var color : TileKind.Colored.ALL) {
                for (var destination : TileDestination.ALL) {
                    var move = new Move(source, color, destination);
                    assertEquals(move, Move.ofPacked(move.packed()));
                }
            }
        }
    }

    @Test
    void packedAndOfPackedAreConsistentWithRandomMoves() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var source = TileSource.ALL.get(rng.nextInt(TileSource.ALL.size()));
            var color = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var destination = TileDestination.ALL.get(rng.nextInt(TileDestination.ALL.size()));
            var move = new Move(source, color, destination);
            var reconstructed = Move.ofPacked(move.packed());
            assertEquals(move.source(), reconstructed.source());
            assertEquals(move.tileColor(), reconstructed.tileColor());
            assertEquals(move.destination(), reconstructed.destination());
        }
    }

    @Test
    void ofPackedIsConsistentWithPkMovePack() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var source = TileSource.ALL.get(rng.nextInt(TileSource.ALL.size()));
            var color = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var destination = TileDestination.ALL.get(rng.nextInt(TileDestination.ALL.size()));
            var packed = PkMove.pack(source, color, destination);
            var move = Move.ofPacked(packed);
            assertEquals(source, move.source());
            assertEquals(color, move.tileColor());
            assertEquals(destination, move.destination());
        }
    }

    // ===================== MAX_MOVES =====================

    @Test
    void maxMovesIsPositive() {
        assertTrue(Move.MAX_MOVES > 0);
    }

    @Test
    void maxMovesMatchesFormula() {
        assertEquals(
                TileSource.Factory.COUNT * (TileKind.Colored.COUNT - 1) * TileDestination.COUNT,
                Move.MAX_MOVES
        );
    }
}
