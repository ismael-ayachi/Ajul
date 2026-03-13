package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyPkWallTest {

    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    // ===================== EMPTY =====================

    @Test
    void pkWallEmptyIsCorrectlyDefined() {
        assertEquals(0, PkWall.EMPTY);
    }

    // ===================== indexOf =====================

    @Test
    void pkWallIndexOfReturnsBetween0And24() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var index = PkWall.indexOf(line, color);
                assertTrue(index >= 0 && index <= 24);
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIndexOfIsUniqueForEachCell() {
        var seen = new boolean[25];
        var remaining = 25;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var index = PkWall.indexOf(line, color);
                assertFalse(seen[index]);
                seen[index] = true;
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
        for (var s : seen) assertTrue(s);
    }

    @Test
    void pkWallIndexOfIsConsistentWithColumn() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                assertEquals(
                        line.index() * PkWall.WALL_WIDTH + PkWall.column(line, color),
                        PkWall.indexOf(line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    // ===================== column =====================

    @Test
    void pkWallColumnReturnsBetween0And4() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var col = PkWall.column(line, color);
                assertTrue(col >= 0 && col <= 4);
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallColumnIsUniquePerLine() {
        for (var line : TileDestination.Pattern.ALL) {
            var seen = new boolean[5];
            var remaining = 5;
            for (var color : TileKind.Colored.ALL) {
                var col = PkWall.column(line, color);
                assertFalse(seen[col]);
                seen[col] = true;
                remaining -= 1;
            }
            assertEquals(0, remaining);
        }
    }

    // ===================== colorAt =====================

    @Test
    void pkWallColorAtIsConsistentWithColumn() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                assertEquals(color, PkWall.colorAt(line, PkWall.column(line, color)));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallColorAtCoversAllColorsPerLine() {
        for (var line : TileDestination.Pattern.ALL) {
            var seen = new boolean[5];
            var remaining = 5;
            for (var col = 0; col < 5; col += 1) {
                seen[PkWall.colorAt(line, col).index()] = true;
                remaining -= 1;
            }
            assertEquals(0, remaining);
            for (var s : seen) assertTrue(s);
        }
    }

    // ===================== hasTileAt =====================

    @Test
    void pkWallHasTileAtReturnsFalseOnEmpty() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                assertFalse(PkWall.hasTileAt(PkWall.EMPTY, line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    // ===================== withTileAt / hasTileAt =====================

    @Test
    void pkWallWithTileAtAndHasTileAtAreConsistent() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color);
                assertTrue(PkWall.hasTileAt(pkWall, line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallWithTileAtDoesNotAffectOtherCells() {
        var remaining = 5 * 5 * (5 * 5 - 1);
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color);
                for (var otherLine : TileDestination.Pattern.ALL) {
                    for (var otherColor : TileKind.Colored.ALL) {
                        if (otherLine != line || otherColor != color) {
                            assertFalse(PkWall.hasTileAt(pkWall, otherLine, otherColor));
                            remaining -= 1;
                        }
                    }
                }
            }
        }
        assertEquals(0, remaining);
    }

    // ===================== hGroupSize =====================

    @Test
    void pkWallHGroupSizeIsOneForIsolatedTile() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color);
                assertEquals(1, PkWall.hGroupSize(pkWall, line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallHGroupSizeIsWallWidthForFullRow() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            var pkWall = PkWall.EMPTY;
            for (var color : TileKind.Colored.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
            for (var color : TileKind.Colored.ALL) {
                assertEquals(PkWall.WALL_WIDTH, PkWall.hGroupSize(pkWall, line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallHGroupSizeIsCorrectForTwoAdjacentTiles() {
        for (var line : TileDestination.Pattern.ALL) {
            for (var col = 0; col < PkWall.WALL_WIDTH - 1; col += 1) {
                var color1 = PkWall.colorAt(line, col);
                var color2 = PkWall.colorAt(line, col + 1);
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color1);
                pkWall = PkWall.withTileAt(pkWall, line, color2);
                assertEquals(2, PkWall.hGroupSize(pkWall, line, color1));
                assertEquals(2, PkWall.hGroupSize(pkWall, line, color2));
            }
        }
    }

    @Test
    void pkWallHGroupSizeIsOneWhenNeighboursAreEmpty() {
        // tuile au milieu, voisins vides → groupe de 1
        for (var line : TileDestination.Pattern.ALL) {
            var color = PkWall.colorAt(line, 2); // colonne centrale
            var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color);
            assertEquals(1, PkWall.hGroupSize(pkWall, line, color));
        }
    }

    // ===================== vGroupSize =====================

    @Test
    void pkWallVGroupSizeIsOneForIsolatedTile() {
        var remaining = 5 * 5;
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color);
                assertEquals(1, PkWall.vGroupSize(pkWall, line, color));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallVGroupSizeIsWallHeightForFullColumn() {
        var remaining = 5 * 5;
        for (var col = 0; col < PkWall.WALL_WIDTH; col += 1) {
            var pkWall = PkWall.EMPTY;
            for (var line : TileDestination.Pattern.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, PkWall.colorAt(line, col));
            for (var line : TileDestination.Pattern.ALL) {
                assertEquals(PkWall.WALL_HEIGHT, PkWall.vGroupSize(pkWall, line, PkWall.colorAt(line, col)));
                remaining -= 1;
            }
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallVGroupSizeIsCorrectForTwoAdjacentTiles() {
        for (var col = 0; col < PkWall.WALL_WIDTH; col += 1) {
            for (var lineIndex = 0; lineIndex < PkWall.WALL_HEIGHT - 1; lineIndex += 1) {
                var line1 = TileDestination.Pattern.ALL.get(lineIndex);
                var line2 = TileDestination.Pattern.ALL.get(lineIndex + 1);
                var color1 = PkWall.colorAt(line1, col);
                var color2 = PkWall.colorAt(line2, col);
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line1, color1);
                pkWall = PkWall.withTileAt(pkWall, line2, color2);
                assertEquals(2, PkWall.vGroupSize(pkWall, line1, color1));
                assertEquals(2, PkWall.vGroupSize(pkWall, line2, color2));
            }
        }
    }

    // ===================== isRowFull =====================

    @Test
    void pkWallIsRowFullReturnsFalseOnEmpty() {
        var remaining = 5;
        for (var line : TileDestination.Pattern.ALL) {
            assertFalse(PkWall.isRowFull(PkWall.EMPTY, line));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIsRowFullReturnsTrueWhenFull() {
        var remaining = 5;
        for (var line : TileDestination.Pattern.ALL) {
            var pkWall = PkWall.EMPTY;
            for (var color : TileKind.Colored.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
            assertTrue(PkWall.isRowFull(pkWall, line));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIsRowFullReturnsFalseForOtherRows() {
        var remaining = 5 * 4;
        for (var line : TileDestination.Pattern.ALL) {
            var pkWall = PkWall.EMPTY;
            for (var color : TileKind.Colored.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
            for (var otherLine : TileDestination.Pattern.ALL) {
                if (otherLine != line) {
                    assertFalse(PkWall.isRowFull(pkWall, otherLine));
                    remaining -= 1;
                }
            }
        }
        assertEquals(0, remaining);
    }

    // ===================== hasFullRow =====================

    @Test
    void pkWallHasFullRowReturnsFalseOnEmpty() {
        assertFalse(PkWall.hasFullRow(PkWall.EMPTY));
    }

    @Test
    void pkWallHasFullRowReturnsTrueWhenAnyRowIsFull() {
        var remaining = 5;
        for (var line : TileDestination.Pattern.ALL) {
            var pkWall = PkWall.EMPTY;
            for (var color : TileKind.Colored.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
            assertTrue(PkWall.hasFullRow(pkWall));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallHasFullRowReturnsFalseWhenAllRowsHaveMissingTile() {
        // chaque ligne a 4 tuiles sur 5 → aucune ligne complète
        for (var line : TileDestination.Pattern.ALL) {
            var pkWall = PkWall.EMPTY;
            var added = 0;
            for (var color : TileKind.Colored.ALL) {
                if (added < 4) {
                    pkWall = PkWall.withTileAt(pkWall, line, color);
                    added++;
                }
            }
            assertFalse(PkWall.hasFullRow(pkWall));
        }
    }

    // ===================== isColumnFull =====================

    @Test
    void pkWallIsColumnFullReturnsFalseOnEmpty() {
        var remaining = 5;
        for (var col = 0; col < PkWall.WALL_WIDTH; col += 1) {
            assertFalse(PkWall.isColumnFull(PkWall.EMPTY, col));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIsColumnFullReturnsTrueWhenFull() {
        var remaining = 5;
        for (var col = 0; col < PkWall.WALL_WIDTH; col += 1) {
            var pkWall = PkWall.EMPTY;
            for (var line : TileDestination.Pattern.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, PkWall.colorAt(line, col));
            assertTrue(PkWall.isColumnFull(pkWall, col));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIsColumnFullReturnsFalseForOtherColumns() {
        var remaining = 5 * 4;
        for (var col = 0; col < PkWall.WALL_WIDTH; col += 1) {
            var pkWall = PkWall.EMPTY;
            for (var line : TileDestination.Pattern.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, PkWall.colorAt(line, col));
            for (var otherCol = 0; otherCol < PkWall.WALL_WIDTH; otherCol += 1) {
                if (otherCol != col) {
                    assertFalse(PkWall.isColumnFull(pkWall, otherCol));
                    remaining -= 1;
                }
            }
        }
        assertEquals(0, remaining);
    }

    // ===================== isColorFull =====================

    @Test
    void pkWallIsColorFullReturnsFalseOnEmpty() {
        var remaining = 5;
        for (var color : TileKind.Colored.ALL) {
            assertFalse(PkWall.isColorFull(PkWall.EMPTY, color));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIsColorFullReturnsTrueWhenFull() {
        var remaining = 5;
        for (var color : TileKind.Colored.ALL) {
            var pkWall = PkWall.EMPTY;
            for (var line : TileDestination.Pattern.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
            assertTrue(PkWall.isColorFull(pkWall, color));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkWallIsColorFullReturnsFalseForOtherColors() {
        var remaining = 5 * 4;
        for (var color : TileKind.Colored.ALL) {
            var pkWall = PkWall.EMPTY;
            for (var line : TileDestination.Pattern.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
            for (var otherColor : TileKind.Colored.ALL) {
                if (otherColor != color) {
                    assertFalse(PkWall.isColorFull(pkWall, otherColor));
                    remaining -= 1;
                }
            }
        }
        assertEquals(0, remaining);
    }

    // ===================== asPkTileSet =====================

    @Test
    void pkWallAsPkTileSetOnEmptyIsEmpty() {
        assertEquals(PkTileSet.EMPTY, PkWall.asPkTileSet(PkWall.EMPTY));
    }

    @Test
    void pkWallAsPkTileSetCountsCorrectlyOnRandomWalls() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var i = 0; i < 1000; i += 1) {
            var pkWall = PkWall.EMPTY;
            var expectedCounts = new int[5];
            for (var line : TileDestination.Pattern.ALL) {
                for (var color : TileKind.Colored.ALL) {
                    if (rng.nextBoolean()) {
                        pkWall = PkWall.withTileAt(pkWall, line, color);
                        expectedCounts[color.index()] += 1;
                    }
                }
            }
            var pkTileSet = PkWall.asPkTileSet(pkWall);
            for (var color : TileKind.Colored.ALL)
                assertEquals(expectedCounts[color.index()], PkTileSet.countOf(pkTileSet, color));
        }
    }

    @Test
    void pkWallAsPkTileSetOnFullWallHas5OfEachColor() {
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            for (var color : TileKind.Colored.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
        var pkTileSet = PkWall.asPkTileSet(pkWall);
        for (var color : TileKind.Colored.ALL)
            assertEquals(5, PkTileSet.countOf(pkTileSet, color));
    }

    // ===================== toString =====================

    @Test
    void pkWallToStringOnEmpty() {
        assertEquals("[abcde, eabcd, deabc, cdeab, bcdea]", PkWall.toString(PkWall.EMPTY));
    }

    @Test
    void pkWallToStringOnFullWall() {
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            for (var color : TileKind.Colored.ALL)
                pkWall = PkWall.withTileAt(pkWall, line, color);
        assertEquals("[ABCDE, EABCD, DEABC, CDEAB, BCDEA]", PkWall.toString(pkWall));
    }

    @Test
    void pkWallToStringHasCorrectBrackets() {
        var s = PkWall.toString(PkWall.EMPTY);
        assertTrue(s.startsWith("["));
        assertTrue(s.endsWith("]"));
    }

    @Test
    void pkWallToStringHasExactlyFourCommas() {
        var s = PkWall.toString(PkWall.EMPTY);
        assertEquals(4, s.chars().filter(c -> c == ',').count());
    }

    @Test
    void pkWallToStringUsesUpperCaseForOccupiedCells() {
        for (var line : TileDestination.Pattern.ALL) {
            for (var color : TileKind.Colored.ALL) {
                var pkWall = PkWall.withTileAt(PkWall.EMPTY, line, color);
                var s = PkWall.toString(pkWall);
                // La lettre de la couleur placée doit apparaître en majuscule
                assertTrue(s.contains(color.toString().toUpperCase()));
            }
        }
    }
}