package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.TileKind.Colored;
import static ch.epfl.ajul.TileKind.FirstPlayerMarker;
import static org.junit.jupiter.api.Assertions.*;

class TileKindTest {
    @Test
    void tileKindFieldsAreEnumConstants() {
        assertSame(Colored.A, TileKind.A);
        assertSame(Colored.B, TileKind.B);
        assertSame(Colored.C, TileKind.C);
        assertSame(Colored.D, TileKind.D);
        assertSame(Colored.E, TileKind.E);
        assertSame(FirstPlayerMarker.FIRST_PLAYER_MARKER, TileKind.FIRST_PLAYER_MARKER);
    }

    @Test
    void tileKindAllIsCorrectlyDefined() {
        var expected = List.of(
                TileKind.A,
                TileKind.B,
                TileKind.C,
                TileKind.D,
                TileKind.E,
                TileKind.FIRST_PLAYER_MARKER);
        var actual = TileKind.ALL;
        assertEquals(expected, actual);
    }

    @Test
    void tileKindAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection Convert2MethodRef,DataFlowIssue
            TileKind.ALL.clear();
        });
    }

    @Test
    void tileKindCountIsCorrectlyDefined() {
        assertEquals(6, TileKind.COUNT);
    }

    @Test
    void tileKindIndexAndAllGetAreInverse() {
        for (var i = 0; i < TileKind.COUNT; i += 1)
            assertEquals(i, TileKind.ALL.get(i).index());
    }

    @Test
    void tileKindTilesCountIsCorrect() {
        for (var tileKind : TileKind.ALL) {
            var expectedCount = tileKind == TileKind.FIRST_PLAYER_MARKER ? 1 : 20;
            assertEquals(expectedCount, tileKind.tilesCount());
        }
    }

    @Test
    void coloredAllIsCorrectlyDefined() {
        assertEquals(List.of(Colored.A, Colored.B, Colored.C, Colored.D, Colored.E), Colored.ALL);
    }

    @Test
    void coloredAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            //noinspection Convert2MethodRef,DataFlowIssue
            Colored.ALL.clear();
        });
    }

    @Test
    void coloredCountIsCorrectlyDefined() {
        assertEquals(5, Colored.COUNT);
    }

    @Test
    void coloredShuffleWorksOnEmptyArray() {
        Colored.shuffle(new Colored[0], RandomGenerator.getDefault());
    }

    @Test
    void coloredShuffleCorrectlyShufflesKnowArray() {
        var allColors = Colored.values();
        var randomGenerator = RandomGeneratorFactory.getDefault()
                .create(2026);
        Colored.shuffle(allColors, randomGenerator);
        var expected = new Colored[]{Colored.B, Colored.E, Colored.A, Colored.C, Colored.D};
        assertArrayEquals(expected, allColors);
    }

    @Test
    void coloredShuffleProducesAllPermutations() {
        var originalArray = Colored.values();
        var randomGenerator = RandomGeneratorFactory.getDefault()
                .create(2026);
        var encounteredPermutations = new HashSet<String>();
        // The number of iterations (600) was determined experimentally
        for (var i = 0; i < 600; i += 1) {
            var array = Colored.values();
            Colored.shuffle(array, randomGenerator);
            encounteredPermutations.add(Arrays.toString(array));

            // Check that the shuffled array is a permutation of the original one.
            Arrays.sort(array);
            assertArrayEquals(originalArray, array);
        }
        // There are 5! = 120 permutations of an array of size 5.
        assertEquals(120, encounteredPermutations.size());
    }
}