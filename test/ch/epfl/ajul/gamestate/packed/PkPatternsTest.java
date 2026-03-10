package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PkPatternsTest {
    @Test
    void pkPatternsEmptyIsCorrectlyDefined() {
        assertEquals(0, PkPatterns.EMPTY);
    }

    @Test
    void pkPatternsSizeWorks() {
        var remaining = 2 + 3 + 4 + 5 + 6;
        for (var line : TileDestination.Pattern.ALL) {
            for (int size = 0; size <= line.capacity(); size += 1) {
                var pkPatterns = size << (6 * line.index());
                assertEquals(size, PkPatterns.size(pkPatterns, line));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPatternsColorWorks() {
        var remaining = (1 + 2 + 3 + 4 + 5) * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (int size = 1; size <= line.capacity(); size += 1) {
                for (var color : TileKind.Colored.ALL) {
                    var pkPair = (color.index() << 3) | size;
                    var pkPatterns = pkPair << (6 * line.index());
                    assertEquals(color, PkPatterns.color(pkPatterns, line));
                    remaining -= 1;
                }
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPatternsIsFullWorks() {
        var remaining = 2 + 3 + 4 + 5 + 6;
        for (var line : TileDestination.Pattern.ALL) {
            for (int size = 0; size <= line.capacity(); size += 1) {
                var pkPatterns = size << (6 * line.index());
                var expected = size == line.capacity();
                assertEquals(expected, PkPatterns.isFull(pkPatterns, line));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPatternsCanContainWorksForEmptyLines() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                assertTrue(PkPatterns.canContain(PkPatterns.EMPTY, line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPatternsCanContainWorksForNonEmptyLines() {
        var remaining = (1 + 2 + 3 + 4 + 5) * 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (int size = 1; size <= line.capacity(); size += 1) {
                for (var color : TileKind.Colored.ALL) {
                    var pkPair = (color.index() << 3) | size;
                    var pkPatterns = pkPair << (6 * line.index());
                    for (var testedColor : TileKind.Colored.ALL) {
                        var expected = testedColor == color;
                        assertEquals(expected, PkPatterns.canContain(pkPatterns, line, testedColor));
                        remaining -= 1;
                    }
                }
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPatternsWithAddedTilesWorksForEmptyLines() {
        var remaining = (1 + 2 + 3 + 4 + 5) * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (int size = 1; size <= line.capacity(); size += 1) {
                for (var color : TileKind.Colored.ALL) {
                    var pkPair = (color.index() << 3) | size;
                    var pkPatterns = pkPair << (6 * line.index());
                    assertEquals(pkPatterns, PkPatterns.withAddedTiles(PkPatterns.EMPTY, line, size, color));
                    remaining -= 1;
                }
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPatternsWithAddedTilesWorksForNonEmptyLines() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 200; i += 1) {
            var pkPatterns = PkPatterns.EMPTY;
            var colors = new TileKind.Colored[TileDestination.Pattern.COUNT];
            var sizes = new int[TileDestination.Pattern.COUNT];
            for (var j = 0; j < 5; j += 1) {
                var lineIndex = rng.nextInt(TileDestination.Pattern.COUNT);
                var line = TileDestination.Pattern.ALL.get(lineIndex);
                var lineSize = sizes[lineIndex];
                var freeCapacity = line.capacity() - lineSize;
                if (freeCapacity == 0) continue;

                var count = rng.nextInt(1,freeCapacity + 1);
                if (colors[lineIndex] == null)
                    colors[lineIndex] = TileKind.Colored.ALL.get(rng.nextInt(TileKind.Colored.COUNT));
                var color = colors[lineIndex];
                pkPatterns = PkPatterns.withAddedTiles(pkPatterns, line, count, color);
                sizes[lineIndex] += count;
            }

            for (var line : TileDestination.Pattern.ALL) {
                assertEquals(sizes[line.index()], PkPatterns.size(pkPatterns, line));
                if (colors[line.index()] != null)
                    assertEquals(colors[line.index()], PkPatterns.color(pkPatterns, line));
            }
        }
    }

    @Test
    void pkPatternsWithEmptyLineWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkPatterns = PkPatterns.EMPTY;
            for (var line : TileDestination.Pattern.ALL) {
                var size = rng.nextInt(line.capacity() + 1);
                var colorIndex = rng.nextInt(TileKind.Colored.COUNT);
                var color = TileKind.Colored.ALL.get(colorIndex);
                pkPatterns = PkPatterns.withAddedTiles(pkPatterns, line, size, color);
            }

            for (var line : TileDestination.Pattern.ALL) {
                var expected = pkPatterns & ~(0b111_111 << (6 * line.index()));
                assertEquals(expected, PkPatterns.withEmptyLine(pkPatterns, line));
            }
        }
    }

    @Test
    void pkPatternsAsPkTileSetWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkTileSet = PkTileSet.EMPTY;
            var pkPatterns = PkPatterns.EMPTY;
            for (var line : TileDestination.Pattern.ALL) {
                var size = rng.nextInt(line.capacity() + 1);
                var colorIndex = rng.nextInt(TileKind.Colored.COUNT);
                var color = TileKind.Colored.ALL.get(colorIndex);
                pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(size, color));
                pkPatterns = PkPatterns.withAddedTiles(pkPatterns, line, size, color);
            }
            assertEquals(pkTileSet, PkPatterns.asPkTileSet(pkPatterns));
        }
    }

    @Test
    void pkPatternsToStringWorksForKnownValues() {
        assertEquals("[., .., ..., ...., .....]", PkPatterns.toString(PkPatterns.EMPTY));

        var pkPatterns = 0b00__000_000__100_011__000_011__000_010__010_001;
        assertEquals("[C, AA, AAA, EEE., .....]", PkPatterns.toString(pkPatterns));
    }
}