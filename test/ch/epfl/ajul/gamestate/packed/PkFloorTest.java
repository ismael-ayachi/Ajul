package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.TileKind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PkFloorTest {
    @Test
    void pkFloorEmptyIsCorrectlyDefined() {
        assertEquals(0, PkFloor.EMPTY);
    }

    @Test
    void pkFloorSizeWorks() {
        var tiles = 0b001_010_011_100_101_000_001;
        for (var size = 0; size <= 7; size += 1) {
            var pkFloor = ((tiles >> (3 * (7 - size))) << 3) | size;
            assertEquals(size, PkFloor.size(pkFloor));
        }
    }

    @Test
    void pkFloorTileAtWorks() {
        var tiles = 0b001_010_011_100_101_000_001;
        for (var size = 1; size <= 7; size += 1) {
            var pkFloor = ((tiles >> (3 * (7 - size))) << 3) | size;
            for (int i = 0; i < size; i += 1) {
                var expectedTile = TileKind.ALL.get((pkFloor >> (3 * (i + 1))) & 0b111);
                assertEquals(expectedTile, PkFloor.tileAt(pkFloor, i));
            }
        }
    }

    @Test
    void pkFloorWithAddedTilesWorksWhenBelowCapacity() {
        var tiles = new ArrayList<TileKind>();
        for (var kind : TileKind.ALL)
            tiles.addAll(Collections.nCopies(kind.tilesCount(), kind));

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            Collections.shuffle(tiles, rng);

            var pkTileSet = PkTileSet.EMPTY;
            var count = rng.nextInt(8);
            for (var j = 0; j < count; j += 1)
                pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(1, tiles.get(j)));

            var expectedList = tiles.subList(0, count);
            expectedList.sort(Comparator.comparingInt(TileKind::index));

            var expected = PkFloor.EMPTY;
            for (var tile : expectedList.reversed()) {
                expected = (expected << 3) | tile.index();
            }
            expected = (expected << 3) | count;

            assertEquals(expected, PkFloor.withAddedTiles(PkFloor.EMPTY, pkTileSet));
        }
    }

    @Test
    void pkFloorWithAddedTilesWorksWhenAboveCapacity() {
        var tiles = new ArrayList<TileKind>();
        for (var kind : TileKind.ALL)
            tiles.addAll(Collections.nCopies(kind.tilesCount(), kind));

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            Collections.shuffle(tiles, rng);

            var pkTileSet = PkTileSet.EMPTY;
            var count = rng.nextInt(8, 20);
            for (var j = 0; j < count; j += 1)
                pkTileSet = PkTileSet.union(pkTileSet, PkTileSet.of(1, tiles.get(j)));

            var expectedList = tiles.subList(0, count);
            var containsMarker = expectedList.contains(TileKind.FIRST_PLAYER_MARKER);
            expectedList.sort(Comparator.comparingInt(TileKind::index));
            expectedList = expectedList.subList(0, 7);

            var expected = PkFloor.EMPTY;
            for (var tile : expectedList.reversed())
                expected = (expected << 3) | tile.index();
            expected = (expected << 3) | 7;

            if (containsMarker)
                expected = (expected & 0b111_111_111_111_111_111_111)
                        | 0b101_000_000_000_000_000_000_000;

            var actual = PkFloor.withAddedTiles(PkFloor.EMPTY, pkTileSet);
            assertEquals(expected, actual);
        }
    }

    @Test
    void pkFloorAsPkTileSetWorks() {
        var tiles = new ArrayList<TileKind>();
        for (var kind : TileKind.ALL)
            tiles.addAll(Collections.nCopies(kind.tilesCount(), kind));

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            Collections.shuffle(tiles, rng);

            var pkTileSet = PkTileSet.EMPTY;
            var pkFloor = PkFloor.EMPTY;
            var count = rng.nextInt(0, 8);
            for (var j = 0; j < count; j += 1) {
                var tile = tiles.get(j);
                pkFloor = (pkFloor << 3) | tile.index();
                pkTileSet = PkTileSet.add(pkTileSet, tile);
            }
            pkFloor = (pkFloor << 3) | count;
            System.out.println(PkFloor.toString(pkFloor));

            assertEquals(pkTileSet, PkFloor.asPkTileSet(pkFloor));
        }
    }

    @Test
    void pkFloorContainsFirstPlayerMarkerWorks() {
        var tiles = new ArrayList<TileKind>();
        for (var kind : TileKind.ALL)
            tiles.addAll(Collections.nCopies(kind.tilesCount(), kind));

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 1000; i += 1) {
            Collections.shuffle(tiles, rng);

            var count = rng.nextInt(8);
            var containsMarker = false;
            var pkFloor = PkFloor.EMPTY;
            for (var j = 0; j < count; j += 1) {
                var tile = tiles.get(j);
                pkFloor = (pkFloor << 3) | tile.index();
                containsMarker |= tile == TileKind.FIRST_PLAYER_MARKER;
            }
            pkFloor = (pkFloor << 3) | count;

            assertEquals(containsMarker, PkFloor.containsFirstPlayerMarker(pkFloor));
        }
    }

    @Test
    void pkFloorToStringWorksOnKnownCases() {
        assertEquals("[]", PkFloor.toString(PkFloor.EMPTY));

        var givenPkFloor = 0b00000000_000_000_000_000_001_001_101_011;
        assertEquals("[FIRST_PLAYER_MARKER, B, B]", PkFloor.toString(givenPkFloor));

        var pkFloorA = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.A));
        assertEquals("[A, A, A, A, A, A, A]", PkFloor.toString(pkFloorA));

        var pkFloorABC = PkFloor.EMPTY;
        pkFloorABC = PkFloor.withAddedTiles(pkFloorABC, PkTileSet.of(1, TileKind.A));
        pkFloorABC = PkFloor.withAddedTiles(pkFloorABC, PkTileSet.of(2, TileKind.B));
        pkFloorABC = PkFloor.withAddedTiles(pkFloorABC, PkTileSet.of(3, TileKind.C));
        assertEquals("[A, B, B, C, C, C]", PkFloor.toString(pkFloorABC));
    }
}