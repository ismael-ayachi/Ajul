package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

public class MyPkTileSetTest {

    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    // ===================== EMPTY / FULL / FULL_COLORED =====================

    @Test
    void emptyIsZero() {
        assertEquals(0, PkTileSet.EMPTY);
    }

    @Test
    void fullContains20OfEachColorAnd1Marker() {
        for (var tileKind : TileKind.Colored.ALL)
            assertEquals(20, PkTileSet.countOf(PkTileSet.FULL, tileKind));
        assertEquals(1, PkTileSet.countOf(PkTileSet.FULL, TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER));
    }

    @Test
    void fullColoredContains20OfEachColorAndNoMarker() {
        for (var tileKind : TileKind.Colored.ALL)
            assertEquals(20, PkTileSet.countOf(PkTileSet.FULL_COLORED, tileKind));
        assertEquals(0, PkTileSet.countOf(PkTileSet.FULL_COLORED, TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER));
    }

    // ===================== of =====================

    @Test
    void ofReturnsCorrectCountForAllColors() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(0, 21);
            var set = PkTileSet.of(count, tileKind);
            assertEquals(count, PkTileSet.countOf(set, tileKind));
        }
    }

    @Test
    void ofDoesNotPollutOtherCounters() {
        for (var tileKind : TileKind.Colored.ALL) {
            var set = PkTileSet.of(10, tileKind);
            for (var other : TileKind.Colored.ALL) {
                if (other != tileKind)
                    assertEquals(0, PkTileSet.countOf(set, other));
            }
            assertEquals(0, PkTileSet.countOf(set, TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER));
        }
    }

    @Test
    void ofWithZeroReturnsEmpty() {
        for (var tileKind : TileKind.Colored.ALL)
            assertEquals(PkTileSet.EMPTY, PkTileSet.of(0, tileKind));
    }

    // ===================== isEmpty =====================

    @Test
    void isEmptyOnEmptySet() {
        assertTrue(PkTileSet.isEmpty(PkTileSet.EMPTY));
    }

    @Test
    void isEmptyOnNonEmptySet() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(1, 21);
            assertFalse(PkTileSet.isEmpty(PkTileSet.of(count, tileKind)));
        }
    }

    // ===================== size =====================

    @Test
    void sizeOfEmptyIsZero() {
        assertEquals(0, PkTileSet.size(PkTileSet.EMPTY));
    }

    @Test
    void sizeOfFullIs101() {
        assertEquals(101, PkTileSet.size(PkTileSet.FULL));
    }

    @Test
    void sizeOfFullColoredIs100() {
        assertEquals(100, PkTileSet.size(PkTileSet.FULL_COLORED));
    }

    @Test
    void sizeIsConsistentWithCountOf() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var set = PkTileSet.EMPTY;
            var expectedSize = 0;
            for (var tileKind : TileKind.Colored.ALL) {
                var count = rng.nextInt(0, 21);
                set = PkTileSet.union(set, PkTileSet.of(count, tileKind));
                expectedSize += count;
            }
            assertEquals(expectedSize, PkTileSet.size(set));
        }
    }

    // ===================== countOf =====================

    @Test
    void countOfReturnsZeroOnEmpty() {
        for (var tileKind : TileKind.Colored.ALL)
            assertEquals(0, PkTileSet.countOf(PkTileSet.EMPTY, tileKind));
        assertEquals(0, PkTileSet.countOf(PkTileSet.EMPTY, TileKind.FirstPlayerMarker.FIRST_PLAYER_MARKER));
    }

    @Test
    void countOfIsConsistentWithOf() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(0, 21);
            assertEquals(count, PkTileSet.countOf(PkTileSet.of(count, tileKind), tileKind));
        }
    }

    // ===================== subsetOf =====================

    @Test
    void subsetOfContainsOnlyRequestedColor() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var set = PkTileSet.EMPTY;
            for (var tileKind : TileKind.Colored.ALL)
                set = PkTileSet.union(set, PkTileSet.of(rng.nextInt(0, 21), tileKind));

            var target = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var subset = PkTileSet.subsetOf(set, target);

            assertEquals(PkTileSet.countOf(set, target), PkTileSet.countOf(subset, target));
            for (var other : TileKind.Colored.ALL) {
                if (other != target)
                    assertEquals(0, PkTileSet.countOf(subset, other));
            }
        }
    }

    // ===================== add / remove =====================

    @Test
    void addIncrementsCorrectColor() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(0, 20);
            var set = PkTileSet.of(count, tileKind);
            assertEquals(count + 1, PkTileSet.countOf(PkTileSet.add(set, tileKind), tileKind));
        }
    }

    @Test
    void removeDecrementsCorrectColor() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(1, 21);
            var set = PkTileSet.of(count, tileKind);
            assertEquals(count - 1, PkTileSet.countOf(PkTileSet.remove(set, tileKind), tileKind));
        }
    }

    @Test
    void addThenRemoveGivesOriginal() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(0, 20);
            var set = PkTileSet.of(count, tileKind);
            assertEquals(set, PkTileSet.remove(PkTileSet.add(set, tileKind), tileKind));
        }
    }

    // ===================== union / difference =====================

    @Test
    void unionSumsCounters() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count1 = rng.nextInt(0, 11);
            var count2 = rng.nextInt(0, 11);
            var set1 = PkTileSet.of(count1, tileKind);
            var set2 = PkTileSet.of(count2, tileKind);
            assertEquals(count1 + count2, PkTileSet.countOf(PkTileSet.union(set1, set2), tileKind));
        }
    }

    @Test
    void differenceSubtractsCounters() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count1 = rng.nextInt(0, 21);
            var count2 = rng.nextInt(0, count1 + 1);
            var set1 = PkTileSet.of(count1, tileKind);
            var set2 = PkTileSet.of(count2, tileKind);
            assertEquals(count1 - count2, PkTileSet.countOf(PkTileSet.difference(set1, set2), tileKind));
        }
    }

    @Test
    void unionWithEmptyGivesOriginal() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var set = PkTileSet.of(rng.nextInt(0, 21), tileKind);
            assertEquals(set, PkTileSet.union(set, PkTileSet.EMPTY));
        }
    }

    @Test
    void differenceWithEmptyGivesOriginal() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var set = PkTileSet.of(rng.nextInt(0, 21), tileKind);
            assertEquals(set, PkTileSet.difference(set, PkTileSet.EMPTY));
        }
    }

    @Test
    void differenceWithItselfGivesEmpty() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var set = PkTileSet.EMPTY;
            for (var tileKind : TileKind.Colored.ALL)
                set = PkTileSet.union(set, PkTileSet.of(rng.nextInt(0, 21), tileKind));
            assertEquals(PkTileSet.EMPTY, PkTileSet.difference(set, set));
        }
    }

    // ===================== copyColoredInto =====================

    @Test
    void copyColoredIntoFillsInOrder() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var set = PkTileSet.EMPTY;
            var counts = new int[TileKind.Colored.ALL.size()];
            for (var k = 0; k < TileKind.Colored.ALL.size(); k++) {
                counts[k] = rng.nextInt(0, 6);
                set = PkTileSet.union(set, PkTileSet.of(counts[k], TileKind.Colored.ALL.get(k)));
            }
            var destination = new TileKind.Colored[100];
            var end = PkTileSet.copyColoredInto(set, destination);

            assertEquals(PkTileSet.size(set), end);

            var idx = 0;
            for (var k = 0; k < TileKind.Colored.ALL.size(); k++) {
                for (var j = 0; j < counts[k]; j++) {
                    assertEquals(TileKind.Colored.ALL.get(k), destination[idx]);
                    idx++;
                }
            }
        }
    }

    // ===================== sampleColoredInto =====================

    @Test
    void sampleColoredIntoReturnsOffsetPlusSize() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var set = PkTileSet.EMPTY;
            for (var tileKind : TileKind.Colored.ALL)
                set = PkTileSet.union(set, PkTileSet.of(rng.nextInt(0, 6), tileKind));
            var offset = rng.nextInt(0, 10);
            var destination = new TileKind.Colored[100];
            var sampleRng = RandomGeneratorFactory.getDefault().create(rng.nextLong());
            var result = PkTileSet.sampleColoredInto(set, destination, offset, sampleRng);
            assertEquals(offset + PkTileSet.size(set), result);
        }
    }

    @Test
    void sampleColoredIntoPlacesTileFromSet() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var set = PkTileSet.EMPTY;
            for (var tileKind : TileKind.Colored.ALL)
                set = PkTileSet.union(set, PkTileSet.of(rng.nextInt(1, 6), tileKind));
            var offset = rng.nextInt(0, 10);
            var destination = new TileKind.Colored[100];
            var sampleRng = RandomGeneratorFactory.getDefault().create(rng.nextLong());
            PkTileSet.sampleColoredInto(set, destination, offset, sampleRng);
            assertNotNull(destination[offset]);
            assertTrue(TileKind.Colored.ALL.contains(destination[offset]));
        }
    }

    @Test
    void sampleColoredIntoOnSingleTileAlwaysSelectsIt() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var tileKind : TileKind.Colored.ALL) {
            var set = PkTileSet.of(1, tileKind);
            var destination = new TileKind.Colored[10];
            var sampleRng = RandomGeneratorFactory.getDefault().create(rng.nextLong());
            PkTileSet.sampleColoredInto(set, destination, 0, sampleRng);
            assertEquals(tileKind, destination[0]);
        }
    }

    @Test
    void sampleColoredIntoIsUniform() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var counts = new int[TileKind.Colored.ALL.size()];
        var totalTiles = TileKind.Colored.ALL.size();
        var set = PkTileSet.EMPTY;
        for (var tileKind : TileKind.Colored.ALL)
            set = PkTileSet.union(set, PkTileSet.of(1, tileKind));

        var selectedCounts = new int[TileKind.Colored.ALL.size()];
        var trials = 10000;
        for (var i = 0; i < trials; i++) {
            var destination = new TileKind.Colored[10];
            var sampleRng = RandomGeneratorFactory.getDefault().create(rng.nextLong());
            PkTileSet.sampleColoredInto(set, destination, 0, sampleRng);
            selectedCounts[TileKind.Colored.ALL.indexOf(destination[0])]++;
        }

        // Chaque couleur devrait être sélectionnée environ trials/totalTiles fois
        // On accepte une marge de 20%
        var expected = (double) trials / totalTiles;
        for (var count : selectedCounts)
            assertTrue(Math.abs(count - expected) < expected * 0.2);
    }

    // ===================== toString =====================

    @Test
    void toStringOnEmpty() {
        assertEquals("{}", PkTileSet.toString(PkTileSet.EMPTY));
    }

    @Test
    void toStringOnFull() {
        assertEquals("{20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}", PkTileSet.toString(PkTileSet.FULL));
    }

    @Test
    void toStringSkipsZeroCounters() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 100; i++) {
            var tileKind = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.ALL.size()));
            var count = rng.nextInt(1, 21);
            var set = PkTileSet.of(count, tileKind);
            var result = PkTileSet.toString(set);
            assertTrue(result.contains(count + "*" + tileKind.name()));
            for (var other : TileKind.Colored.ALL) {
                if (other != tileKind)
                    assertFalse(result.contains("*" + other.name()));
            }
        }
    }
}