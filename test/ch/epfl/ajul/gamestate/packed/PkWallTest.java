package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileDestination.Pattern;
import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PkWallTest {
    @Test
    void pkWallConstantsAreCorrectlyDefined() {
        assertEquals(0, PkWall.EMPTY);
        assertEquals(5, PkWall.WALL_WIDTH);
        assertEquals(5, PkWall.WALL_HEIGHT);
    }

    @Test
    void pkWallIndexOfWorks() {
        var colors = new ArrayList<>(TileKind.Colored.ALL);
        var expectedIndex = 0;
        for (var line : Pattern.ALL) {
            for (var color : colors) {
                assertEquals(expectedIndex, PkWall.indexOf(line, color));
                expectedIndex += 1;
            }
            Collections.rotate(colors, 1);
        }
    }

    @Test
    void pkWallColumnWorks() {
        var colors = new ArrayList<>(TileKind.Colored.ALL);
        for (var line : Pattern.ALL) {
            var expectedColumn = 0;
            for (var color : colors) {
                assertEquals(expectedColumn, PkWall.column(line, color));
                expectedColumn += 1;
            }
            Collections.rotate(colors, 1);
        }
    }

    @Test
    void pkWallColorAtWorks() {
        var colors = new ArrayList<>(TileKind.Colored.ALL);
        for (var line : Pattern.ALL) {
            for (int column = 0; column < 5; column += 1) {
                assertEquals(colors.get(column), PkWall.colorAt(line, column));
            }
            Collections.rotate(colors, 1);
        }
    }

    @Test
    void pkWallWithTileAtWorks() {
        record Cell(Pattern line, TileKind.Colored color) {
            public int index() {
                return 5 * line.index() + (color.index() + line.index()) % 5;
            }
        }
        var cells = new ArrayList<Cell>();
        for (var line : Pattern.ALL) for (var color : TileKind.Colored.ALL) cells.add(new Cell(line, color));
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 100; i += 1) {
            var expectedPkWall = 0;
            var actualPkWall = PkWall.EMPTY;
            for (var cell : cells) {
                actualPkWall = PkWall.withTileAt(actualPkWall, cell.line, cell.color);
                expectedPkWall |= 1 << cell.index();
                assertEquals(expectedPkWall, actualPkWall);
            }
            Collections.shuffle(cells, rng);
        }
    }

    @Test
    void pkWallHasTileAtWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 100; i += 1) {
            var pkWall = rng.nextInt(1 << 25);
            var colors = new ArrayList<>(TileKind.Colored.ALL);
            var mask = 1;
            for (var line : Pattern.ALL) {
                for (var color : colors) {
                    var expected = (pkWall & mask) != 0;
                    assertEquals(expected, PkWall.hasTileAt(pkWall, line, color));
                    mask <<= 1;
                }
                Collections.rotate(colors, 1);
            }
        }
    }

    @Test
    void pkWallGroupSizeWorkOnFullWall() {
        var pkFullWall = (1 << 25) - 1;
        for (var line : Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                assertEquals(5, PkWall.hGroupSize(pkFullWall, line, color));
                assertEquals(5, PkWall.vGroupSize(pkFullWall, line, color));
            }
        }
    }

    @Test
    void pkWallGroupSizeWorksOnGivenExample() {
        var pkWall = PkWall.EMPTY;
        for (var i : new int[]{0, 2, 3, 6, 11, 13, 18, 23, 20, 21, 22, 24}) {
            var l = TileDestination.Pattern.ALL.get(i / 5);
            var c = i % 5;
            var col = PkWall.colorAt(l, c);
            pkWall = PkWall.withTileAt(pkWall, l, col);
        }

        assertEquals(1, PkWall.hGroupSize(pkWall, Pattern.PATTERN_1, TileKind.Colored.A));
        assertEquals(1, PkWall.vGroupSize(pkWall, Pattern.PATTERN_1, TileKind.Colored.A));

        assertEquals(2, PkWall.hGroupSize(pkWall, Pattern.PATTERN_1, TileKind.Colored.C));
        assertEquals(1, PkWall.vGroupSize(pkWall, Pattern.PATTERN_1, TileKind.Colored.C));

        assertEquals(5, PkWall.hGroupSize(pkWall, Pattern.PATTERN_5, TileKind.Colored.E));
        assertEquals(3, PkWall.vGroupSize(pkWall, Pattern.PATTERN_5, TileKind.Colored.E));
    }

    @Test
    void pkWallIsRowFullWorks() {
        var pkWall = 0b11111;
        for (var line : Pattern.ALL) {
            for (var line2 : Pattern.ALL) {
                var expected = line2 == line;
                assertEquals(expected, PkWall.isRowFull(pkWall, line2));
            }
            pkWall <<= 5;
        }
    }

    @Test
    void pkWallHasFullRowWorks() {
        var pkWall = 0b11111_00000_00000_00000_00000;
        for (var i = 0; i < 25; i += 1) {
            var expected = (i % 5) == 0;
            assertEquals(expected, PkWall.hasFullRow(pkWall));
            pkWall >>= 1;
        }
    }

    @Test
    void pkWallIsColumnFullWorks() {
        var pkWall = 0b00001_00001_00001_00001_00001;
        for (var column = 0; column < 5; column += 1) {
            for (var column2 = 0; column2 < 5; column2 += 1) {
                var expected = column2 == column;
                assertEquals(expected, PkWall.isColumnFull(pkWall, column2));
            }
            pkWall <<= 1;
        }
    }

    @Test
    void pkWallIsColorFullWorks() {
        var pkWall = 0b10000_01000_00100_00010_00001;
        for (var color : TileKind.Colored.ALL) {
            for (var color2 : TileKind.Colored.ALL) {
                var expected = color2 == color;
                assertEquals(expected, PkWall.isColorFull(pkWall, color2));
            }

            pkWall = pkWall << 1;
            var overflow = pkWall & 0b1_00001_00001_00001_00001_00000;
            pkWall ^= overflow;
            pkWall |= overflow >> 5;
        }
    }

    @Test
    void pkWallAsPkTileSetWorks() {
        assertEquals(PkTileSet.EMPTY, PkWall.asPkTileSet(PkWall.EMPTY));

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkWall = rng.nextInt(1 << 25);
            var expectedPkTileSet = PkTileSet.EMPTY;
            var colors = new ArrayList<>(TileKind.Colored.ALL);
            var mask = 1;
            for (var _ : Pattern.ALL) {
                for (var color : colors) {
                    if ((pkWall & mask) != 0)
                        expectedPkTileSet = PkTileSet.add(expectedPkTileSet, color);
                    mask <<= 1;
                }
                Collections.rotate(colors, 1);
            }
            assertEquals(expectedPkTileSet, PkWall.asPkTileSet(pkWall));
        }
    }


    @Test
    void pkWallToStringWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            var pkWall = rng.nextInt(1 << 25);
            var expectedStringJ = new StringJoiner(", ", "[", "]");
            var colors = new ArrayList<>(TileKind.Colored.ALL);
            var mask = 1;
            for (var _ : Pattern.ALL) {
                var lineB = new StringBuilder(5);
                for (var color : colors) {
                    var letter = color.name();
                    if ((pkWall & mask) == 0) letter = letter.toLowerCase();
                    lineB.append(letter);
                    mask <<= 1;
                }
                expectedStringJ.add(lineB.toString());
                Collections.rotate(colors, 1);
            }
            assertEquals(expectedStringJ.toString(), PkWall.toString(pkWall));
        }
    }
}