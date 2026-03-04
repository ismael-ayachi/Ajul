package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PkTileSetTest {
    private static int randomPkTileSet(RandomGenerator rng) {
        var pkTileSet = rng.nextInt(2); // M (0 or 1)
        for (var c = 0; c < 5; c += 1) // E, C, D, B, A
            pkTileSet = (pkTileSet << 6) | rng.nextInt(21);
        return pkTileSet;
    }

    @Test
    void pkTileSetConstantsAreCorrectlyDefined() {
        assertEquals(0, PkTileSet.EMPTY);
        assertEquals(0b00_010100_010100_010100_010100_010100, PkTileSet.FULL_COLORED);
        assertEquals(0b01_010100_010100_010100_010100_010100, PkTileSet.FULL);
    }

    @Test
    void pkTileSetOfWorks() {
        for (var kind : TileKind.ALL) {
            for (var n = 0; n < kind.tilesCount(); n += 1) {
                var expected = n << (6 * kind.index());
                assertEquals(expected, PkTileSet.of(n, kind));
            }
        }
    }

    @Test
    void pkTileSetIsEmptyWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkTileSet = randomPkTileSet(rng);
            var expected = pkTileSet == 0;
            assertEquals(expected, PkTileSet.isEmpty(pkTileSet));
        }
        assertTrue(PkTileSet.isEmpty(0));
    }

    @Test
    void pkTileSetSizeWorks() {
        assertEquals(0, PkTileSet.size(PkTileSet.EMPTY));
        assertEquals(100, PkTileSet.size(PkTileSet.FULL_COLORED));
        assertEquals(101, PkTileSet.size(PkTileSet.FULL));

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkTileSet = rng.nextInt(2); // M (0 or 1)
            var size = pkTileSet;
            for (var c = 0; c < 5; c += 1) { // E, C, D, B, A
                var count = rng.nextInt(21);
                pkTileSet = (pkTileSet << 6) | count;
                size += count;
            }
            assertEquals(size, PkTileSet.size(pkTileSet));
        }
    }

    @Test
    void pkTileSetCountOfWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var expectedCounts = new int[6];
            var pkTileSet = rng.nextInt(2); // M (0 or 1)
            expectedCounts[5] = pkTileSet;
            for (var c = 4; c >= 0; c -= 1) { // E, C, D, B, A
                var count = rng.nextInt(21);
                pkTileSet = (pkTileSet << 6) | count;
                expectedCounts[c] = count;
            }

            var actualCounts = new int[6];
            for (var kind : TileKind.ALL)
                actualCounts[kind.index()] = PkTileSet.countOf(pkTileSet, kind);
            assertArrayEquals(expectedCounts, actualCounts);
        }
    }

    @Test
    void pkTileSetSubsetOfWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkTileSet = randomPkTileSet(rng);
            for (var kind : TileKind.ALL) {
                var offset = kind.index() * 6;
                var expected = ((pkTileSet >> offset) & 0b111111) << offset;
                assertEquals(expected, PkTileSet.subsetOf(pkTileSet, kind));
            }
        }
    }

    @Test
    void pkTileSetAddWorks() {
        var pkTileSet = PkTileSet.EMPTY;

        assertEquals(0, PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER));
        pkTileSet = PkTileSet.add(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        assertEquals(1, PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER));

        for (var i = 1; i <= 20; i += 1) {
            for (var color : TileKind.Colored.ALL) {
                pkTileSet = PkTileSet.add(pkTileSet, color);
                assertEquals(i, PkTileSet.countOf(pkTileSet, color));
            }
        }
        assertEquals(PkTileSet.FULL, pkTileSet);
    }

    @Test
    void pkTileSetRemoveWorks() {
        var pkTileSet = PkTileSet.FULL;

        assertEquals(1, PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER));
        pkTileSet = PkTileSet.remove(pkTileSet, TileKind.FIRST_PLAYER_MARKER);
        assertEquals(0, PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER));

        for (var i = 19; i >= 0; i -= 1) {
            for (var color : TileKind.Colored.ALL) {
                pkTileSet = PkTileSet.remove(pkTileSet, color);
                assertEquals(i, PkTileSet.countOf(pkTileSet, color));
            }
        }
        assertEquals(PkTileSet.EMPTY, pkTileSet);

    }

    @Test
    void pkTileSetUnionWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var expectedCounts = new int[TileKind.COUNT];
        var actualCounts = new int[TileKind.COUNT];
        for (var i = 0; i < 1000; i += 1) {
            var pkTileSets = new int[TileKind.COUNT];
            Arrays.fill(pkTileSets, PkTileSet.EMPTY);
            for (var kind : TileKind.ALL) {
                var count = rng.nextInt(kind.tilesCount() + 1);
                var j = rng.nextInt(pkTileSets.length);
                pkTileSets[j] = PkTileSet.union(pkTileSets[j], PkTileSet.of(count, kind));
                expectedCounts[kind.index()] = count;
            }

            var pkTileSet = PkTileSet.EMPTY;
            for (var s : pkTileSets) pkTileSet = PkTileSet.union(pkTileSet, s);

            for (var kind : TileKind.ALL)
                actualCounts[kind.index()] = PkTileSet.countOf(pkTileSet, kind);

            assertArrayEquals(expectedCounts, actualCounts);
        }
    }

    @Test
    void pkTileSetDifferenceWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var expectedCounts = new int[TileKind.COUNT];
        var actualCounts = new int[TileKind.COUNT];
        for (var i = 0; i < 1000; i += 1) {
            var subSet = PkTileSet.EMPTY;
            var superSet = PkTileSet.EMPTY;
            for (var kind : TileKind.ALL) {
                var subCount = rng.nextInt(kind.tilesCount() + 1);
                var superCount = rng.nextInt(subCount, kind.tilesCount() + 1);
                subSet |= subCount << (6 * kind.index());
                superSet |= superCount << (6 * kind.index());
                expectedCounts[kind.index()] = superCount - subCount;
            }
            var difference = PkTileSet.difference(superSet, subSet);
            for (var kind : TileKind.ALL)
                actualCounts[kind.index()] = PkTileSet.countOf(difference, kind);

            assertArrayEquals(expectedCounts, actualCounts);
        }
    }

    @Test
    void pkTileSetCopyColoredIntoWorksOnConstantSets() {
        var empty = new TileKind.Colored[0];
        var countEmpty = PkTileSet.copyColoredInto(PkTileSet.EMPTY, empty);
        assertEquals(empty.length, countEmpty);

        var full = new TileKind.Colored[100];
        var expectedFull = full.clone();
        for (var color : TileKind.Colored.ALL) {
            var start = color.index() * 20;
            var end = start + 20;
            Arrays.fill(expectedFull, start, end, color);
        }
        var countFull = PkTileSet.copyColoredInto(PkTileSet.FULL_COLORED, full);
        assertEquals(full.length, countFull);
        assertArrayEquals(expectedFull, full);
    }

    @Test
    void pkTileSetSampleColoredIntoWorksOnEmptySet() {
        var empty = new TileKind.Colored[0];
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var count = PkTileSet.sampleColoredInto(PkTileSet.EMPTY, empty, 0, rng);
        assertEquals(0, count);
    }

    @Test
    void pkTileSetSampleColoredIntoWorksOnTrivialSetWithZeroOffset() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var pkTileSet = PkTileSet.of(20, TileKind.A);
        for (var size = 1; size <= 20; size += 1) {
            var actual = new TileKind.Colored[size];
            var expected = actual.clone();
            PkTileSet.sampleColoredInto(pkTileSet, actual, 0, rng);
            Arrays.fill(expected, TileKind.Colored.A);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void pkTileSetSampleColoredIntoWorksOnTrivialSetWithNonZeroOffset() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var pkTileSet = PkTileSet.of(20, TileKind.A);
        for (var size = 2; size <= 20; size += 1) {
            var actual = new TileKind.Colored[size];
            for (var i = 0; i < actual.length; i += 1)
                actual[i] = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.COUNT));

            var offset = rng.nextInt(1, actual.length);
            var expected = actual.clone();
            Arrays.fill(expected, offset, expected.length, TileKind.Colored.A);

            var res = PkTileSet.sampleColoredInto(pkTileSet, actual, offset, rng);
            assertArrayEquals(expected, actual);
            assertEquals(offset + 20, res);
        }
    }

    @Test
    void pkTileSetSampleColoredIntoProducesAllPossibleSamples() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var variants = new HashSet<String>();
        var destination = new TileKind.Colored[4];
        // The number of iterations (3000) was determined experimentally
        for (var i = 0; i < 3_000; i += 1) {
            PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, destination, 0, rng);
            Arrays.sort(destination);
            variants.add(Arrays.toString(destination));
        }
        // There are 70 (8 choose 4) possible combinations of tiles of 5 colors put into 4 bins,
        // see https://en.wikipedia.org/wiki/Stars_and_bars_(combinatorics)
        assertEquals(70, variants.size());
    }

    @Test
    void pkTileSetSampleColoredIntoProducesCorrectResult() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var destination = new TileKind.Colored[20];
        PkTileSet.sampleColoredInto(PkTileSet.FULL_COLORED, destination, 0, rng);
        var expected = new TileKind.Colored[]{
                TileKind.Colored.D, TileKind.Colored.A, TileKind.Colored.E,
                TileKind.Colored.E, TileKind.Colored.A, TileKind.Colored.D,
                TileKind.Colored.E, TileKind.Colored.A, TileKind.Colored.C,
                TileKind.Colored.E, TileKind.Colored.B, TileKind.Colored.E,
                TileKind.Colored.D, TileKind.Colored.E, TileKind.Colored.D,
                TileKind.Colored.B, TileKind.Colored.C, TileKind.Colored.C,
                TileKind.Colored.B, TileKind.Colored.D
        };
        assertArrayEquals(expected, destination);
    }

    @Test
    void pkTileSetToStringWorksOnConstantSets() {
        assertEquals("{}", PkTileSet.toString(PkTileSet.EMPTY));
        assertEquals(
                "{20*A,20*B,20*C,20*D,20*E}",
                PkTileSet.toString(PkTileSet.FULL_COLORED));
        assertEquals(
                "{20*A,20*B,20*C,20*D,20*E,1*FIRST_PLAYER_MARKER}",
                PkTileSet.toString(PkTileSet.FULL));
    }
}