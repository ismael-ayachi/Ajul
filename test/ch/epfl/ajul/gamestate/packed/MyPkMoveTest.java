package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyPkMoveTest {
    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);


    @Test
    void packAndColorAreConsistent() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var source = TileSource.ALL.get(rng.nextInt(TileSource.ALL.size()));
            var color = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var destination = TileDestination.ALL.get(rng.nextInt(TileDestination.ALL.size()));
            var packed = PkMove.pack(source, color, destination);
            assertEquals(color, PkMove.color(packed));
        }
    }

    @Test
    void packAndDestinationAreConsistent() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var source = TileSource.ALL.get(rng.nextInt(TileSource.ALL.size()));
            var color = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var destination = TileDestination.ALL.get(rng.nextInt(TileDestination.ALL.size()));
            var packed = PkMove.pack(source, color, destination);
            assertEquals(destination, PkMove.destination(packed));
        }
    }

    @Test
    void packIsConsistentForAllCombinations() {
        for (var source : TileSource.ALL) {
            for (var color : TileKind.Colored.ALL) {
                for (var destination : TileDestination.ALL) {
                    var packed = PkMove.pack(source, color, destination);
                    assertEquals(source, PkMove.source(packed));
                    assertEquals(color, PkMove.color(packed));
                    assertEquals(destination, PkMove.destination(packed));
                }
            }
        }
    }

    @Test
    void packedValueHasZeroHighBits() {
        for (var source : TileSource.ALL) {
            for (var color : TileKind.Colored.ALL) {
                for (var destination : TileDestination.ALL) {
                    var packed = PkMove.pack(source, color, destination);
                    assertEquals(0, (packed >> 10) & 0b111111);
                }
            }
        }
    }
}